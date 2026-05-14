package com.wtd.service.impl;

import com.wtd.config.DeepSeekProperties;
import com.wtd.dto.ChatRequestDto;
import com.wtd.entity.ChatRecord;
import com.wtd.service.ChatService;
import com.wtd.vo.ChatResponseVo;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class ChatServiceImpl implements ChatService {

    private static final Set<String> SUPPORTED_MODELS = Set.of("deepseek-v4-flash", "deepseek-v4-pro");
    private static final String CHAT_SESSION_KEY_PREFIX = "chat:session:";
    private static final int HISTORY_ROUNDS = 3;
    private static final long SESSION_TTL_HOURS = 24;

    private final RestClient restClient;
    private final DeepSeekProperties deepSeekProperties;
    private final StringRedisTemplate stringRedisTemplate;

    public ChatServiceImpl(DeepSeekProperties deepSeekProperties, StringRedisTemplate stringRedisTemplate) {
        this.deepSeekProperties = deepSeekProperties;
        this.stringRedisTemplate = stringRedisTemplate;

        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(Duration.ofSeconds(10));
        requestFactory.setReadTimeout(Duration.ofSeconds(120));

        this.restClient = RestClient.builder()
                .baseUrl(trimTrailingSlash(deepSeekProperties.getBaseUrl()))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deepSeekProperties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .requestFactory(requestFactory)
                .build();
    }

    @PostConstruct
    private void validateConfig() {
        if (deepSeekProperties.getApiKey() == null || deepSeekProperties.getApiKey().isBlank()) {
            throw new IllegalStateException("未读取到 DEEPSEEK_API_KEY 环境变量，请在配置中设置后再启动项目");
        }
        log.info("DeepSeek 配置校验通过, baseUrl={}, model={}",
                deepSeekProperties.getBaseUrl(), deepSeekProperties.getChat().getOptions().getModel());
    }

    @Override
    public ChatResponseVo chat(ChatRequestDto requestDto) {
        String sessionId = requestDto.getSessionId().trim();
        String message = requestDto.getMessage().trim();
        String actualModel = resolveModel(requestDto.getModel());

        List<String> historyRecords = readRecentHistory(sessionId);
        String finalPrompt = buildPrompt(historyRecords, message);

        DeepSeekRequest request = new DeepSeekRequest(
                actualModel,
                List.of(
                        new DeepSeekMessage("system", "你是一名专业、友好、简洁的中文智能助手。回答时请结合历史对话上下文，保持同一会话的连续性。"),
                        new DeepSeekMessage("user", finalPrompt)
                ),
                deepSeekProperties.getChat().getOptions().getTemperature()
        );

        DeepSeekResponse response = restClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .onStatus(HttpStatusCode::is4xxClientError, (req, res) -> {
                    String body = new String(res.getBody().readAllBytes());
                    log.warn("DeepSeek API 客户端错误, status={}, body={}", res.getStatusCode(), body);
                    throw new IllegalStateException("请求 DeepSeek 失败（" + res.getStatusCode() + "），请检查配置或稍后重试");
                })
                .onStatus(HttpStatusCode::is5xxServerError, (req, res) -> {
                    log.warn("DeepSeek API 服务端错误, status={}", res.getStatusCode());
                    throw new IllegalStateException("DeepSeek 服务暂时不可用，请稍后重试");
                })
                .body(DeepSeekResponse.class);

        String answer = extractAnswer(response);
        saveCurrentRound(sessionId, message, answer);
        return new ChatResponseVo(sessionId, message, answer);
    }

    private List<String> readRecentHistory(String sessionId) {
        String redisKey = buildRedisKey(sessionId);
        try {
            List<String> records = stringRedisTemplate.opsForList().range(redisKey, -HISTORY_ROUNDS, -1);
            return records == null ? List.of() : records;
        } catch (DataAccessException ex) {
            log.warn("读取 Redis 聊天历史失败, sessionId={}", sessionId, ex);
            return List.of();
        }
    }

    private void saveCurrentRound(String sessionId, String message, String answer) {
        String redisKey = buildRedisKey(sessionId);
        ChatRecord chatRecord = new ChatRecord(sessionId, message, answer, LocalDateTime.now());

        try {
            stringRedisTemplate.opsForList().rightPush(redisKey, chatRecord.toPromptText());
            stringRedisTemplate.opsForList().trim(redisKey, -HISTORY_ROUNDS, -1);
            stringRedisTemplate.expire(redisKey, SESSION_TTL_HOURS, TimeUnit.HOURS);
        } catch (DataAccessException ex) {
            log.warn("写入 Redis 聊天历史失败, sessionId={}", sessionId, ex);
        }
    }

    private String buildPrompt(List<String> historyRecords, String message) {
        if (historyRecords.isEmpty()) {
            return "当前用户问题:\n" + message;
        }

        List<String> promptParts = new ArrayList<>();
        promptParts.add("以下是历史对话:");
        promptParts.add(String.join("\n\n", historyRecords));
        promptParts.add("当前用户问题:\n" + message);
        return String.join("\n\n", promptParts);
    }

    private String buildRedisKey(String sessionId) {
        return CHAT_SESSION_KEY_PREFIX + sessionId;
    }

    private String resolveModel(String requestModel) {
        String fallbackModel = deepSeekProperties.getChat().getOptions().getModel();
        if (requestModel == null || requestModel.isBlank()) {
            return fallbackModel;
        }

        if (!SUPPORTED_MODELS.contains(requestModel)) {
            throw new IllegalArgumentException("当前仅支持模型 deepseek-v4-flash, deepseek-v4-pro");
        }

        return requestModel;
    }

    private String extractAnswer(DeepSeekResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new IllegalStateException("DeepSeek 返回为空，请稍后重试");
        }

        DeepSeekChoice choice = response.getChoices().get(0);
        if (choice == null || choice.getMessage() == null || choice.getMessage().getContent() == null || choice.getMessage().getContent().isBlank()) {
            throw new IllegalStateException("DeepSeek 未返回有效内容，请稍后重试");
        }

        return choice.getMessage().getContent();
    }

    private String trimTrailingSlash(String url) {
        Objects.requireNonNull(url, "DeepSeek base-url 不能为空");
        return url.endsWith("/") ? url.substring(0, url.length() - 1) : url;
    }

    public static class DeepSeekRequest {
        private final String model;
        private final List<DeepSeekMessage> messages;
        private final Double temperature;

        public DeepSeekRequest(String model, List<DeepSeekMessage> messages, Double temperature) {
            this.model = model;
            this.messages = messages;
            this.temperature = temperature;
        }

        public String getModel() {
            return model;
        }

        public List<DeepSeekMessage> getMessages() {
            return messages;
        }

        public Double getTemperature() {
            return temperature;
        }
    }

    public static class DeepSeekMessage {
        private final String role;
        private final String content;

        public DeepSeekMessage(String role, String content) {
            this.role = role;
            this.content = content;
        }

        public String getRole() {
            return role;
        }

        public String getContent() {
            return content;
        }
    }

    public static class DeepSeekResponse {
        private List<DeepSeekChoice> choices;

        public List<DeepSeekChoice> getChoices() {
            return choices;
        }

        public void setChoices(List<DeepSeekChoice> choices) {
            this.choices = choices;
        }
    }

    public static class DeepSeekChoice {
        private DeepSeekMessageContent message;

        public DeepSeekMessageContent getMessage() {
            return message;
        }

        public void setMessage(DeepSeekMessageContent message) {
            this.message = message;
        }
    }

    public static class DeepSeekMessageContent {
        private String content;

        public String getContent() {
            return content;
        }

        public void setContent(String content) {
            this.content = content;
        }
    }
}
