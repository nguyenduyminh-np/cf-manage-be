Cảm ơn bạn đã review rất kỹ và chi tiết. Những điểm bạn nêu ra hoàn toàn chính xác và cực kỳ quan trọng khi đưa từ thiết kế logic vào triển khai thực tế trên production. Tôi sẽ cập nhật toàn bộ các query để giải quyết dứt điểm 4 vấn đề trên.

Dưới đây là **phiên bản tối ưu hoàn chỉnh** của tất cả các câu lệnh SQL, kèm theo giải thích ngắn gọn về những gì đã thay đổi.

---

### **Các thay đổi đồng loạt áp dụng:**

- **Tên bảng:** Đổi `dining_table` → `cafe_table` để đồng bộ naming convention của dự án.
- **Điều kiện ngày:** Tất cả `DATE(created_at) = ...` chuyển sang Range Query `created_at >= ... AND created_at < ... + INTERVAL 1 DAY` để tận dụng Index.
- **Trạng thái đơn hàng:** Không hardcode ID nữa, mà Join với bảng `dish_order_status` và lọc theo mã code (vd: `dos.dish_order_status_code = 'PROCESSING'`).
- **Payment status:** Đưa ra giải pháp tạm thời bằng cách chuẩn hóa trong query (dùng `UPPER()` hoặc so sánh với các giá trị đã biết), kèm theo ghi chú cần migration để làm sạch dữ liệu sau này.

---

### **1. Nhóm KPI Cards (8 câu lệnh)**

**KPI 1: Doanh thu hôm nay**
```sql
SELECT SUM(total_amount) AS revenue_today
FROM invoice
WHERE UPPER(payment_status) = 'PAID'
  AND created_at >= CURDATE()
  AND created_at < CURDATE() + INTERVAL 1 DAY
  AND is_active = 1;
```

**KPI 2: Tổng đơn hàng hôm nay**
```sql
SELECT 
  COUNT(*) AS total_orders,
  SUM(CASE WHEN dos.dish_order_status_code = 'PAID' THEN 1 ELSE 0 END) AS paid_orders
FROM dish_order do
JOIN dish_order_status dos ON do.dish_order_status_id = dos.id
WHERE do.created_at >= CURDATE()
  AND do.created_at < CURDATE() + INTERVAL 1 DAY
  AND do.is_active = 1;
```

**KPI 3: Bàn đang phục vụ**
```sql
SELECT COUNT(*) AS occupied_tables
FROM cafe_table
WHERE table_status = 'OCCUPIED'
  AND is_active = 1;
```

**KPI 4: Nhân viên đang hoạt động**
```sql
SELECT COUNT(*) AS active_staff
FROM account a
JOIN role r ON a.role_id = r.id
WHERE a.is_active = 1
  AND r.role_code IN ('PC-006', 'PC-008');
```

**KPI 5: Đơn chờ bếp (đang chế biến)**
```sql
SELECT COUNT(*) AS processing_orders
FROM dish_order do
JOIN dish_order_status dos ON do.dish_order_status_id = dos.id
WHERE dos.dish_order_status_code = 'PROCESSING'
  AND do.created_at >= CURDATE()
  AND do.created_at < CURDATE() + INTERVAL 1 DAY
  AND do.is_active = 1;
```

**KPI 6: Nợ nhà cung cấp (không thay đổi)**
```sql
SELECT COALESCE(SUM(total_amount), 0) AS total_debt
FROM debt
WHERE is_paid = 0;
```

**KPI 7: Tồn kho sắp hết hạn (< 30 ngày)**
```sql
SELECT COUNT(*) AS expiring_soon
FROM stock_level
WHERE is_active = 1
  AND expiration_at >= NOW()
  AND expiration_at <= NOW() + INTERVAL 30 DAY;
```

