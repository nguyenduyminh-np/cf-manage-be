# Tài liệu triển khai FE — Module AI Chatbot (Angular 21)

## 1. Tổng quan module

Module AI Chatbot cung cấp giao diện trò chuyện với trợ lý AI quán cà phê, dành cho nhân viên đã đăng nhập. Chatbot hỗ trợ:

- Xem menu, kiểm tra bàn trống, hỏi thông tin quán (FAQ).
- Đặt bàn qua chat (2 bước xác nhận).
- Xem doanh thu, tồn kho, KPI dashboard.
- Xem booking sắp đến, đơn đang chế biến.

Lưu ý: Module này yêu cầu đăng nhập (JWT). Không có endpoint public.

---

## 2. API sử dụng

### 2.1. Endpoint duy nhất

- Method: POST
- URL: `/api/v1/chat/staff`
- Auth: Bắt buộc Bearer Token (JWT)
- Role yêu cầu: ADMIN
- Content-Type: application/json

---

## 3. Request DTO

Backend class: `ChatRequest`

```json
{
  "message": "Quán mở cửa mấy giờ?",
  "sessionId": "550e8400-e29b-41d4-a716-446655440000"
}
```

Ý nghĩa từng field:

| Field | Kiểu | Bắt buộc | Mô tả |
|-------|------|----------|-------|
| message | string | ✅ Có | Nội dung tin nhắn. Không được rỗng, tối đa 1000 ký tự. |
| sessionId | string | Không | UUID phiên chat. FE sinh 1 lần khi mở chat, giữ nguyên suốt cuộc hội thoại. Nếu null, server tự sinh và trả về trong response. |

Lưu ý: field `userId` trong DTO vẫn tồn tại nhưng bị bỏ qua ở endpoint `/staff` (server lấy userId từ JWT). FE không cần gửi.

Validation backend:

- message rỗng hoặc null → 400 Bad Request: "Tin nhắn không được để trống".
- message dài hơn 1000 ký tự → 400 Bad Request: "Tin nhắn không được vượt quá 1000 ký tự".

---

## 4. Response DTO

Backend class: `ChatResponse` (được bọc trong `ApiResponse<ChatResponse>`)

### 4.1. Response wrapper chuẩn hệ thống

```json
{
  "status": 200,
  "message": "SUCCESS",
  "data": {
    "reply": "Dạ, quán mở cửa từ 7:00 đến 22:00 hàng ngày ạ.",
    "sessionId": "550e8400-e29b-41d4-a716-446655440000",
    "timestamp": "2026-05-08T15:30:00Z",
    "intentType": null,
    "suggestedActions": null,
    "structuredData": null
  }
}
```

### 4.2. Chi tiết field trong `data`

| Field | Kiểu | Nullable | Mô tả |
|-------|------|----------|-------|
| reply | string | Không | Câu trả lời text từ AI. Luôn có giá trị. Có thể chứa markdown (danh sách gạch đầu dòng, in đậm). |
| sessionId | string | Không | ID phiên chat. FE phải lưu lại và gửi kèm mọi request tiếp theo trong cùng cuộc hội thoại. |
| timestamp | string (ISO-8601 UTC) | Không | Thời điểm server tạo response. FE dùng để hiển thị giờ gửi tin. |
| intentType | string | Có | Loại intent AI phát hiện. Nullable. Dùng cho debug hoặc analytics. |
| suggestedActions | array | Có | Danh sách nút gợi ý. Nullable. Chi tiết xem mục 4.3. |
| structuredData | object | Có | Dữ liệu có cấu trúc cho FE render đặc biệt. Nullable. Chi tiết xem mục 4.4. |

### 4.3. SuggestedAction — Nút gợi ý

Khi field `suggestedActions` không null, FE render thành các nút quick-reply bên dưới tin nhắn bot.

```json
{
  "suggestedActions": [
    { "label": "Xem menu", "action": "CHAT:Cho tôi xem menu" },
    { "label": "Đặt bàn", "action": "CHAT:Tôi muốn đặt bàn" },
    { "label": "Trang đặt bàn", "action": "NAVIGATE:/booking" }
  ]
}
```

Ý nghĩa field:

| Field | Kiểu | Mô tả |
|-------|------|-------|
| label | string | Text hiển thị trên nút |
| action | string | Hành động khi bấm. Format: `"PREFIX:payload"` |

Xử lý action theo prefix:

| Prefix | Hành động FE |
|--------|-------------|
| CHAT: | Gửi `payload` làm tin nhắn chat mới (tự động gọi API). VD: `"CHAT:Cho tôi xem menu"` → FE gửi request với `message = "Cho tôi xem menu"`. |
| NAVIGATE: | Chuyển trang Angular Router. VD: `"NAVIGATE:/booking"` → `router.navigate(['/booking'])`. |

