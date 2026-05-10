Dưới đây là bản thiết kế hoàn chỉnh cho module **AI Chatbot** tích hợp vào hệ thống `cf-manager`, đã khắc phục tất cả các điểm còn tồn tại từ phân tích trước. Thiết kế bám sát kiến trúc hiện tại, sử dụng Spring AI (Gemini), native query, Redis cache, và WebSocket.

---

## Mục lục
1. [Cấu trúc thư mục](#cấu-trúc-thư-mục)
2. [Cấu hình & Dependency](#cấu-hình--dependency)
3. [Entity & Database Migration](#entity--database-migration)
4. [Repository & Native Query](#repository--native-query)
5. [Service Layer](#service-layer)
6. [Controller & WebSocket](#controller--websocket)
7. [Intent Detection & Prompt Engineering](#intent-detection--prompt-engineering)
8. [Fallback & Cache Strategy](#fallback--cache-strategy)
9. [Xử lý múi giờ](#xử-lý-múi-giờ)
10. [Phân quyền & Bảo mật](#phân-quyền--bảo-mật)
11. [Triển khai chi tiết theo giai đoạn](#triển-khai-chi-tiết-theo-giai-đoạn)

---

## 1. Cấu trúc thư mục
```
com.duyminhdev.cf_manager
├── config
│   └── AiConfig.java
├── controller
│   └── AiChatController.java
├── dto
│   ├── request
│   │   └── chat
│   │       ├── ChatRequest.java
│   │       └── ChatFeedbackRequest.java
│   └── response
│       └── chat
│           ├── ChatResponse.java
│           └── ChatIntentType.java
├── entity
│   ├── ConversationMessage.java
│   └── UserPreference.java
├── repository
│   ├── ConversationMessageRepository.java
│   ├── UserPreferenceRepository.java
│   ├── native_interface
│   │   ├── NativeSqlChatRepository.java
│   │   └── impl
│   │       └── NativeSqlChatRepositoryImpl.java
│   └── ...
├── service
│   ├── AiChatService.java
│   └── impl
│       └── AiChatServiceImpl.java
└── websocket
    └── AiChatWebSocketHandler.java (tuỳ chọn, tận dụng kênh hiện có)
```

---

## 2. Cấu hình & Dependency

### 2.1. `pom.xml` bổ sung
```xml
<dependencyManagement>
    <dependencies>
        <dependency>
            <groupId>org.springframework.ai</groupId>
            <artifactId>spring-ai-bom</artifactId>
            <version>1.2.0-M1</version> <!-- Bản milestone tương thích Boot 3.5.x -->
            <type>pom</type>
            <scope>import</scope>
        </dependency>
    </dependencies>
</dependencyManagement>

<dependencies>
    <dependency>
        <groupId>org.springframework.ai</groupId>
        <artifactId>spring-ai-starter-model-google-genai</artifactId>
    </dependency>
    <!-- Các dependency hiện có giữ nguyên -->
</dependencies>
```

### 2.2. `application.yml` bổ sung
```yaml
spring:
  ai:
    google:
      genai:
        api-key: ${GEMINI_API_KEY}
        chat:
          options:
            model: gemini-2.5-flash
            temperature: 0.7
            max-output-tokens: 500

chat:
  ai:
    enabled: true
    quota-backoff-ms: 300000
  cache:
    table-availability-ttl-seconds: 30  # Cache bàn trống
  timezone: Asia/Ho_Chi_Minh             # Múi giờ quán
```

### 2.3. `AiConfig.java`
```java
@Configuration
public class AiConfig {
    @Value("${chat.ai.enabled:true}")
    private boolean aiEnabled;

    @Bean
    public ChatClient chatClient(ChatClient.Builder builder) {
        // Có thể thêm customizer nếu cần
        return builder.build();
    }
}
```

---

## 3. Entity & Database Migration

### 3.1. `conversation_messages` (đã có script SQL)
```java
@Entity
@Table(name = "conversation_messages")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConversationMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 255)
    private String userId;   // "guest_<sessionId>" hoặc username nhân viên

    @Column(nullable = false, length = 255)
    private String sessionId;

    @Column(nullable = false, length = 20)
    private String role;     // "user" hoặc "assistant"

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @CreationTimestamp
    private Instant createdAt;
}
```

### 3.2. `user_preferences`
```java
@Entity
@Table(name = "user_preferences")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UserPreference {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 255)
    private String userId;          // username hoặc "guest_<sessionId>" tạm thời

    private String preferredDishType;
    private Integer preferredTableFloor;
    private String preferredPriceRange; // "LOW", "MEDIUM", "HIGH"
    private Integer totalBookings;
    private Long lastBookedTableId;
    private String lastBookedRoomType;  // ánh xạ từ mẫu hotel
    @Column(columnDefinition = "TEXT")
    private String notes;

    @UpdateTimestamp
    private Instant updatedAt;
}
```

---

## 4. Repository & Native Query

### 4.1. `ConversationMessageRepository`
```java
public interface ConversationMessageRepository extends JpaRepository<ConversationMessage, Long> {
    List<ConversationMessage> findTop10ByUserIdAndSessionIdOrderByCreatedAtAsc(String userId, String sessionId);
}
```

### 4.2. `UserPreferenceRepository`
```java
public interface UserPreferenceRepository extends JpaRepository<UserPreference, Long> {
    Optional<UserPreference> findByUserId(String userId);
}
```

### 4.3. Native Query cho Chatbot (tối ưu)
Vì chatbot cần các truy vấn phức tạp như kiểm tra bàn trống theo thời gian thực, chúng ta tạo `NativeSqlChatRepository`:

```java
public interface NativeSqlChatRepository {
    List<TableAvailabilityDTO> findAvailableTables(Integer floor, Instant start, Instant end);
    SalesSummaryDTO getSalesSummary(Instant from, Instant to);
    // ...
}
```

Implementation sử dụng EntityManager và native query tương tự các repository khác (tham khảo `PurchaseOrderRepositoryImpl`). Chúng ta sẽ query trên `dining_table` và `table_booking` với điều kiện thời gian.

---

## 5. Service Layer

### 5.1. `AiChatService` (interface)
```java
public interface AiChatService {
    ChatResponse processChat(ChatRequest request, Authentication authentication);
}
```

### 5.2. `AiChatServiceImpl`
Triển khai chính, kết hợp rule-based + AI. Dưới đây là các thành phần quan trọng:

```java
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AiChatServiceImpl implements AiChatService {
    private final ChatClient chatClient;
    private final ConversationMessageRepository conversationRepo;
    private final UserPreferenceRepository preferenceRepo;
    private final TableBookingService tableBookingService;
    private final DishService dishService;
    private final InvoiceService invoiceService;
    private final NativeSqlChatRepository nativeSqlChatRepository;
    private final ServiceSupport serviceSupport;
    private final RedisTemplate<String, Object> redisTemplate; // Cache bàn trống

    @Value("${chat.ai.quota-backoff-ms:300000}")
    private long quotaBackoffMs;
    private volatile long aiQuotaBackoffUntilMs = 0;

    @Override
    @Transactional
    public ChatResponse processChat(ChatRequest request, Authentication authentication) {
        String message = request.getMessage().trim();
        String userId = resolveUserId(authentication, request.getUserId());
        String sessionId = request.getSessionId() != null ? request.getSessionId() : UUID.randomUUID().toString();

        // Lưu user message
        saveMessage(userId, sessionId, "user", message);

        // Intent detection (rule-based trước)
        IntentType intent = detectIntent(message);
        String reply;

        switch (intent) {
            case TABLE_AVAILABILITY -> reply = handleTableAvailability(message, userId);
            case BOOK_TABLE -> reply = handleBookTable(message, userId, authentication);
            case MENU_QUERY -> reply = handleMenuQuery(message, userId);
            case SALES_REPORT -> reply = handleSalesReport(message, authentication);
            case INVENTORY_CHECK -> reply = handleInventoryCheck(message);
            case CREATE_PURCHASE_ORDER -> reply = handleCreatePurchaseOrder(message, authentication);
            default -> reply = callAIWithContext(message, userId, sessionId);
        }

        // Nếu fallback từ AI (lỗi) và chưa có câu trả lời, dùng rule-based fallback
        if (reply == null || reply.isBlank()) {
            reply = fallbackReply(intent);
        }

        saveMessage(userId, sessionId, "assistant", reply);
        updateUserPreferences(userId, message, reply);
        return new ChatResponse(reply);
    }

    private String resolveUserId(Authentication auth, String clientUserId) {
        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getPrincipal())) {
            return auth.getName(); // username của account
        }
        return "guest_" + (clientUserId != null ? clientUserId : UUID.randomUUID().toString());
    }

    // ================= INTENT HANDLERS =================
    private String handleTableAvailability(String message, String userId) {
        // Trích xuất tầng, thời gian từ message
        TableQueryParams params = TableQueryParser.parse(message);
        if (params == null || params.getStart() == null || params.getEnd() == null) {
            return "Xin lỗi, tôi chưa hiểu thời gian bạn muốn kiểm tra. Hãy nói rõ ngày giờ.";
        }
        Instant start = convertToInstant(params.getStart(), params.getDate());
        Instant end = convertToInstant(params.getEnd(), params.getDate());

        // Cache key
        String cacheKey = "avail:" + params.getFloor() + ":" + start + ":" + end;
        List<TableAvailabilityDTO> cached = (List<TableAvailabilityDTO>) redisTemplate.opsForValue().get(cacheKey);
        if (cached != null) {
            return formatAvailabilityResponse(cached, start, end);
        }

        List<TableAvailabilityDTO> tables = nativeSqlChatRepository.findAvailableTables(params.getFloor(), start, end);
        redisTemplate.opsForValue().set(cacheKey, tables, Duration.ofSeconds(30));

        if (tables.isEmpty()) {
            return String.format("Rất tiếc, không còn bàn trống tầng %d từ %s đến %s.",
                    params.getFloor(), formatInstant(start), formatInstant(end));
        }
        return formatAvailabilityResponse(tables, start, end);
    }

    private String handleBookTable(String message, String userId, Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || "anonymousUser".equals(auth.getPrincipal())) {
            return "Để đặt bàn, bạn cần đăng nhập. Vui lòng đăng nhập và thử lại.";
        }
        // Phân tích thông tin đặt bàn
        BookingParseResult result = BookingParser.parse(message);
        if (result == null) {
            return "Tôi chưa đủ thông tin để đặt bàn. Bạn có thể cung cấp ngày, giờ, số người, tầng yêu thích.";
        }
        // Gọi service đặt bàn hiện có (sử dụng lock)
        try {
            TableBookingCreateRequestDTO req = new TableBookingCreateRequestDTO();
            // ... set các trường từ result
            TableBookingResponseDTO booking = tableBookingService.create(req);
            // Xoá cache bàn trống liên quan
            evictAvailabilityCache(booking.getTable().getFloor());
            return "Đặt bàn thành công! Mã đặt bàn của bạn là: " + booking.getBookingInvoiceCode();
        } catch (InvalidDataException e) {
            return "Không thể đặt bàn: " + e.getMessage();
        }
    }

    // ... các handler khác (menuQuery, salesReport, inventoryCheck, createPurchaseOrder)

    private String callAIWithContext(String message, String userId, String sessionId) {
        if (!aiEnabled || System.currentTimeMillis() < aiQuotaBackoffUntilMs) {
            return null; // fallback sau
        }

        // Xây dựng prompt với context từ user preferences, lịch sử chat, dữ liệu quán
        String prompt = buildPrompt(message, userId, sessionId);
        try {
            String aiReply = chatClient.prompt().user(prompt).call().content();
            return aiReply;
        } catch (Exception e) {
            if (isQuotaExceeded(e)) {
                aiQuotaBackoffUntilMs = System.currentTimeMillis() + quotaBackoffMs;
            }
            log.warn("AI call failed", e);
            return null;
        }
    }

    private String buildPrompt(String message, String userId, String sessionId) {
        UserPreference pref = preferenceRepo.findByUserId(userId).orElse(null);
        List<ConversationMessage> history = conversationRepo.findTop10ByUserIdAndSessionIdOrderByCreatedAtAsc(userId, sessionId);

        StringBuilder prompt = new StringBuilder();
        prompt.append("Bạn là trợ lý quán cà phê thông minh, trả lời bằng tiếng Việt, thân thiện.\n");
        // Thêm thông tin quán (từ config)
        prompt.append("Quán mở cửa 7h-22h, địa chỉ 123 Nguyễn Văn Linh...\n");
        // Thêm user preferences
        if (pref != null) {
            prompt.append("Khách hàng thích: ").append(pref.getPreferredDishType()).append("\n");
        }
        // Lịch sử chat
        if (!history.isEmpty()) {
            prompt.append("Lịch sử hội thoại gần đây:\n");
            history.forEach(msg -> prompt.append(msg.getRole()).append(": ").append(msg.getContent()).append("\n"));
        }
        prompt.append("Câu hỏi hiện tại: ").append(message);
        return prompt.toString();
    }

    private boolean isQuotaExceeded(Throwable t) {
        while (t != null) {
            if (t.getMessage() != null && (t.getMessage().contains("429") || t.getMessage().contains("quota"))) {
                return true;
            }
            t = t.getCause();
        }
        return false;
    }
}
```

### 5.3. Các helper class (Parser, Cache)
- `TableQueryParser`: dùng regex + `DateTimeFormatter` để trích xuất tầng, ngày, giờ từ tin nhắn.
- `BookingParser`: tương tự, nhưng cho đặt bàn.
- `CacheManager`: xoá cache khi có thay đổi đặt bàn.

---

## 6. Controller & WebSocket

### 6.1. `AiChatController`
```java
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class AiChatController {
    private final AiChatService aiChatService;

    @PostMapping("/public")
    public ResponseEntity<ChatResponse> publicChat(@Valid @RequestBody ChatRequest request) {
        ChatResponse response = aiChatService.processChat(request, null);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/staff")
    @PreAuthorize("hasAnyRole('ADMIN','QL','PC','PV')")
    public ResponseEntity<ChatResponse> staffChat(@Valid @RequestBody ChatRequest request,
                                                 Authentication authentication) {
        ChatResponse response = aiChatService.processChat(request, authentication);
        return ResponseEntity.ok(response);
    }
}
```

### 6.2. WebSocket (tuỳ chọn)
Sử dụng kênh WebSocket hiện có của module notification, khi AI có phản hồi, publish một message đến `/topic/chat/{userId}`. Client có thể subscribe để nhận real-time.

---

## 7. Intent Detection & Prompt Engineering
- **Rule-based**: sử dụng từ khoá regex cho các intent chính (đặt bàn, trống bàn, thực đơn, doanh thu…).
- **AI fallback**: các câu không xác định được sẽ đưa vào Gemini với prompt hệ thống ràng buộc chỉ trả lời dựa trên dữ liệu cung cấp, không suy diễn ngoài. Prompt bao gồm dữ liệu quán, sở thích user, lịch sử chat và nếu có thể, kết quả truy vấn thời gian thực (bàn trống, doanh thu) để AI diễn đạt lại.

---

## 8. Fallback & Cache Strategy
- **Fallback 2 lớp**:
  - Lớp 1: Xử lý bằng rule-based (trả về dữ liệu thô như danh sách bàn trống).
  - Lớp 2: Thông báo lỗi chung "Hệ thống đang quá tải".
- **Cache**:
  - Sử dụng Redis lưu kết quả kiểm tra bàn trống với TTL 30 giây.
  - Tự động xoá cache khi có thay đổi đặt bàn (sử dụng `BookingDomainEventPublisher` hoặc AOP).

---

## 9. Xử lý múi giờ
- Tất cả `Instant` trong DB là UTC.
- Parser sẽ parse chuỗi ngày giờ từ user theo `Asia/Ho_Chi_Minh`, sau đó chuyển sang UTC bằng `ZonedDateTime.toInstant()`.
- Khi hiển thị giờ cho user, dùng `DateTimeFormatter` với `ZoneId` của quán.

---

## 10. Phân quyền & Bảo mật
- Endpoint `/public`: không yêu cầu auth, nhưng giới hạn chức năng (không được đặt bàn, không xem doanh thu). UserId sẽ là `guest_<sessionId>`.
- Endpoint `/staff`: yêu cầu JWT, các role tương ứng (ADMIN xem doanh thu, nhân viên phục vụ có thể hỏi bàn trống…).