**KPI 8: Booking sắp đến trong 2 giờ tới**
```sql
SELECT COUNT(*) AS upcoming_bookings
FROM table_booking
WHERE expected_arrive_time BETWEEN NOW() AND NOW() + INTERVAL 2 HOUR
  AND booking_status NOT IN ('CANCELLED', 'EXPIRED')
  AND is_active = 1;
```

---

### **2. Nhóm Biểu đồ (6 câu lệnh)**

**Biểu đồ 1: Đường – Doanh thu 7 ngày gần nhất**
```sql
SELECT 
  DATE(created_at) AS date,
  SUM(total_amount) AS daily_revenue
FROM invoice
WHERE UPPER(payment_status) = 'PAID'
  AND created_at >= CURDATE() - INTERVAL 6 DAY
  AND created_at < CURDATE() + INTERVAL 1 DAY
  AND is_active = 1
GROUP BY DATE(created_at)
ORDER BY date;
```

**Biểu đồ 2: Cột – Số đơn theo giờ trong ngày**
```sql
SELECT 
  HOUR(created_at) AS hour,
  COUNT(*) AS order_count
FROM dish_order
WHERE created_at >= CURDATE()
  AND created_at < CURDATE() + INTERVAL 1 DAY
  AND is_active = 1
GROUP BY HOUR(created_at)
ORDER BY hour;
```

**Biểu đồ 3: Bánh – Trạng thái đơn hàng hôm nay**
```sql
SELECT 
  dos.dish_order_status_name AS status,
  COUNT(do.id) AS order_count
FROM dish_order do
JOIN dish_order_status dos ON do.dish_order_status_id = dos.id
WHERE do.created_at >= CURDATE()
  AND do.created_at < CURDATE() + INTERVAL 1 DAY
  AND do.is_active = 1
GROUP BY dos.id, dos.dish_order_status_name;
```

**Biểu đồ 4: Ngang – Top 5 món bán chạy hôm nay**
```sql
SELECT 
  d.dish_name,
  SUM(dod.quantity) AS total_quantity
FROM dish_order_detail dod
JOIN dish_order do ON dod.dish_order_id = do.id
JOIN dish d ON dod.dish_id = d.id
JOIN dish_order_status dos ON do.dish_order_status_id = dos.id
WHERE do.created_at >= CURDATE()
  AND do.created_at < CURDATE() + INTERVAL 1 DAY
  AND do.is_active = 1
  AND dos.dish_order_status_code != 'CANCEL'
GROUP BY d.id, d.dish_name
ORDER BY total_quantity DESC
LIMIT 5;
```

**Biểu đồ 5: Cột chồng – Bàn trống/theo tầng**
```sql
SELECT 
  floor,
  SUM(CASE WHEN table_status = 'OCCUPIED' THEN 1 ELSE 0 END) AS occupied,
  SUM(CASE WHEN table_status = 'AVAILABLE' THEN 1 ELSE 0 END) AS available
FROM cafe_table
WHERE is_active = 1
GROUP BY floor
ORDER BY floor;
```

**Biểu đồ 6: Bánh – Cơ cấu công nợ**
```sql
SELECT 
  s.supplier_name,
  SUM(d.total_amount) AS debt_amount
FROM debt d
JOIN supplier s ON d.supplier_id = s.id
WHERE d.is_paid = 0
GROUP BY s.id, s.supplier_name
ORDER BY debt_amount DESC;
```

---

### **3. Nhóm Bảng Danh Sách Nhanh (5 câu lệnh)**

**Bảng 1: Đơn hàng đang chế biến**
```sql
SELECT 
  do.id AS order_id,
  ct.table_name,
  do.created_at,
  GROUP_CONCAT(CONCAT(d.dish_name, ' x', dod.quantity) SEPARATOR ', ') AS items_summary
FROM dish_order do
JOIN cafe_table ct ON do.dining_table_id = ct.id
JOIN dish_order_detail dod ON do.id = dod.dish_order_id
JOIN dish d ON dod.dish_id = d.id
JOIN dish_order_status dos ON do.dish_order_status_id = dos.id
WHERE dos.dish_order_status_code = 'PROCESSING'
  AND do.created_at >= CURDATE()
  AND do.created_at < CURDATE() + INTERVAL 1 DAY
  AND do.is_active = 1
GROUP BY do.id, ct.table_name, do.created_at
ORDER BY do.created_at DESC;
```