### 4.4. StructuredData — Dữ liệu có cấu trúc

Field này dùng cho các trường hợp AI trả về dữ liệu dạng bảng, list, KPI mà FE muốn render UI đặc biệt thay vì chỉ hiển thị text.

Hiện tại field này chưa được sử dụng (luôn null). FE chỉ cần render `reply` dạng text. Trong tương lai khi BE bổ sung, FE có thể dựa vào `intentType` để quyết định cách render `structuredData`.

---

## 5. Ví dụ request/response theo kịch bản

### 5.1. Hỏi FAQ

Request:
```json
POST /api/v1/chat/staff
Headers: Authorization: Bearer <jwt_token>
{
  "message": "Quán có wifi không? Pass wifi là gì?",
  "sessionId": "sess-001"
}
```

Response:
```json
{
  "status": 200,
  "message": "SUCCESS",
  "data": {
    "reply": "Dạ, quán có wifi miễn phí ạ. Mật khẩu wifi bạn hỏi nhân viên tại quầy nhé.",
    "sessionId": "sess-001",
    "timestamp": "2026-05-08T15:30:00Z",
    "intentType": null,
    "suggestedActions": null,
    "structuredData": null
  }
}
```

### 5.2. Xem menu

Request:
```json
POST /api/v1/chat/staff
Headers: Authorization: Bearer <jwt_token>
{
  "message": "Cho tôi xem menu đồ uống",
  "sessionId": "sess-001"
}
```

Response:
```json
{
  "status": 200,
  "message": "SUCCESS",
  "data": {
    "reply": "Dạ, menu quán hiện có:\n- Cà phê sữa đá: 35,000 VNĐ\n- Bạc xỉu: 39,000 VNĐ\n- Trà đào cam sả: 45,000 VNĐ\n- Sinh tố bơ: 55,000 VNĐ\n...",
    "sessionId": "sess-001",
    "timestamp": "2026-05-08T15:31:00Z",
    "intentType": null,
    "suggestedActions": null,
    "structuredData": null
  }
}
```

### 5.3. Kiểm tra bàn trống

Request:
```json
POST /api/v1/chat/staff
Headers: Authorization: Bearer <jwt_token>
{
  "message": "Tối nay 19h còn bàn trống tầng 2 không?",
  "sessionId": "sess-001"
}
```

Response:
```json
{
  "status": 200,
  "message": "SUCCESS",
  "data": {
    "reply": "Dạ, từ 19:00 đến 21:00 tầng 2 còn các bàn sau trống:\n- Bàn 05 (Tầng 2, 4 chỗ)\n- Bàn 07 (Tầng 2, 6 chỗ)\n- Bàn 08 (Tầng 2, 8 chỗ)",
    "sessionId": "sess-001",
    "timestamp": "2026-05-08T15:32:00Z",
    "intentType": null,
    "suggestedActions": null,
    "structuredData": null
  }
}
```

### 5.4. Đặt bàn qua chat (2 bước xác nhận)

Bước 1 — Yêu cầu đặt bàn:

Request:
```json
POST /api/v1/chat/staff
Headers: Authorization: Bearer <jwt_token>
{
  "message": "Đặt bàn 4 người tối nay 19h tầng 2 cho anh Minh, SĐT 0901234567",
  "sessionId": "sess-002"
}
```

Response:
```json
{
  "status": 200,
  "message": "SUCCESS",
  "data": {
    "reply": "Dạ, tôi tìm được bàn phù hợp:\n- Bàn 05 (Tầng 2, 4 chỗ)\n- Thời gian: 19:00 - 21:00 tối nay\n- Khách: Anh Minh - 0901234567\n\nBạn có muốn xác nhận đặt bàn không ạ?",
    "sessionId": "sess-002",
    "timestamp": "2026-05-08T15:33:00Z",
    "intentType": null,
    "suggestedActions": null,
    "structuredData": null
  }
}
```

Bước 2 — Xác nhận (phải cùng sessionId):

Request:
```json
POST /api/v1/chat/staff
Headers: Authorization: Bearer <jwt_token>
{
  "message": "Xác nhận đặt bàn",
  "sessionId": "sess-002"
}
```

Response:
```json
{
  "status": 200,
  "message": "SUCCESS",
  "data": {
    "reply": "Đã đặt bàn thành công! Mã booking: BK-2026050801. Bàn 05, tầng 2, 19:00 - 21:00 tối nay.",
    "sessionId": "sess-002",
    "timestamp": "2026-05-08T15:33:30Z",
    "intentType": null,
    "suggestedActions": null,
    "structuredData": null
  }
}
```

### 5.5. Xem doanh thu

