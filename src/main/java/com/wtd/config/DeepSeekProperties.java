package com.wtd.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "spring.ai.deepseek")
public class DeepSeekProperties {
    private String apiKey;
    private String baseUrl = "https://api.deepseek.com";
    private Chat chat = new Chat();

    @Data
    public static class Chat {
        private Options options = new Options();
    }

    @Data
    public static class Options {
        private String model = "deepseek-v4-flash";
        private Double temperature = 0.7;
    }
}
