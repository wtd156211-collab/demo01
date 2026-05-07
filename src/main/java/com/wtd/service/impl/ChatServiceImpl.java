package com.wtd.service.impl;

import com.wtd.config.DeepSeekProperties;
import com.wtd.service.ChatService;
import com.wtd.vo.ChatResponseVo;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Objects;
import java.util.Set;

@Service
public class ChatServiceImpl implements ChatService {

    private static final Set<String> SUPPORTED_MODELS = Set.of("deepseek-v4-flash", "deepseek-v4-pro");

    private final RestClient restClient;
    private final DeepSeekProperties deepSeekProperties;

    public ChatServiceImpl(DeepSeekProperties deepSeekProperties) {
        this.deepSeekProperties = deepSeekProperties;
        this.restClient = RestClient.builder()
                .baseUrl(trimTrailingSlash(deepSeekProperties.getBaseUrl()))
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + deepSeekProperties.getApiKey())
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public ChatResponseVo chat(String message, String model) {
        if (deepSeekProperties.getApiKey() == null || deepSeekProperties.getApiKey().isBlank()) {
            throw new IllegalStateException("未读取到 DEEPSEEK_API_KEY 环境变量，请先配置后再启动项目");
        }

        String actualModel = resolveModel(model);
        DeepSeekRequest request = new DeepSeekRequest(
                actualModel,
                List.of(
                        new DeepSeekMessage("system", "你是一名专业、友好、简洁的中文智能助手，请根据用户的问题直接给出清晰回答。"),
                        new DeepSeekMessage("user", message)
                ),
                deepSeekProperties.getChat().getOptions().getTemperature()
        );

        DeepSeekResponse response = restClient.post()
                .uri("/chat/completions")
                .body(request)
                .retrieve()
                .body(DeepSeekResponse.class);

        String answer = extractAnswer(response);
        return new ChatResponseVo(message, answer);
    }

    private String resolveModel(String requestModel) {
        String fallbackModel = deepSeekProperties.getChat().getOptions().getModel();
        if (requestModel == null || requestModel.isBlank()) {
            return fallbackModel;
        }

        if (!SUPPORTED_MODELS.contains(requestModel)) {
            throw new IllegalArgumentException("当前仅支持模型: deepseek-v4-flash, deepseek-v4-pro");
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