Request:
```json
POST /api/v1/chat/staff
Headers: Authorization: Bearer <jwt_token>
{
  "message": "Doanh thu hôm nay bao nhiêu?",
  "sessionId": "sess-003"
}
```

Response:
```json
{
  "status": 200,
  "message": "SUCCESS",
  "data": {
    "reply": "Doanh thu hôm nay: 12,500,000 VNĐ với 45 hóa đơn đã thanh toán.",
    "sessionId": "sess-003",
    "timestamp": "2026-05-08T15:35:00Z",
    "intentType": null,
    "suggestedActions": null,
    "structuredData": null
  }
}
```

### 5.6. Session mới (không truyền sessionId)

Request:
```json
POST /api/v1/chat/staff
Headers: Authorization: Bearer <jwt_token>
{
  "message": "Xin chào"
}
```

Response (server tự sinh sessionId):
```json
{
  "status": 200,
  "message": "SUCCESS",
  "data": {
    "reply": "Xin chào! Tôi là trợ lý AI của quán CF Manager Coffee. Tôi có thể giúp bạn xem menu, kiểm tra bàn trống, đặt bàn, xem doanh thu và nhiều thứ khác. Bạn cần gì ạ?",
    "sessionId": "a1b2c3d4-e5f6-7890-abcd-ef1234567890",
    "timestamp": "2026-05-08T15:36:00Z",
    "intentType": null,
    "suggestedActions": null,
    "structuredData": null
  }
}
```

FE phải lưu `sessionId` từ response này và gửi kèm tất cả request tiếp theo.

---

## 6. Quy trình triển khai FE đề xuất

### 6.1. Quản lý session

1. Khi mở chat lần đầu (hoặc bắt đầu cuộc trò chuyện mới): sinh UUID v4 bằng `crypto.randomUUID()` làm `sessionId`.
2. Lưu `sessionId` vào state của component (hoặc signal). Không cần persist vào localStorage.
3. Gửi `sessionId` kèm mọi request trong cuộc hội thoại.
4. Khi user bấm "Cuộc trò chuyện mới": sinh UUID mới, xóa lịch sử chat trên UI.

### 6.2. Xử lý gửi tin nhắn

1. User nhập text vào input, bấm Enter hoặc nút Gửi.
2. Validate: không gửi nếu rỗng, không vượt 1000 ký tự.
3. Thêm tin nhắn user vào danh sách hiển thị ngay lập tức (optimistic UI).
4. Hiển thị trạng thái "đang trả lời..." (loading indicator).
5. Gọi API `POST /api/v1/chat/staff` kèm JWT.
6. Khi nhận response: thêm tin nhắn bot vào danh sách, ẩn loading.
7. Scroll xuống tin nhắn mới nhất.

### 6.3. Xử lý SuggestedAction buttons

1. Khi response có `suggestedActions` không null: render danh sách nút bên dưới tin nhắn bot.
2. Khi user bấm nút:
   - Nếu action bắt đầu bằng `CHAT:` → lấy payload sau prefix, gửi làm message mới (lặp lại bước 6.2).
   - Nếu action bắt đầu bằng `NAVIGATE:` → gọi `router.navigate()` với path payload.

### 6.4. Xử lý lỗi

| HTTP Status | Xử lý FE |
|-------------|----------|
| 200 | Thành công. Hiển thị `data.reply`. |
| 400 | Lỗi validation. Hiển thị message lỗi từ response. |
| 401 | JWT hết hạn. Redirect về trang login hoặc dùng refresh token. |
| 403 | Không có quyền. Hiển thị thông báo "Bạn không có quyền sử dụng chức năng này". |
| 500 | Lỗi server. Hiển thị tin nhắn bot: "Hệ thống đang gặp sự cố, vui lòng thử lại sau." |
| Timeout / Network Error | Hiển thị tin nhắn bot: "Không thể kết nối server, vui lòng kiểm tra mạng." |

---

## 7. Gợi ý TypeScript interfaces

```typescript
// === Request ===
export interface ChatRequest {
  message: string;
  sessionId?: string;
}

// === Response (unwrapped from ApiResponse) ===
export interface ChatResponse {
  reply: string;
  sessionId: string;
  timestamp: string;           // ISO-8601 UTC
  intentType?: string | null;
  suggestedActions?: SuggestedAction[] | null;
  structuredData?: unknown | null;
}

export interface SuggestedAction {
  label: string;
  action: string;              // "CHAT:..." hoặc "NAVIGATE:..."
}

// === ApiResponse wrapper (chuẩn hệ thống) ===
export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}
```

---

## 8. Gợi ý Angular service

