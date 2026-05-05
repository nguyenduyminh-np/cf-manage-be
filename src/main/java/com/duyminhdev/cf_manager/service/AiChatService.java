package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.request.chat.ChatRequest;
import com.duyminhdev.cf_manager.dto.response.chat.ChatResponse;
import org.springframework.security.core.Authentication;

/**
 * Service xử lý nghiệp vụ cho AI Chatbot.
 */
public interface AiChatService {

    /**
     * Xử lý tin nhắn chat từ client.
     * Hỗ trợ cả khách vãng lai (public) và nhân viên (staff có Authentication).
     *
     * @param request        chứa message và sessionId
     * @param authentication có thể null nếu gọi từ public endpoint
     * @return ChatResponse chứa câu trả lời và sessionId
     */
    ChatResponse processChat(ChatRequest request, Authentication authentication);
}
