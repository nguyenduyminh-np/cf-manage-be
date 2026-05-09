package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.config.CafeInfoProperties;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.IngredientStockDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.SalesSummaryDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.TableAvailabilityDTO;
import com.duyminhdev.cf_manager.dto.request.chat.ChatRequest;
import com.duyminhdev.cf_manager.dto.response.chat.ChatResponse;
import com.duyminhdev.cf_manager.entity.ConversationMessage;
import com.duyminhdev.cf_manager.entity.UserPreference;
import com.duyminhdev.cf_manager.enums.IntentType;
import com.duyminhdev.cf_manager.repository.ConversationMessageRepository;
import com.duyminhdev.cf_manager.repository.UserPreferenceRepository;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlChatRepository;
import com.duyminhdev.cf_manager.service.AiChatService;
import com.duyminhdev.cf_manager.tools.ChatToolAuthFilter;
import com.duyminhdev.cf_manager.tools.ChatToolFunctions;
import com.duyminhdev.cf_manager.utils.chat.BookingParser;
import com.duyminhdev.cf_manager.utils.chat.ChatTimeUtils;
import com.duyminhdev.cf_manager.utils.chat.TableQueryParser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Pattern;

/**
 * Service chính xử lý nghiệp vụ AI Chatbot.
 * <p>
 * Kiến trúc v2 — Dual-mode:
 * <ul>
 *   <li><b>AI Mode (mặc định):</b> Sử dụng Gemini Function Calling với {@link ChatToolFunctions}.
 *       AI tự quyết định gọi tool nào dựa trên ngữ cảnh tin nhắn.</li>
 *   <li><b>Regex Fallback:</b> Khi AI bị disable hoặc quota exceeded, tự động chuyển về
 *       xử lý rule-based với regex pattern (giữ nguyên logic v1).</li>
 * </ul>
 * <p>
 * Flow xử lý:
 * <pre>
 * User message → Save history → AI enabled?
 *   ├─ Yes → Build context + Filter tools by role → Gemini + Tool Calling → Response
 *   └─ No  → Regex detect intent → Handler → Response
 *            (hoặc khi AI gặp lỗi, tự động fallback về đây)
 * </pre>
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiChatServiceImpl implements AiChatService {

    private final ChatClient chatClient;
    private final ConversationMessageRepository conversationRepo;
    private final UserPreferenceRepository preferenceRepo;
    private final NativeSqlChatRepository nativeSqlChatRepository;
    private final CafeInfoProperties cafeInfoProperties;
    private final StringRedisTemplate redisTemplate;

    // ===== v2: Tool Calling dependencies =====
    private final ChatToolFunctions chatToolFunctions;
    private final ChatToolAuthFilter toolAuthFilter;

    @Value("${chat.ai.enabled:true}")
    private boolean aiEnabled;

    @Value("${chat.ai.quota-backoff-ms:300000}")
    private long quotaBackoffMs;

    @Value("${chat.cache.table-availability-ttl-seconds:30}")
    private long tableAvailabilityTtlSeconds;

    private volatile long aiQuotaBackoffUntilMs = 0;

    // ===== Regex Intent Rules (giữ nguyên v1 cho fallback) =====
    private static final Pattern INTENT_TABLE_AVAIL = Pattern.compile("trống|còn bàn|bàn nào", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern INTENT_BOOK_TABLE = Pattern.compile("đặt bàn|book bàn|giữ bàn", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern INTENT_MENU = Pattern.compile("thực đơn|menu|có món|loại|đồ uống", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern INTENT_SALES = Pattern.compile("doanh thu|bán được bao nhiêu", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern INTENT_INVENTORY = Pattern.compile("tồn kho|sắp hết|hết hạn|nguyên liệu", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern INTENT_PURCHASE = Pattern.compile("nhập hàng|tạo đơn nhập", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
    private static final Pattern INTENT_FAQ = Pattern.compile("mở cửa|đóng cửa|địa chỉ|ở đâu|wifi|đỗ xe|giữ xe|pass wifi", Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);


    // =========================================================================
    // MAIN ENTRY POINT
    // =========================================================================

    @Override
    @Transactional
    public ChatResponse processChat(ChatRequest request, Authentication authentication) {
        String message = request.getMessage().trim();
        String userId = resolveUserId(authentication, request.getUserId());
        String sessionId = StringUtils.hasText(request.getSessionId()) ? request.getSessionId() : UUID.randomUUID().toString();

        // 1. Lưu tin nhắn user
        saveMessage(userId, sessionId, "user", message);

        String reply;
        String intentType = null;

        // 2. Chọn mode xử lý
        if (aiEnabled && System.currentTimeMillis() >= aiQuotaBackoffUntilMs) {
            // ===== V2: AI-driven with Tool Calling =====
            reply = callAIWithTools(message, userId, sessionId, authentication);

            if (!StringUtils.hasText(reply)) {
                // AI thất bại → fallback về regex
                log.warn("AI tool calling returned empty, falling back to regex");
                IntentType intent = detectIntent(message);
                intentType = intent.name();
                reply = handleByRegex(message, intent, authentication);
            }
        } else {
            // ===== V1 Fallback: Regex-based =====
            IntentType intent = detectIntent(message);
            intentType = intent.name();
            reply = handleByRegex(message, intent, authentication);
        }

        // 3. Hard fallback nếu vẫn không có reply
        if (!StringUtils.hasText(reply)) {
            reply = "Xin lỗi, hệ thống AI hiện đang bận hoặc tôi chưa hiểu ý bạn. "
                    + "Vui lòng thử lại sau hoặc diễn đạt cách khác.";
        }

        // 4. Lưu tin nhắn assistant
        saveMessage(userId, sessionId, "assistant", reply);

        // 5. Cập nhật User Preferences
        updateUserPreferences(userId, message, detectIntent(message));

        return ChatResponse.builder()
                .reply(reply)
                .sessionId(sessionId)
                .intentType(intentType)
                .timestamp(Instant.now())
                .build();
    }


    // =========================================================================
    // V2: AI + TOOL CALLING
    // =========================================================================

    /**
     * Gọi Gemini với Tool Calling.
     * Tools được lọc theo role của user qua {@link ChatToolAuthFilter}.
     * Context (lịch sử chat, sở thích) được inject vào user prompt.
     */
    private String callAIWithTools(String message, String userId, String sessionId,
                                   Authentication auth) {
        try {
            // Build user prompt kèm context
            String userPrompt = buildUserPromptWithContext(message, userId, sessionId);

            // Lọc tool callbacks theo role
            Set<String> allowedToolNames = toolAuthFilter.getAllowedToolNames(auth);
            ToolCallback[] allCallbacks = ToolCallbacks.from(chatToolFunctions);
            ToolCallback[] filteredTools = filterToolCallbacks(allCallbacks, allowedToolNames);

            log.debug("AI call with {} tools for user {}", filteredTools.length, userId);

            return chatClient.prompt()
                    .user(userPrompt)
                    .toolCallbacks(filteredTools)
                    .call()
                    .content();
        } catch (Exception e) {
            log.error("AI call with tools failed: {}", e.getMessage(), e);
            if (isQuotaExceeded(e)) {
                aiQuotaBackoffUntilMs = System.currentTimeMillis() + quotaBackoffMs;
                log.warn("AI quota exceeded, backing off for {}ms", quotaBackoffMs);
            }
            return null; // Sẽ fallback về regex
        }
    }

    /**
     * Lọc ToolCallback[] theo tên tool được phép.
     */
    private ToolCallback[] filterToolCallbacks(ToolCallback[] allCallbacks, Set<String> allowedNames) {
        return java.util.Arrays.stream(allCallbacks)
                .filter(tc -> allowedNames.contains(tc.getToolDefinition().name()))
                .toArray(ToolCallback[]::new);
    }

    /**
     * Build user prompt kèm context: sở thích, lịch sử chat.
     * System prompt (quy tắc, thông tin quán) đã được set mặc định trong AiConfig.
     */
    private String buildUserPromptWithContext(String message, String userId, String sessionId) {
        StringBuilder prompt = new StringBuilder();

        // Sở thích khách hàng
        preferenceRepo.findByUserId(userId).ifPresent(pref -> {
            prompt.append("[SỞ THÍCH KHÁCH HÀNG] ");
            if (pref.getPreferredTableFloor() != null) {
                prompt.append("Thích ngồi tầng ").append(pref.getPreferredTableFloor()).append(". ");
            }
            if (pref.getPreferredDishType() != null) {
                prompt.append("Thích ").append(pref.getPreferredDishType()).append(". ");
            }
            if (pref.getPreferredPriceRange() != null) {
                prompt.append("Mức giá: ").append(pref.getPreferredPriceRange()).append(". ");
            }
            prompt.append("\n");
        });

        // Lịch sử chat (context window)
        List<ConversationMessage> history = conversationRepo
                .findTop10ByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);
        if (!history.isEmpty()) {
            prompt.append("[LỊCH SỬ CHAT GẦN ĐÂY]\n");
            for (ConversationMessage msg : history) {
                prompt.append(msg.getRole().equals("user") ? "Khách: " : "Bot: ")
                      .append(msg.getContent()).append("\n");
            }
            prompt.append("\n");
        }

        prompt.append("Câu hỏi hiện tại: ").append(message);
        return prompt.toString();
    }


    // =========================================================================
    // V1: REGEX FALLBACK (giữ nguyên logic cũ)
    // =========================================================================

    /**
     * Xử lý tin nhắn bằng regex rule-based (v1).
     * Được sử dụng khi AI bị disable hoặc gặp lỗi.
     */
    private String handleByRegex(String message, IntentType intent, Authentication auth) {
        return switch (intent) {
            case TABLE_AVAILABILITY -> handleTableAvailability(message);
            case BOOK_TABLE -> handleBookTable(message, auth);
            case MENU_QUERY -> null; // Không hỗ trợ menu query trong regex mode
            case SALES_REPORT -> handleSalesReport(message, auth);
            case INVENTORY_CHECK -> handleInventoryCheck(message, auth);
            case CREATE_PURCHASE_ORDER -> handleCreatePurchaseOrder(message, auth);
            case FAQ -> hardFallbackReply(intent);
            case UNKNOWN -> hardFallbackReply(intent);
        };
    }


    // =========================================================================
    // HELPER METHODS (giữ nguyên từ v1)
    // =========================================================================

    private String resolveUserId(Authentication auth, String clientUserId) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName();
        }
        return "guest_" + (StringUtils.hasText(clientUserId) ? clientUserId : UUID.randomUUID().toString());
    }

    private IntentType detectIntent(String message) {
        if (INTENT_BOOK_TABLE.matcher(message).find()) return IntentType.BOOK_TABLE;
        if (INTENT_TABLE_AVAIL.matcher(message).find()) return IntentType.TABLE_AVAILABILITY;
        if (INTENT_SALES.matcher(message).find()) return IntentType.SALES_REPORT;
        if (INTENT_PURCHASE.matcher(message).find()) return IntentType.CREATE_PURCHASE_ORDER;
        if (INTENT_INVENTORY.matcher(message).find()) return IntentType.INVENTORY_CHECK;
        if (INTENT_MENU.matcher(message).find()) return IntentType.MENU_QUERY;
        if (INTENT_FAQ.matcher(message).find()) return IntentType.FAQ;
        return IntentType.UNKNOWN;
    }

    private void saveMessage(String userId, String sessionId, String role, String content) {
        ConversationMessage msg = ConversationMessage.builder()
                .userId(userId)
                .sessionId(sessionId)
                .role(role)
                .content(content)
                .build();
        conversationRepo.save(msg);
    }

    private void updateUserPreferences(String userId, String message, IntentType intent) {
        // Implementation đơn giản để demo. Trong thực tế có thể dùng AI để trích xuất sở thích.
        UserPreference pref = preferenceRepo.findByUserId(userId)
                .orElse(UserPreference.builder().userId(userId).build());

        if (intent == IntentType.BOOK_TABLE || intent == IntentType.TABLE_AVAILABILITY) {
            TableQueryParser.TableQueryParams params = TableQueryParser.parse(message);
            if (params != null && params.getFloor() != null) {
                pref.setPreferredTableFloor(params.getFloor());
            }
        }
        preferenceRepo.save(pref);
    }

    // ==========================================
    // Regex Intent Handlers (v1 — giữ nguyên)
    // ==========================================

    private String handleTableAvailability(String message) {
        TableQueryParser.TableQueryParams params = TableQueryParser.parse(message);
        if (params == null || params.getStartTime() == null || params.getEndTime() == null) {
            return null; // Fallback to hard reply
        }

        Instant start = params.startInstant();
        Instant end = params.endInstant();

        List<TableAvailabilityDTO> tables = nativeSqlChatRepository.findAvailableTables(params.getFloor(), start, end);

        if (tables.isEmpty()) {
            return String.format("Rất tiếc, hiện không còn bàn trống %s từ %s đến %s ạ.",
                    (params.getFloor() != null ? "tầng " + params.getFloor() : ""),
                    ChatTimeUtils.formatTimeOnly(start), ChatTimeUtils.formatTimeOnly(end));
        }

        StringBuilder sb = new StringBuilder();
        sb.append("Dạ, từ ").append(ChatTimeUtils.formatTimeOnly(start))
          .append(" đến ").append(ChatTimeUtils.formatTimeOnly(end));
        if (params.getFloor() != null) {
            sb.append(" tầng ").append(params.getFloor());
        }
        sb.append(" còn các bàn sau trống:\n");

        for (int i = 0; i < Math.min(tables.size(), 5); i++) {
            TableAvailabilityDTO t = tables.get(i);
            sb.append("- ").append(t.getTableName()).append(" (Tầng ").append(t.getFloor())
              .append(", ").append(t.getSlot()).append(" chỗ)\n");
        }
        if (tables.size() > 5) {
            sb.append("... và ").append(tables.size() - 5).append(" bàn khác.");
        }
        return sb.toString();
    }

    private String handleBookTable(String message, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "Dạ, để đặt bàn qua chat, bạn vui lòng đăng nhập vào tài khoản nhân viên ạ. "
                    + "Khách hàng vui lòng liên hệ hotline hoặc dùng chức năng đặt bàn trên web.";
        }

        BookingParser.BookingParseResult result = BookingParser.parse(message);
        if (result == null || !result.hasRequiredInfo()) {
            return "Bạn muốn đặt bàn cho mấy người và vào thời gian nào ạ? "
                    + "Vui lòng cung cấp chi tiết (VD: Đặt bàn 4 người tối nay lúc 19h cho anh A).";
        }

        return String.format("Chức năng đặt bàn tự động đang được hoàn thiện. "
                        + "Hệ thống ghi nhận yêu cầu: Đặt bàn cho %s (%s), %d người, %s lúc %s.",
                result.getCustomerName() != null ? result.getCustomerName() : "Khách",
                result.getPhoneNumber() != null ? result.getPhoneNumber() : "Không có SĐT",
                result.getGuestCount() != null ? result.getGuestCount() : 2,
                result.getPreferredFloor() != null ? "tầng " + result.getPreferredFloor() : "tầng trệt",
                ChatTimeUtils.format(result.getExpectedArriveTime()));
    }

    private boolean hasRole(Authentication auth, String... roles) {
        if (auth == null) return false;
        for (String role : roles) {
            for (GrantedAuthority authority : auth.getAuthorities()) {
                if (authority.getAuthority().equals(role) || authority.getAuthority().equals("ROLE_" + role)) {
                    return true;
                }
            }
        }
        return false;
    }

    private String handleSalesReport(String message, Authentication auth) {
        if (!hasRole(auth, "ADMIN", "QL")) {
            return "Xin lỗi, chỉ có Quản lý hoặc Admin mới có quyền xem báo cáo doanh thu.";
        }

        Instant start = ChatTimeUtils.startOfToday();
        Instant end = ChatTimeUtils.endOfToday();
        SalesSummaryDTO summary = nativeSqlChatRepository.getSalesSummary(start, end);

        return String.format("Doanh thu hôm nay (từ 00:00): %,.0f VNĐ với %d hóa đơn đã thanh toán.",
                summary.getTotalRevenue(), summary.getInvoiceCount());
    }

    private String handleInventoryCheck(String message, Authentication auth) {
        if (!hasRole(auth, "ADMIN", "QL", "PC")) {
            return "Xin lỗi, bạn không có quyền kiểm tra tồn kho.";
        }
        List<IngredientStockDTO> lowStock = nativeSqlChatRepository.getLowStockIngredients();
        if (lowStock.isEmpty()) {
            return "Hiện tại kho đã đủ nguyên liệu, không có nguyên liệu nào sắp hết hay sắp hết hạn.";
        }

        StringBuilder sb = new StringBuilder("Cảnh báo tồn kho:\n");
        for (IngredientStockDTO dto : lowStock) {
            sb.append("- ").append(dto.getIngredientName()).append(": ");
            if (dto.getIsNearExpiry()) {
                sb.append("Sắp hết hạn (").append(ChatTimeUtils.format(dto.getNearestExpirationAt())).append("). ");
            } else {
                sb.append("Sắp hết (Còn ").append(dto.getTotalQuantity()).append(" ").append(dto.getUnitName()).append(").");
            }
            sb.append("\n");
        }
        return sb.toString();
    }

    private String handleCreatePurchaseOrder(String message, Authentication auth) {
        if (!hasRole(auth, "ADMIN", "QL")) {
            return "Xin lỗi, chỉ có Quản lý hoặc Admin mới có quyền tạo đơn nhập hàng.";
        }
        return "Tính năng tạo đơn nhập hàng bằng AI đang trong giai đoạn phát triển.";
    }

    private String hardFallbackReply(IntentType intent) {
        return switch (intent) {
            case TABLE_AVAILABILITY -> "Hiện tại hệ thống kiểm tra bàn đang bảo trì, bạn vui lòng liên hệ hotline nhé.";
            case BOOK_TABLE -> "Bạn có thể sử dụng chức năng đặt bàn trực tiếp trên ứng dụng ạ.";
            case MENU_QUERY -> "Bạn có thể xem menu đầy đủ tại trang chủ ứng dụng nhé.";
            default -> "Xin lỗi, hệ thống AI hiện đang bận hoặc tôi chưa hiểu ý bạn. Vui lòng thử lại sau hoặc diễn đạt cách khác.";
        };
    }

    private boolean isQuotaExceeded(Throwable t) {
        while (t != null) {
            String msg = t.getMessage() != null ? t.getMessage().toLowerCase() : "";
            if (msg.contains("429") || msg.contains("quota") || msg.contains("too many requests")) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}