```typescript
@Injectable({ providedIn: 'root' })
export class ChatService {
  private readonly http = inject(HttpClient);
  private readonly API_URL = '/api/v1/chat/staff';

  /**
   * Gửi tin nhắn chat. Luôn gọi endpoint staff (yêu cầu JWT).
   * HttpInterceptor sẽ tự gắn Authorization header.
   */
  sendMessage(request: ChatRequest): Observable<ApiResponse<ChatResponse>> {
    return this.http.post<ApiResponse<ChatResponse>>(this.API_URL, request);
  }
}
```

---

## 9. Gợi ý cấu trúc UI chat

```
┌─────────────────────────────────┐
│  🤖 Trợ lý CF Manager          │ ← Header (tên bot + nút đóng)
├─────────────────────────────────┤
│                                 │
│  [Bot] Xin chào! Tôi có thể    │ ← Tin nhắn bot (căn trái, background khác)
│  giúp gì cho bạn?              │
│                                 │
│            [User] Cho xem menu  │ ← Tin nhắn user (căn phải)
│                                 │
│  [Bot] Dạ, menu quán hiện có:  │
│  - Cà phê sữa đá: 35,000 VNĐ  │
│  - Bạc xỉu: 39,000 VNĐ        │
│  ...                            │
│                                 │
│  [Đặt bàn] [Xem bàn trống]     │ ← Suggested action buttons
│                                 │
│  ●●● đang trả lời...           │ ← Loading indicator (khi chờ API)
│                                 │
├─────────────────────────────────┤
│  [________________] [Gửi]      │ ← Input + nút gửi
└─────────────────────────────────┘
```

Lưu ý thiết kế:

- Chat panel nên là floating panel góc phải dưới (giống Messenger/Tawk.to).
- Có nút toggle mở/đóng chat.
- Tin nhắn bot có thể chứa markdown: danh sách gạch đầu dòng, in đậm. FE nên render markdown hoặc ít nhất xử lý newline `\n` thành line break.
- Scroll tự động xuống cuối khi có tin nhắn mới.

---

## 10. Danh sách khả năng chatbot

Tất cả các khả năng bên dưới đều yêu cầu đăng nhập với role ADMIN.

| Khả năng | Ví dụ câu hỏi |
|----------|---------------|
| Xem thực đơn/menu | "Cho xem menu", "Có món gì?", "Giá cà phê bao nhiêu?" |
| Kiểm tra bàn trống | "Tối nay 19h còn bàn trống không?", "Bàn tầng 2 trống không?" |
| Top món bán chạy | "Món nào bán chạy nhất?", "Gợi ý đồ uống" |
| Thông tin quán (FAQ) | "Mở cửa mấy giờ?", "Địa chỉ ở đâu?", "Có wifi không?" |
| Đặt bàn qua chat (2 bước) | "Đặt bàn 4 người tối nay 19h tầng 2" → xác nhận |
| Booking sắp đến | "Booking nào sắp tới?", "Khách nào sắp đến?" |
| Đơn đang chế biến | "Bếp đang nấu gì?", "Đơn nào đang làm?" |
| Xem doanh thu | "Doanh thu hôm nay?", "Bán được bao nhiêu tuần này?" |
| Tổng quan KPI | "Tình hình hôm nay thế nào?", "Tổng quan dashboard" |
| Kiểm tra tồn kho | "Nguyên liệu sắp hết?", "Tồn kho thế nào?" |
| Cảnh báo kho | "Có cảnh báo kho gì không?" |

---

## 11. Curl mẫu

```bash
curl --location 'http://localhost:8080/api/v1/chat/staff' \
--header 'Authorization: Bearer <access_token>' \
--header 'Content-Type: application/json' \
--data '{
  "message": "Cho tôi xem menu",
  "sessionId": "test-session-001"
}'
```

---

## 12. Lưu ý quan trọng

1. Thời gian response có thể lâu (2-10 giây) vì server gọi Gemini AI. FE cần hiển thị loading indicator rõ ràng.
2. Khi server AI bị quá tải (quota exceeded), response vẫn trả 200 OK nhưng `reply` sẽ là câu trả lời fallback đơn giản hơn. FE không cần xử lý đặc biệt.
3. Không gọi API liên tục (debounce): đợi response trước rồi mới cho phép gửi tin nhắn tiếp.
4. `reply` có thể chứa ký tự xuống dòng `\n`. FE phải render thành `<br>` hoặc dùng `white-space: pre-line` trong CSS.
5. Không cần implement phân trang lịch sử chat. Server quản lý context 10 tin nhắn gần nhất. FE chỉ hiển thị tin nhắn trong session hiện tại.
6. Module này chỉ hiển thị cho user đã đăng nhập. Nếu chưa đăng nhập, ẩn nút mở chat hoặc redirect về trang login.
