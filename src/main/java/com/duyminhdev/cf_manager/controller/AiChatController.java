package com.duyminhdev.cf_manager.controller;

import com.duyminhdev.cf_manager.dto.base.ApiResponse;
import com.duyminhdev.cf_manager.dto.request.chat.ChatRequest;
import com.duyminhdev.cf_manager.dto.response.chat.ChatResponse;
import com.duyminhdev.cf_manager.service.AiChatService;
import com.duyminhdev.cf_manager.security.authorization.AdminOnlyAccess;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class AiChatController {

    private final AiChatService aiChatService;

    /**
     * Endpoint dành cho nhân viên.
     * Yêu cầu JWT. Có các quyền mở rộng như xem doanh thu, xem tồn kho.
     */
    @PostMapping("/staff")
    @AdminOnlyAccess
    public ApiResponse<ChatResponse> staffChat(
            @Valid @RequestBody ChatRequest request,
            Authentication authentication) {
        ChatResponse response = aiChatService.processChat(request, authentication);
        return new ApiResponse<>(200, "SUCCESS", response);
    }
}