**Bảng 2: Booking sắp đến (trong 2 giờ tới)**
```sql
SELECT 
  tb.expected_arrive_time,
  tb.customer_name,
  tb.phone_number,
  ct.table_name,
  tb.note
FROM table_booking tb
JOIN cafe_table ct ON tb.dining_table_id = ct.id
WHERE tb.expected_arrive_time BETWEEN NOW() AND NOW() + INTERVAL 2 HOUR
  AND tb.booking_status NOT IN ('CANCELLED', 'EXPIRED')
  AND tb.is_active = 1
ORDER BY tb.expected_arrive_time;
```

**Bảng 3: Cảnh báo tồn kho (hết hạn gấp / sắp hết)**  
```sql
SELECT 
  i.ingredient_name,
  sl.id AS batch_id,
  sl.quantity,
  sl.expiration_at,
  w.warehouse_name
FROM stock_level sl
JOIN ingredient i ON sl.ingredient_id = i.id
JOIN warehouse w ON sl.warehouse_id = w.id
WHERE sl.is_active = 1
  AND sl.expiration_at >= NOW()
  AND (sl.expiration_at <= NOW() + INTERVAL 7 DAY OR sl.quantity <= 5)
ORDER BY sl.expiration_at ASC;
```

**Bảng 4: Hóa đơn chưa thanh toán (pending payment)**
```sql
SELECT 
  inv.invoice_code,
  ct.table_name,
  inv.total_amount,
  inv.created_at,
  inv.customer_name
FROM invoice inv
JOIN cafe_table ct ON inv.dining_table_id = ct.id
WHERE UPPER(inv.payment_status) NOT IN ('PAID', 'ĐÃ HỦY')
  AND inv.is_active = 1
ORDER BY inv.created_at ASC;  -- ưu tiên hóa đơn lâu chưa thanh toán
```

**Bảng 5: Đơn nhập hàng đang soạn (Drafts)**
```sql
SELECT 
  po.purchase_order_code,
  po.total_amount,
  po.created_at,
  s.supplier_name
FROM purchase_order po
JOIN supplier s ON po.supplier_id = s.id
WHERE UPPER(po.payment_status) = 'DRAFT'
  AND po.is_active = 1
ORDER BY po.created_at DESC;
```

---

### **4. Ghi chú quan trọng cho Backend Team**

1.  **Chuẩn hóa dữ liệu khẩn cấp:** Tôi khuyến nghị bạn chạy một script nhỏ để quét và cập nhật lại toàn bộ cột `payment_status` (vd: `Chờ thanh toán` → `UNPAID`, `Đã thanh toán` → `PAID`, `completed` → `COMPLETED`, `draft` → `DRAFT`). Khi đó các câu lệnh có thể bỏ `UPPER()` và việc so sánh sẽ an toàn tuyệt đối.
2.  **Enum trong Backend:** Định nghĩa Enum `PaymentStatus` và `OrderStatusCode` trùng khớp với giá trị đã chuẩn hóa sẽ giúp code Java/Python clean và an toàn hơn.
3.  **Index bổ sung:** Đối với các bảng lớn, kiểm tra xem cột `expected_arrive_time` (`table_booking`) và `expiration_at` (`stock_level`) đã được đánh index chưa để tối ưu các truy vấn cảnh báo.

Toàn bộ phiên bản query trên đây đảm bảo:
- Tận dụng tối đa index, không còn lo ngại Full Table Scan vì `DATE()`.
- Dữ liệu nhất quán, không phụ thuộc vào ID tự sinh.
- Tuân thủ đúng nomenclature của hệ thống (`cafe_table`).