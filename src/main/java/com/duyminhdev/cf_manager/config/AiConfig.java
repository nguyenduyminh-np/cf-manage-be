package com.duyminhdev.cf_manager.config;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Cấu hình Spring AI ChatClient (Gemini).
 * Bean này sẽ được inject vào AiChatServiceImpl để gọi Gemini API.
 */
@Configuration
public class AiConfig {

    @Value("${chat.ai.enabled:true}")
    private boolean aiEnabled;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        return builder.build();
    }
}
