# TECHNICAL ANALYSIS – PHÂN TÍCH KỸ THUẬT TRIỂN KHAI MODULE BOOKING

> **Phiên bản:** 1.0  
> **Cập nhật:** 2026-04-11  
> **Dự án:** CF Manager – Module Quản lý đặt bàn nhà hàng  
> **Nguồn tài liệu nghiệp vụ:** CORE_BUSINESS_BOOKING_MANAGE_STATE

---

## MỤC LỤC

1. [Kiến trúc tổng quan & Sơ đồ luồng dữ liệu](#1-kiến-trúc-tổng-quan--sơ-đồ-luồng-dữ-liệu)
2. [Kỹ thuật 1 – State Machine (Máy trạng thái)](#2-kỹ-thuật-1--state-machine-máy-trạng-thái)
3. [Kỹ thuật 2 – Validator Chain (Chuỗi xác thực rule nghiệp vụ)](#3-kỹ-thuật-2--validator-chain-chuỗi-xác-thực-rule-nghiệp-vụ)
4. [Kỹ thuật 3 – Redis Distributed Lock (Khóa phân tán)](#4-kỹ-thuật-3--redis-distributed-lock-khóa-phân-tán)
5. [Kỹ thuật 4 – Scheduler + ShedLock (Tác vụ nền lên lịch)](#5-kỹ-thuật-4--scheduler--shedlock-tác-vụ-nền-lên-lịch)
6. [Kỹ thuật 5 – Domain Event + After-Commit Listener (Sự kiện miền)](#6-kỹ-thuật-5--domain-event--after-commit-listener-sự-kiện-miền)
7. [Kỹ thuật 6 – WebSocket STOMP (Thông báo thời gian thực)](#7-kỹ-thuật-6--websocket-stomp-thông-báo-thời-gian-thực)
8. [Kỹ thuật 7 – Custom Exception Handling (Xử lý lỗi tập trung)](#8-kỹ-thuật-7--custom-exception-handling-xử-lý-lỗi-tập-trung)
9. [Tổng hợp – Luồng end-to-end hoàn chỉnh](#9-tổng-hợp--luồng-end-to-end-hoàn-chỉnh)

---

## 1. KIẾN TRÚC TỔNG QUAN & SƠ ĐỒ LUỒNG DỮ LIỆU

### 1.1 Sơ đồ kiến trúc tổng thể

```
┌─────────────────────────────────────────────────────────────────────┐
│                         CLIENT (FE / POS)                           │
│  ┌────────────────┐                    ┌───────────────────────┐    │
│  │ REST API Call   │                    │ WebSocket Subscribe   │    │
│  └──────┬─────────┘                    └───────────▲───────────┘    │
└─────────┼──────────────────────────────────────────┼────────────────┘
          │                                          │
          ▼                                          │
┌─────────────────────────────────────────────────────────────────────┐
│                     SPRING BOOT APPLICATION                         │
│                                                                     │
│  ┌──────────────────┐    ┌──────────────────────────────────┐      │
│  │   Controller     │───▶│     Service Layer                │      │
│  │ (REST Endpoint)  │    │  ┌───────────────────────────┐   │      │
│  └──────────────────┘    │  │  BookingLockService       │   │      │
│                          │  │  (Redis Distributed Lock) │   │      │
│                          │  └─────────┬─────────────────┘   │      │
│                          │            │                     │      │
│                          │            ▼                     │      │
│                          │  ┌───────────────────────────┐   │      │
│                          │  │  BookingStateMachine      │   │      │
│                          │  │  (State Transition)       │   │      │
│                          │  └─────────┬─────────────────┘   │      │
│                          │            │                     │      │
│                          │            ▼                     │      │
│                          │  ┌───────────────────────────┐   │      │
│                          │  │ BookingRuleValidatorChain │   │      │
│                          │  │ (Rule 1→18 Validation)    │   │      │
│                          │  └─────────┬─────────────────┘   │      │
│                          │            │                     │      │
│                          │            ▼                     │      │
│                          │  ┌───────────────────────────┐   │      │
│                          │  │ Repository (JPA + Native) │   │      │
│                          │  └─────────┬─────────────────┘   │      │
│                          │            │                     │      │
│                          │            ▼                     │      │
│                          │  ┌───────────────────────────┐   │      │
│                          │  │ DomainEventPublisher      │───┼──┐   │
│                          │  │ (publish trong TX)        │   │  │   │
│                          │  └───────────────────────────┘   │  │   │
│                          └──────────────────────────────────┘  │   │
│                                                                │   │
│  ┌──────────────────────────────────────────────────────────┐  │   │
│  │  AfterCommitListener                                     │◀─┘   │
│  │  (Chỉ push notify SAU KHI transaction commit thành công) │      │
│  └──────────────┬───────────────────────────────────────────┘      │
│                 │                                                   │
│                 ▼                                                   │
│  ┌──────────────────────────────────────────────────────────┐      │
│  │  BookingNotificationService                               │      │
│  │  (SimpMessagingTemplate → WebSocket STOMP)                │──────┼──▶ Client
│  └──────────────────────────────────────────────────────────┘      │
│                                                                     │
│  ┌──────────────────────────────────────────────────────────┐      │
│  │  BookingSchedulerTasks                                    │      │
│  │  (Spring @Scheduled + ShedLock)                           │      │
│  │  → Gọi BookingSchedulerService → State Machine → DB      │      │
│  └──────────────────────────────────────────────────────────┘      │
└─────────────────────────────────────────────────────────────────────┘
          │
          ▼
┌─────────────────────┐    ┌──────────────────────┐
│  MySQL 8            │    │  Redis                │
│  (Data persistence) │    │  (Lock + ShedLock)    │
└─────────────────────┘    └──────────────────────┘
```

### 1.2 Cây folder kỹ thuật trong project

```
src/main/java/com/duyminhdev/cf_manager/
├── state_machine/booking/          ← Kỹ thuật 1: State Machine
│   ├── BookingStateMachine.java           (Interface)
│   ├── BookingStateMachineImpl.java       (Implementation 280 dòng)
│   └── BookingTransitionContext.java      (Context truyền vào transition)
│
├── validator/booking/              ← Kỹ thuật 2: Validator Chain
│   ├── BookingValidationRule.java         (Interface: order + supports + validate)
│   ├── AbstractBookingValidationRule.java (Base class: fail + warn)
│   ├── BookingRuleValidatorChain.java     (Orchestrator chạy chain)
│   ├── BookingValidationContext.java      (Shared context)
│   ├── AdvanceBookingValidator.java       (Rule 1)
│   ├── DurationValidator.java            (Rule 1)
│   ├── NoConflictValidator.java          (Rule 1,2,12,15)
│   ├── DepositValidator.java             (Rule 2)
│   ├── EarlyArrivalValidator.java        (Rule 10)
│   ├── LateArrivalValidator.java         (Rule 4,12)
│   ├── ExtensionValidator.java           (Rule 13)
│   ├── BookingOwnershipValidator.java    (Rule 11)
│   ├── WalkInGuardValidator.java         (Rule 9,16)
│   ├── WalkInPreAssignWarningValidator.java (Rule 16)
│   └── PendingConfirmationAdvisoryValidator.java (Rule 18)
│
├── lock/booking/                   ← Kỹ thuật 3: Redis Lock
│   ├── BookingLockService.java            (Interface)
│   └── BookingLockServiceImpl.java        (Redisson implementation)
│
├── scheduler/booking/              ← Kỹ thuật 4: Scheduler
│   └── BookingSchedulerTasks.java         (5 cron jobs + ShedLock)
│
├── event/booking/                  ← Kỹ thuật 5: Domain Event
│   ├── BookingMutationType.java           (Enum 13 loại mutation)
│   ├── BookingMutationEvent.java          (Event object)
│   ├── BookingDomainEventPublisher.java   (Publisher trong TX)
│   └── BookingMutationAfterCommitListener.java (Listener SAU commit)
│
├── config/                         ← Kỹ thuật 6 & cấu hình
│   ├── WebSocketConfig.java               (STOMP + SockJS)
│   ├── RedissonConfig.java                (Redisson client)
│   └── SchedulerConfig.java              (ShedLock + RedisLockProvider)
│
└── exceptions/                     ← Kỹ thuật 7: Exception Handling
    ├── BookingStateTransitionException.java
    ├── BookingLockException.java
    └── GlobalExceptionHandler.java        (Centralized handler)
```

---

## 2. KỸ THUẬT 1 – STATE MACHINE (MÁY TRẠNG THÁI)

### Mục đích

Đảm bảo **vòng đời booking** (PENDING → CONFIRMED → CHECKED_IN → COMPLETED / CANCELLED / EXPIRED) luôn tuân thủ **đúng thứ tự nghiệp vụ**, không cho phép nhảy trạng thái tùy ý. Ngăn chặn mọi thao tác set `bookingStatus` trực tiếp ngoài State Machine.

### Kỹ thuật

**Design Pattern:** Finite State Machine (FSM) kết hợp Transition Map.

Thay vì dùng thư viện state machine phức tạp (Spring Statemachine), triển khai nhẹ bằng:
- **Transition Map** (`Map<BookingStatusEnum, Set<BookingStatusEnum>>`): Khai báo tĩnh tất cả các cặp chuyển trạng thái hợp lệ.
- **Transition Context** (`BookingTransitionContext`): Đối tượng chứa toàn bộ thông tin cần thiết cho 1 lần chuyển trạng thái (booking, target status, thời gian, force flag, ...).
- **Guard checks**: Mỗi transition bắt buộc qua các kiểm tra: booking tồn tại → active → lock table → validate transition hợp lệ → re-validate business rule → set trạng thái.

### Chức năng

1. **`initialize(booking, initialStatus)`**: Khởi tạo booking mới vào `PENDING` hoặc `CONFIRMED` (walk-in có thể bypass PENDING). Chạy validator chain cho CREATE + CONFIRM nếu cần.

2. **`transition(context)`**: Chuyển trạng thái booking. Là **điểm vào duy nhất** cho mọi thay đổi status.
   - Validate context (null check)
   - Kiểm tra booking active
   - Lock table (pessimistic lock DB level qua `findByIdAndActiveTrueForUpdate`)
   - Kiểm tra transition hợp lệ theo ALLOWED_TRANSITIONS map
   - Resolve `checkInAt` / `checkOutAt` (tự động set NOW nếu chưa có)
   - Re-validate business rules theo target status
   - Set trạng thái mới

3. **Guard riêng cho từng target status:**
   - `CANCELLED`: Kiểm tra chưa qua giờ hẹn
   - `EXPIRED`: Kiểm tra quá 30p no-show + chưa check-in
   - `COMPLETED`: Bắt buộc có `checkInAt` và `checkOutAt`

### Triển khai luồng thực tế

```
API Call: POST /{id}/check-in
       │
       ▼
Service: tableBookingService.checkInBooking(id, request)
       │
       ▼
LockService.executeWithTableLock(tableId, () -> {
       │
       ▼
    StateMachine.transition(BookingTransitionContext.builder()
        .booking(booking)                         ← booking entity từ DB
        .targetStatus(BookingStatusEnum.CHECKED_IN) ← trạng thái đích
        .requestedCheckInAt(request.getCheckInAt()) ← thời gian check-in
        .force(request.isForce())                   ← bypass cảnh báo conflict
        .build()
    )
       │
       ▼  Bên trong StateMachine.transition():
       │
       ├─ 1. validateContext(context)           → null check booking, targetStatus
       ├─ 2. ensureActiveBooking(booking)       → kiểm tra is_active = 1
       ├─ 3. lockTable(booking)                 → SELECT ... FOR UPDATE trên dining_table
       ├─ 4. isAllowedTransition(CONFIRMED, CHECKED_IN) → true ✓
       ├─ 5. resolveCheckInAt → requestedCheckInAt hoặc NOW()
       ├─ 6. resolveCheckOutAt → giữ nguyên booking.checkOutAt
       ├─ 7. revalidateBusinessRules():
       │      BookingRuleValidatorChain.validate(CHECK_IN, validationContext)
       │      → chạy EarlyArrivalValidator, LateArrivalValidator, BookingOwnershipValidator
       └─ 8. booking.setBookingStatus("CHECKED_IN")
              booking.setCheckInAt(resolvedCheckInAt)
       │
       ▼
    Repository save(booking)
    Table save(table → OCCUPIED)
    EventPublisher.publish(CHECK_IN, bookingId, tableId)
})  ← release lock
```

### Code thực tế – ALLOWED_TRANSITIONS Map

```java
// File: BookingStateMachineImpl.java (line 23-40)
private static final Map<BookingStatusEnum, Set<BookingStatusEnum>> ALLOWED_TRANSITIONS = Map.of(
    BookingStatusEnum.PENDING, Set.of(
        BookingStatusEnum.CONFIRMED,
        BookingStatusEnum.CANCELLED
    ),
    BookingStatusEnum.CONFIRMED, Set.of(
        BookingStatusEnum.CHECKED_IN,
        BookingStatusEnum.CANCELLED,
        BookingStatusEnum.EXPIRED
    ),
    BookingStatusEnum.CHECKED_IN, Set.of(
        BookingStatusEnum.COMPLETED,
        BookingStatusEnum.CANCELLED
    ),
    BookingStatusEnum.CANCELLED, Set.of(),   // terminal state
    BookingStatusEnum.COMPLETED, Set.of(),   // terminal state
    BookingStatusEnum.EXPIRED, Set.of()      // terminal state
);
```

### Tại sao dùng kỹ thuật này?

| Vấn đề nghiệp vụ | Giải pháp State Machine |
|---|---|
| Nhân viên vô tình set COMPLETED cho booking PENDING | Map không cho phép PENDING → COMPLETED |
| Code service set status trực tiếp → khó kiểm soát | Chỉ 1 điểm vào duy nhất (transition) |
| Cần re-validate rule tại thời điểm chuyển trạng thái | State Machine tự động gọi ValidatorChain |
| Cần audit/log mọi transition | Tập trung tại 1 method |

---

## 3. KỸ THUẬT 2 – VALIDATOR CHAIN (CHUỖI XÁC THỰC RULE NGHIỆP VỤ)

### Mục đích

Tách riêng **18 quy tắc nghiệp vụ** (Rule 1→18) thành các validator độc lập, dễ test đơn vị, dễ thêm/xóa rule mà không ảnh hưởng nhau. Đảm bảo đúng rule được chạy đúng thời điểm (tạo booking, confirm, check-in, walk-in, extend...).

### Kỹ thuật

**Design Pattern:** Chain of Responsibility + Strategy Pattern.

```
                     BookingRuleValidatorChain
                              │
                    validate(useCase, context)
                              │
              ┌───────────────┼───────────────┐
              ▼               ▼               ▼
    rules.stream()                                
        .filter(rule -> rule.supports(useCase))   ← Chỉ chạy rule phù hợp use-case
        .sorted(Comparator.comparingInt(order))   ← Chạy theo thứ tự ưu tiên
        .forEach(rule -> rule.validate(context))  ← Chạy tuần tự, fail-fast
```

**Các thành phần:**

| Component | Vai trò |
|---|---|
| `BookingValidationRule` (interface) | Contract: `order()` + `supports(useCase)` + `validate(context)` |
| `AbstractBookingValidationRule` (abstract) | Base class: cung cấp `fail(message)` ném exception, `warn(context, message)` ghi cảnh báo |
| `BookingRuleValidatorChain` (orchestrator) | Inject toàn bộ rule qua Spring DI, lọc/sắp xếp/chạy theo useCase |
| `BookingValidationContext` (shared data) | Chứa booking, table, thời gian, force flag, warnings list |
| `BookingValidationUseCase` (enum) | Định danh use-case: `CREATE_BOOKING`, `CONFIRM_BOOKING`, `CHECK_IN`, `WALK_IN_BOOKING`, `EXTEND_BOOKING`, `LATE_ARRIVAL_WALK_IN` |

### Chức năng

**Mỗi Validator là 1 class riêng biệt:**

| Validator | Rule | order | UseCase áp dụng | Logic kiểm tra |
|---|---|---|---|---|
| `AdvanceBookingValidator` | 1 | 10 | CREATE | `expectedArriveTime >= NOW() + 2h` |
| `DurationValidator` | 1 | 20 | CREATE | `expectedCheckOut > expectedArriveTime`, tối thiểu 2h |
| `NoConflictValidator` | 1,2,12 | 30 | CREATE, CONFIRM, LATE_ARRIVAL_WALK_IN | Không overlap khung giờ với booking CONFIRMED/CHECKED_IN trên cùng bàn |
| `DepositValidator` | 2 | 10 | CONFIRM | Nếu `depositAmount > 0` → bắt buộc `depositPaid = true` |
| `EarlyArrivalValidator` | 10 | 10 | CHECK_IN | Tính lại checkout khi sớm >30p, check conflict booking kế tiếp |
| `LateArrivalValidator` | 4,12 | 20 | CHECK_IN | Kiểm tra muộn >30p → chặn (phải qua flow walk-in) |
| `BookingOwnershipValidator` | 11 | 30 | CHECK_IN | Bàn BOOKED phải do chính booking này reserve |
| `ExtensionValidator` | 13 | 10 | EXTEND | Conflict booking kế tiếp khi gia hạn |
| `WalkInGuardValidator` | 9,16 | 10 | WALK_IN, LATE_ARRIVAL_WALK_IN | Chặn walk-in nếu bàn có booking CONFIRMED trong 30p |
| `WalkInPreAssignWarningValidator` | 16 | 20 | WALK_IN, LATE_ARRIVAL_WALK_IN | Cảnh báo nếu bàn có booking CONFIRMED trong 30p→2h |
| `PendingConfirmationAdvisoryValidator` | 18 | 30 | WALK_IN | Cảnh báo nếu bàn có PENDING chưa cọc |

### Triển khai luồng thực tế – Ví dụ tạo booking mới

```
Service: createBooking(request)
       │
       ▼
StateMachine.initialize(booking, PENDING)
       │
       ▼
BookingRuleValidatorChain.validate(BookingValidationUseCase.CREATE_BOOKING, context)
       │
       ├─ Filter: rule.supports(CREATE_BOOKING) ?
       │    ✓ AdvanceBookingValidator (order=10)
       │    ✓ DurationValidator (order=20)
       │    ✓ NoConflictValidator (order=30)
       │    ✗ DepositValidator → skip (chỉ CONFIRM)
       │    ✗ EarlyArrivalValidator → skip (chỉ CHECK_IN)
       │    ✗ WalkInGuardValidator → skip (chỉ WALK_IN)
       │    ...
       │
       ├─ Sort by order: 10 → 20 → 30
       │
       └─ Chạy tuần tự (fail-fast):
            │
            ├─ AdvanceBookingValidator.validate(context):
            │    expectedArriveTime >= NOW() + 2h ?
            │    → Nếu vi phạm: fail("must book at least 2 hours in advance")
            │    → Exception → DỪNG chain, không chạy validator tiếp
            │
            ├─ DurationValidator.validate(context):
            │    expectedCheckOut > expectedArriveTime ?
            │    → OK → tiếp tục
            │
            └─ NoConflictValidator.validate(context):
                 existsConflictBookingOnTable(tableId, start, end, statuses, excludeId) ?
                 → Nếu có conflict: fail("time slot conflicts with existing booking")
                 → OK → Chain hoàn thành ✓
```

### Code thực tế – BookingRuleValidatorChain

```java
// File: BookingRuleValidatorChain.java
@Component
@RequiredArgsConstructor
public class BookingRuleValidatorChain {

    private final List<BookingValidationRule> rules;  // Spring auto-inject tất cả impl

    public void validate(BookingValidationUseCase useCase, BookingValidationContext context) {
        rules.stream()
                .filter(rule -> rule.supports(useCase))    // chỉ rule phù hợp use-case
                .sorted(Comparator.comparingInt(BookingValidationRule::order))  // theo thứ tự
                .forEach(rule -> rule.validate(context));  // chạy tuần tự, fail-fast
    }
}
```

### Tại sao dùng kỹ thuật này?

| Vấn đề | Giải pháp Validator Chain |
|---|---|
| 18 rule lẫn lộn trong service → khó đọc, khó test | Mỗi rule = 1 file riêng, unit test riêng |
| Thêm rule mới phải sửa service → rủi ro regression | Tạo class mới implements BookingValidationRule → tự động inject |
| Cùng rule chạy ở nhiều use-case → duplicate code | Mỗi rule tự khai báo `supports()` cho use-cases cần thiết |
| Cần thứ tự chạy rule → khó quản lý | `order()` method cho phép sắp xếp rõ ràng |

---

## 4. KỸ THUẬT 3 – REDIS DISTRIBUTED LOCK (KHÓA PHÂN TÁN)

### Mục đích

Ngăn chặn **race condition** khi nhiều nhân viên thao tác trên cùng một bàn cùng lúc (Rule 15, 17). Ví dụ: 2 NV cùng đặt 1 bàn cho cùng khung giờ, hoặc 2 NV cùng xếp walk-in vào 1 bàn trống.

### Kỹ thuật

**Công nghệ:** Redis + Redisson Client.

Redisson cung cấp `RLock` (Reentrant Lock) hoạt động trên Redis, đảm bảo:
- **Mutual exclusion:** Chỉ 1 thread/process hold lock tại 1 thời điểm.
- **Distributed:** Hoạt động đúng trên multi-instance deployment.
- **Auto-release:** Lock tự giải phóng sau `leaseTime` nếu thread bị treo.
- **Reentrant:** Cùng 1 thread có thể acquire cùng lock nhiều lần.

**Cấu hình Redisson:**

```java
// File: RedissonConfig.java
@Bean(destroyMethod = "shutdown")
@ConditionalOnMissingBean(RedissonClient.class)
public RedissonClient redissonClient(
        @Value("${spring.data.redis.host:localhost}") String host,
        @Value("${spring.data.redis.port:6379}") int port,
        @Value("${spring.data.redis.database:0}") int database,
        @Value("${spring.data.redis.password:}") String password,
        @Value("${spring.data.redis.timeout:60000}") long timeoutMs
) {
    Config config = new Config();
    config.useSingleServer()
            .setAddress("redis://" + host + ":" + port)
            .setDatabase(database)
            .setTimeout((int) Math.min(timeoutMs, Integer.MAX_VALUE));
    return Redisson.create(config);
}
```

### Chức năng

**`BookingLockServiceImpl`** cung cấp 2 method:

1. **`executeWithTableLock(tableId, action)`** – Lock 1 bàn:
   - Acquire lock key `lock:table:{tableId}`
   - Thực thi action (lambda)
   - Release lock trong `finally` block

2. **`executeWithTableLocks(tableIds, action)`** – Lock nhiều bàn (đổi bàn):
   - **Normalize + Sort theo id tăng dần** → tránh deadlock
   - Acquire lock tuần tự cho từng bàn
   - Nếu bất kỳ lock nào fail → release tất cả đã acquire, throw exception
   - Thực thi action
   - Release lock **theo thứ tự ngược** (LIFO) trong `finally`

**Thông số:**

| Tham số | Giá trị | Ý nghĩa |
|---|---|---|
| `waitSeconds` | Cấu hình qua constant | Thời gian tối đa chờ acquire lock |
| `leaseSeconds` | Cấu hình qua constant | Thời gian hold lock tối đa (auto-release) |
| Lock key format | `lock:table:{tableId}` | Mỗi bàn 1 lock riêng |

### Triển khai luồng thực tế – Race condition Rule 15

```
Nhân viên A: POST /create (bàn 8, 19:00-21:00)
Nhân viên B: POST /create (bàn 8, 19:30-21:30)   (cùng lúc)

Timeline:
──────────────────────────────────────────────────────

[NV A] → acquire lock:table:8 ✓ (SUCCESS - hold lock)
[NV B] → acquire lock:table:8 ✗ (WAIT... tryLock waitSeconds)

[NV A] → validate → save booking 101 → release lock ✓
[NV B] → acquire lock:table:8 ✓ (lock vừa được release)
[NV B] → NoConflictValidator: kiểm tra bàn 8, slot 19:30-21:30
         → Phát hiện booking 101 (19:00-21:00) xung đột!
         → fail("time slot conflicts with existing booking") ✗

Kết quả: NV B nhận lỗi "Bàn đã có người đặt vào khung giờ này"
```

### Triển khai luồng thực tế – Đổi bàn (Lock 2 bàn)

```
Nhân viên: chuyển booking từ bàn 8 sang bàn 3

LockService.executeWithTableLocks(List.of(8, 3), () -> {
    // Sort: [3, 8] → acquire lock:table:3 trước, rồi lock:table:8
    // → Tránh deadlock nếu NV khác đang đổi bàn 3 sang 8
    
    // ... validate + update ...
})
```

### Code thực tế – Core lock method

```java
// File: BookingLockServiceImpl.java
public <T> T executeWithTableLocks(Collection<Integer> tableIds, Supplier<T> action) {
    List<Integer> normalizedTableIds = normalizeTableIds(tableIds);  // distinct + sort ASC
    List<RLock> acquiredLocks = new ArrayList<>();

    try {
        for (Integer tableId : normalizedTableIds) {
            RLock lock = redissonClient.getLock(toLockKey(tableId));
            boolean acquired = tryAcquire(lock, tableId);
            if (!acquired) {
                throw new BookingLockException(
                    "Table is being updated by another request (tableId=" + tableId + ")");
            }
            acquiredLocks.add(lock);
        }
        return action.get();
    } finally {
        releaseLocks(acquiredLocks);  // LIFO: giải phóng ngược
    }
}
```

### Tại sao dùng kỹ thuật này?

| Vấn đề | Giải pháp Redis Lock |
|---|---|
| 2 NV cùng đặt 1 bàn → double booking | Lock theo tableId, giao dịch sau phải chờ |
| DB transaction isolation không đủ (race between read + write) | Lock trước khi đọc DB, đảm bảo serialize |
| Multi-instance deployment | Distributed lock (Redis) hoạt động cross-instance |
| Deadlock khi lock nhiều bàn | Sort tableId tăng dần → consistent lock order |

---

## 5. KỸ THUẬT 4 – SCHEDULER + SHEDLOCK (TÁC VỤ NỀN LÊN LỊCH)

### Mục đích

Tự động hóa các nghiệp vụ **xảy ra theo thời gian** mà không cần nhân viên thao tác thủ công:
- Reserve bàn trước 30p (Rule 3)
- Hết hạn booking no-show (Rule 7)
- Cảnh báo kẹt bàn (Rule 8)
- Hủy booking không order (Rule 14)
- Nhắc nhở sắp hết giờ (Rule 5)

### Kỹ thuật

**Công nghệ:** Spring `@Scheduled` + ShedLock (với Redis là backend).

| Layer | Công nghệ | Vai trò |
|---|---|---|
| Trigger | `@Scheduled(cron = "0 * * * * *")` | Chạy đầu mỗi phút |
| Distributed safety | `@SchedulerLock(name = "...")` | Đảm bảo chỉ 1 instance chạy |
| Lock backend | `RedisLockProvider` | ShedLock lưu lock trên Redis |
| Business logic | `BookingSchedulerService` | Service thực thi nghiệp vụ |

**Cấu hình ShedLock:**

```java
// File: SchedulerConfig.java
@Configuration
@EnableScheduling
@EnableSchedulerLock(defaultLockAtMostFor = "PT55S")  // lock tối đa 55 giây
public class SchedulerConfig {

    @Bean
    public LockProvider lockProvider(RedisConnectionFactory redisConnectionFactory) {
        return new RedisLockProvider(redisConnectionFactory);
    }
}
```

### Chức năng

**5 Cron Job được khai báo trong `BookingSchedulerTasks.java`:**

| Job | Method | ShedLock name | Nghiệp vụ |
|---|---|---|---|
| Job A | `reserveTablesBeforeArrival()` | `bookingSchedulerReserveTables` | AVAILABLE → BOOKED trước 30p |
| Job B | `expireNoShowBookings()` | `bookingSchedulerExpireNoShow` | CONFIRMED → EXPIRED sau 30p no-show |
| Job C | `notifyOccupiedConflicts()` | `bookingSchedulerOccupiedConflicts` | Cảnh báo đỏ bàn OCCUPIED sắp có booking |
| Job D | `processNoOrderTimeout()` | `bookingSchedulerNoOrderTimeout` | Cảnh báo 10p + auto cancel 20p không order |
| Job E | `notifyCheckoutReminder()` | `bookingSchedulerCheckoutReminder` | Nhắc nhở 15p trước hết giờ |

**Đặc điểm thiết kế:**
- **Cron configurable:** `${booking.scheduler.cron:0 * * * * *}` → có thể thay đổi qua config
- **Conditional:** `@ConditionalOnProperty(value = "booking.scheduler.enabled", havingValue = "true", matchIfMissing = true)` → có thể tắt scheduler trong test
- **Lock timing configurable:** `lockAtMostFor` và `lockAtLeastFor` qua config
- **Separation of concerns:** `BookingSchedulerTasks` chỉ là trigger, logic nằm trong `BookingSchedulerService`

### Triển khai luồng thực tế – Job A (Reserve bàn)

```
[Phút 18:30] Spring trigger cron → BookingSchedulerTasks.reserveTablesBeforeArrival()
       │
       ├─ ShedLock kiểm tra: lock "bookingSchedulerReserveTables" đã có instance khác hold?
       │   ├─ Có → SKIP (instance khác đang chạy)
       │   └─ Không → Acquire lock, bắt đầu chạy
       │
       ▼
BookingSchedulerService.reserveTablesForUpcomingConfirmedBookings()
       │
       ├─ Query: booking CONFIRMED, expectedArriveTime trong (18:30, 19:00]
       │   → Tìm thấy booking #101 (bàn 8, 19:00)
       │
       ├─ Với booking #101:
       │   ├─ Bàn 8 hiện tại: AVAILABLE
       │   ├─ Lock table 8 (Redis)
       │   ├─ Cập nhật: table_status = BOOKED
       │   ├─ Save DB
       │   ├─ Push WebSocket: TABLE_RESERVED
       │   └─ Release lock
       │
       └─ ShedLock release → sẵn sàng cho phút tiếp theo

[Phút 18:31] Spring trigger cron → lặp lại...
       │
       ├─ Query: booking CONFIRMED, expectedArriveTime trong (18:31, 19:01]
       │   → booking #101 vẫn match NHƯNG bàn 8 đã BOOKED
       │   → SKIP (chỉ chuyển bàn AVAILABLE)
       │
       └─ Idempotent ✓ (không gây side-effect khi chạy lại)
```

### Code thực tế – BookingSchedulerTasks

```java
// File: BookingSchedulerTasks.java
@Component
@RequiredArgsConstructor
@Slf4j
@ConditionalOnProperty(value = "booking.scheduler.enabled", havingValue = "true", matchIfMissing = true)
public class BookingSchedulerTasks {

    private final BookingSchedulerService bookingSchedulerService;

    @Scheduled(cron = "${booking.scheduler.cron:0 * * * * *}")
    @SchedulerLock(name = "bookingSchedulerReserveTables",
        lockAtMostFor = "${booking.scheduler.lock-at-most:PT50S}",
        lockAtLeastFor = "${booking.scheduler.lock-at-least:PT2S}")
    public void reserveTablesBeforeArrival() {
        bookingSchedulerService.reserveTablesForUpcomingConfirmedBookings();
    }
    
    // ... 4 jobs tương tự
}
```

### Tại sao dùng kỹ thuật này?

| Vấn đề | Giải pháp |
|---|---|
| Nhân viên phải nhớ dọn bàn trước 30p → hay quên | Job A tự động chuyển BOOKED + push thông báo |
| Khách no-show mà không ai hủy booking → bàn bị giữ | Job B tự động EXPIRED + giải phóng bàn |
| Deploy multi-instance → job chạy trùng → dữ liệu sai | ShedLock chỉ cho 1 instance chạy |
| Job fail giữa chừng → chạy lại bị duplicate | Thiết kế idempotent + de-dup notification |

---

## 6. KỸ THUẬT 5 – DOMAIN EVENT + AFTER-COMMIT LISTENER (SỰ KIỆN MIỀN)

### Mục đích

Đảm bảo thông báo realtime **chỉ được gửi SAU KHI transaction DB commit thành công**. Ngăn chặn tình huống FE nhận event "BOOKING_CONFIRMED" nhưng DB rollback → dữ liệu không khớp.

### Kỹ thuật

**Design Pattern:** Domain Event + Transactional Event Listener.

**Luồng hoạt động:**

```
[Trong @Transactional]                    [SAU commit]
        │                                       │
        ▼                                       ▼
┌─────────────────────┐              ┌─────────────────────────────┐
│ Service:             │              │ AfterCommitListener:         │
│  save(booking)       │              │  @TransactionalEventListener │
│  save(table)         │              │  (phase = AFTER_COMMIT)      │
│  publisher.publish   │──event──▶    │                               │
│    (mutationType,    │  (in-memory) │  1. Load booking/table mới   │
│     bookingId,       │              │  2. Build JSON payload       │
│     tableId)         │              │  3. Push WebSocket           │
└─────────────────────┘              └─────────────────────────────┘
```

**Các thành phần:**

| Component | File | Vai trò |
|---|---|---|
| `BookingMutationType` (enum) | `BookingMutationType.java` | 13 loại mutation: CREATE, CONFIRM, CHECK_IN, CHECK_OUT, CANCEL, EXPIRE, EXTEND, WALK_IN, ... |
| `BookingMutationEvent` (POJO) | `BookingMutationEvent.java` | Event object: mutationType + bookingId + tableId + occurredAt |
| `BookingDomainEventPublisher` | `BookingDomainEventPublisher.java` | Wrap `ApplicationEventPublisher`, publish event trong transaction |
| `BookingMutationAfterCommitListener` | `BookingMutationAfterCommitListener.java` | `@TransactionalEventListener(phase = AFTER_COMMIT)`: Nhận event SAU commit |

### Chức năng

**`BookingMutationAfterCommitListener.onBookingMutationAfterCommit(event)`:**

1. **Re-load dữ liệu mới nhất** từ DB (sau commit, đảm bảo đúng trạng thái).
2. **Resolve event name** từ mutationType → tên event string (BOOKING_CREATED, BOOKING_CONFIRMED, ...).
3. **Build payload JSON** chuẩn hóa (event, mutationType, bookingId, tableId, tableCode, tableStatus, bookingStatus, at, message, source).
4. **Push event chính** qua topic `/topic/booking-updates` (với de-dup key).
5. **Push event bổ sung** nếu cần:
   - Nếu mutation ảnh hưởng table status → push thêm `TABLE_STATUS_SYNC` qua `/topic/table-status`
   - Nếu mutation là DEPOSIT → push thêm `BOOKING_DEPOSIT_PAID` qua `/topic/deposit-events`
6. **Tất cả push đều qua `sendOnce()`** với dedup key để tránh gửi trùng.

### Triển khai luồng thực tế – Confirm booking

```
Service.confirmBooking(bookingId):
    @Transactional {
        │
        ├─ booking = loadFromDB
        ├─ stateMachine.transition(CONFIRMED)
        ├─ table giữ nguyên AVAILABLE
        ├─ save(booking)
        │
        ├─ eventPublisher.publish(CONFIRM, bookingId, tableId)
        │   └─ ApplicationEventPublisher.publishEvent(BookingMutationEvent)
        │      → Event được queue lại, CHƯA gửi
        │
        └─ return booking
    }   ← Transaction commit thành công ✓
        │
        ▼
    [Spring framework trigger @TransactionalEventListener(AFTER_COMMIT)]
        │
        ▼
    AfterCommitListener.onBookingMutationAfterCommit(event):
        │
        ├─ booking = reload từ DB (data mới nhất sau commit)
        ├─ table = load table
        ├─ eventName = "BOOKING_CONFIRMED"
        ├─ payload = { event, mutationType, bookingId, tableId, tableCode, ... }
        │
        ├─ notificationService.sendOnce("/topic/booking-updates",
        │      "booking:mutation:booking:CONFIRM:123:8:2026-04-10T...", payload)
        │
        ├─ affectsTableStatus(CONFIRM) = true
        │   → push TABLE_STATUS_SYNC via /topic/table-status
        │
        └─ log.info("Booking mutation committed: CONFIRM, bookingId=123")
```

### Code thực tế – Event Name Resolution

```java
// File: BookingMutationAfterCommitListener.java
private String resolveBookingEventName(BookingMutationType mutationType) {
    return switch (mutationType) {
        case CREATE -> "BOOKING_CREATED";
        case UPDATE -> "BOOKING_UPDATED";
        case CONFIRM -> "BOOKING_CONFIRMED";
        case CHECK_IN -> "BOOKING_CHECKED_IN";
        case CHECK_OUT -> "BOOKING_CHECKED_OUT";
        case CANCEL -> "BOOKING_CANCELLED";
        case EXPIRE -> "BOOKING_EXPIRED";
        case EXTEND -> "BOOKING_EXTENDED";
        case WALK_IN -> "BOOKING_WALK_IN_CREATED";
        case LATE_ARRIVAL_WALK_IN -> "BOOKING_LATE_ARRIVAL_WALK_IN_CREATED";
        case CANCEL_NO_ORDER_TIMEOUT -> "BOOKING_AUTO_CANCELLED_NO_ORDER";
        case DEPOSIT -> "BOOKING_DEPOSIT_UPDATED";
        // ...
    };
}
```

### Tại sao dùng kỹ thuật này?

| Vấn đề | Giải pháp |
|---|---|
| Push notify trước commit → DB rollback → FE hiển thị sai | `AFTER_COMMIT` đảm bảo chỉ push khi commit OK |
| Notify logic lẫn trong service → khó maintain | Tách riêng vào listener, service chỉ publish event |
| Cần push cùng event cho nhiều topic (booking-updates, table-status, deposit-events) | Listener tự fan-out dựa trên mutationType |
| Event gửi trùng do retry | `sendOnce()` với dedupKey |

---

## 7. KỸ THUẬT 6 – WEBSOCKET STOMP (THÔNG BÁO THỜI GIAN THỰC)

### Mục đích

Đẩy thông báo **realtime** xuống FE/POS mà không cần FE polling API liên tục. Cho phép UI cập nhật ngay lập tức khi:
- Booking thay đổi trạng thái
- Bàn thay đổi trạng thái
- Cảnh báo kẹt bàn, sắp hết giờ, không order
- Sự kiện đặt cọc

### Kỹ thuật

**Công nghệ:** Spring WebSocket + STOMP protocol + SockJS fallback.

**Cấu hình:**

```java
// File: WebSocketConfig.java
@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws")           // Endpoint kết nối
                .setAllowedOriginPatterns("*") // CORS
                .withSockJS();                 // SockJS fallback cho browser cũ
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker("/topic", "/queue");  // Broker topics
        registry.setApplicationDestinationPrefixes("/app"); // Client → Server
        registry.setUserDestinationPrefix("/user");         // User-specific messages
    }
}
```

### Chức năng

**4 Topics broadcast:**

| Topic | Mô tả | Dữ liệu gửi |
|---|---|---|
| `/topic/booking-updates` | Tất cả thay đổi booking | CREATED, CONFIRMED, CHECKED_IN, ... |
| `/topic/table-status` | Thay đổi trạng thái bàn | TABLE_STATUS_SYNC, TABLE_RESERVED |
| `/topic/table-alerts` | Cảnh báo vận hành | Kẹt bàn, sắp hết giờ, không order |
| `/topic/deposit-events` | Sự kiện cọc | BOOKING_DEPOSIT_PAID |

**Payload chuẩn hóa:**
- Tất cả event đều có: `event`, `mutationType`, `bookingId`, `tableId`, `tableCode`, `at`, `message`, `source`, `topic`, `dedupKey`, `eventId`
- FE dùng `dedupKey` để de-duplicate (cache TTL 2-5 phút)
- FE dùng `eventId` (UUID) để trace/log

### Triển khai luồng thực tế

```
[Server]                                      [Client FE/POS]
    │                                              │
    │  Service commit + publish event              │
    │         │                                    │
    │         ▼                                    │
    │  AfterCommitListener                         │
    │         │                                    │
    │         ▼                                    │
    │  SimpMessagingTemplate                       │
    │    .convertAndSend(                          │
    │      "/topic/booking-updates",               │
    │      payload                                 │
    │    )                                         │
    │         │                                    │
    │         └──── STOMP MESSAGE ──────────────▶  │
    │                                              │
    │                                    onBookingEvent(message):
    │                                     │ parse JSON payload
    │                                     │ check dedupKey
    │                                     │ if new → update UI
    │                                     │ if duplicate → skip
```

---

## 8. KỸ THUẬT 7 – CUSTOM EXCEPTION HANDLING (XỬ LÝ LỖI TẬP TRUNG)

### Mục đích

Chuẩn hóa tất cả response lỗi từ hệ thống thành **format JSON thống nhất** cho FE, phân biệt rõ loại lỗi (business rule vi phạm, lock xung đột, dữ liệu sai, lỗi hệ thống) bằng **mã HTTP + error code** riêng.

### Kỹ thuật

**Design Pattern:** `@RestControllerAdvice` + Custom Exception hierarchy.

**Exception hierarchy:**

```
RuntimeException
├── BookingStateTransitionException     ← Lỗi state machine / validator
│     HTTP 400 Bad Request
│     code: "BOOKING_STATE_TRANSITION_INVALID"
│
├── BookingLockException                ← Lỗi lock xung đột
│     HTTP 409 Conflict
│     code: "TABLE_LOCK_BUSY"
│
├── InvalidDataException                ← Lỗi dữ liệu đầu vào
│     HTTP 400 Bad Request
│     code: "INVALID_DATA"
│
└── Exception (catch-all)               ← Lỗi không dự kiến
      HTTP 500 Internal Server Error
      code: "INTERNAL_SERVER_ERROR"
```

### Chức năng

**`GlobalExceptionHandler`** (centralized `@RestControllerAdvice`):

| Exception | HTTP | Code | Khi nào xảy ra |
|---|---|---|---|
| `BookingStateTransitionException` | 400 | `BOOKING_STATE_TRANSITION_INVALID` | Chuyển trạng thái không hợp lệ, rule nghiệp vụ vi phạm |
| `BookingLockException` | 409 | `TABLE_LOCK_BUSY` | Bàn đang bị lock bởi request khác |
| `InvalidDataException` | 400 | `INVALID_DATA` | Dữ liệu request không hợp lệ |
| `Exception` (catch-all) | 500 | `INTERNAL_SERVER_ERROR` | Lỗi không dự kiến |

**Error Response chuẩn:**

```json
{
  "timestamp": "2026-04-10T15:00:00+07:00",
  "status": 400,
  "error": "Bad Request",
  "code": "BOOKING_STATE_TRANSITION_INVALID",
  "message": "[RULE_VIOLATION] time slot conflicts with an existing booking on the same table",
  "details": null,
  "path": "/api/v1/table-booking/create"
}
```

**Lưu ý prefix trong message:**
- `[GUARD_FAILED]` – Thiếu dữ liệu bắt buộc (booking null, table null, ...)
- `[NOT_ALLOWED]` – Transition không hợp lệ (PENDING → COMPLETED)
- `[RULE_VIOLATION]` – Rule nghiệp vụ vi phạm (conflict, quá hạn, ...)

### Triển khai luồng thực tế

```
FE gọi: POST /api/v1/table-booking/create
    body: { tableId: 8, expectedArriveTime: "2026-04-10T16:00:00" }  ← chỉ sau 1h
       │
       ▼
Controller → Service → StateMachine.initialize()
       │
       ▼
ValidatorChain → AdvanceBookingValidator.validate():
    expectedArriveTime < NOW() + 2h ← VI PHẠM Rule 1
       │
       ▼
AbstractBookingValidationRule.fail("must book at least 2 hours in advance")
       │
       ▼
throw BookingStateTransitionException("[RULE_VIOLATION] must book at least 2 hours in advance")
       │
       ▼
GlobalExceptionHandler.bookingStateTransition(ex, request):
       │
       ▼
ResponseEntity 400:
{
  "timestamp": "2026-04-10T15:00:00+07:00",
  "status": 400,
  "error": "Bad Request",
  "code": "BOOKING_STATE_TRANSITION_INVALID",
  "message": "[RULE_VIOLATION] must book at least 2 hours in advance",
  "details": null,
  "path": "/api/v1/table-booking/create"
}
       │
       ▼
FE nhận response → hiển thị message cho user
```

---

## 9. TỔNG HỢP – LUỒNG END-TO-END HOÀN CHỈNH

Dưới đây là luồng hoàn chỉnh khi **nhân viên check-in cho khách**, thể hiện tất cả 7 kỹ thuật phối hợp:

```
[1] FE gọi: POST /api/v1/table-booking/123/check-in
    body: { force: false }
         │
         ▼
[2] Controller → Service.checkInBooking(123, request)
         │
         ▼
[3] ──── KỸ THUẬT 3: REDIS LOCK ────
    BookingLockService.executeWithTableLock(tableId=8, () -> {
         │
         ▼  Acquire lock:table:8 via Redisson ✓
         │
[4] ──── LOAD DATA ────
    booking = repository.findById(123)  → CONFIRMED
    table = booking.getTable()          → BOOKED (Job A đã reserve trước)
         │
         ▼
[5] ──── KỸ THUẬT 1: STATE MACHINE ────
    StateMachine.transition(BookingTransitionContext.builder()
        .booking(booking)
        .targetStatus(CHECKED_IN)
        .force(false)
        .build()
    )
         │
         ├─ ensureActiveBooking(booking)        → is_active = 1 ✓
         ├─ lockTable(booking)                  → SELECT ... FOR UPDATE ✓
         ├─ isAllowedTransition(CONFIRMED→CHECKED_IN) → ✓
         │
         ▼
[6] ──── KỸ THUẬT 2: VALIDATOR CHAIN ────
    BookingRuleValidatorChain.validate(CHECK_IN, context)
         │
         ├─ EarlyArrivalValidator (order=10):
         │    checkInAt = NOW(), arriveTime = 18:00
         │    NOW < arriveTime - 30p ? → Không (đến đúng giờ) → PASS
         │
         ├─ LateArrivalValidator (order=20):
         │    NOW > arriveTime + 30p ? → Không → PASS
         │
         └─ BookingOwnershipValidator (order=30):
              table BOOKED, nhưng đến đúng giờ → PASS
         │
         ▼
[7] ──── STATE MACHINE SET STATUS ────
    booking.setBookingStatus("CHECKED_IN")
    booking.setCheckInAt(NOW)
         │
         ▼
[8] ──── SAVE TO DB ────
    @Transactional {
        bookingRepository.save(booking)    → CHECKED_IN
        table.setTableStatus("OCCUPIED")
        tableRepository.save(table)        → OCCUPIED
         │
         ▼
[9] ──── KỸ THUẬT 5: DOMAIN EVENT ────
        eventPublisher.publish(CHECK_IN, 123, 8)
        → ApplicationEventPublisher.publishEvent(BookingMutationEvent)
        → Event queued (chưa gửi notify)
    }
    ← Transaction COMMIT ✓
         │
         ▼
[10] ──── KỸ THUẬT 5: AFTER-COMMIT LISTENER ────
    AfterCommitListener.onBookingMutationAfterCommit(event):
         │
         ├─ Re-load booking 123 từ DB → status = CHECKED_IN ✓
         ├─ Re-load table 8 → status = OCCUPIED ✓
         ├─ eventName = "BOOKING_CHECKED_IN"
         │
         ▼
[11] ──── KỸ THUẬT 6: WEBSOCKET PUSH ────
    notificationService.sendOnce("/topic/booking-updates",
        dedupKey = "booking:mutation:booking:CHECK_IN:123:8:2026-...",
        payload = {
            event: "BOOKING_CHECKED_IN",
            mutationType: "CHECK_IN",
            bookingId: 123,
            tableId: 8,
            tableCode: "T08",
            tableStatus: "OCCUPIED",
            bookingStatus: "CHECKED_IN",
            at: "2026-04-10T18:00:05",
            message: "Booking mutation committed: BOOKING_CHECKED_IN"
        }
    )
         │
    notificationService.sendOnce("/topic/table-status",
        dedupKey = "booking:mutation:table:CHECK_IN:123:8:2026-...",
        payload = { event: "TABLE_STATUS_SYNC", tableStatus: "OCCUPIED", ... }
    )
         │
         ▼
[12] ──── RELEASE LOCK ────
    }) ← release lock:table:8 via Redisson
         │
         ▼
[13] ──── KỸ THUẬT 7: RESPONSE ────
    Controller trả về:
    {
      "status": 200,
      "message": "CHECK_IN_SUCCESS",
      "data": { booking details... }
    }
         │
         ▼
[14] FE nhận response + WebSocket event → Update UI:
     - Booking row: status = CHECKED_IN ✓
     - Table board: bàn 8 = OCCUPIED (đổi màu) ✓
     - POS: cho phép order món cho bàn 8 ✓
```

**Kết quả:** 7 kỹ thuật phối hợp chặt chẽ trong 1 luồng duy nhất, đảm bảo:
- ✅ Dữ liệu nhất quán (lock + transaction)
- ✅ Rule nghiệp vụ được enforce (validator chain)
- ✅ Trạng thái đúng logic (state machine)
- ✅ UI cập nhật realtime (domain event + after-commit + websocket)
- ✅ Lỗi trả về rõ ràng (custom exception)

---

> **Ghi chú cuối:**  
> Toàn bộ kỹ thuật trên được thiết kế để **có thể test độc lập**:
> - State Machine: unit test transition hợp lệ / không hợp lệ
> - Validator: unit test từng rule riêng biệt
> - Lock: integration test concurrent access
> - Scheduler: integration test với test data
> - Event/Listener: integration test verify notify chỉ gửi sau commit
> - Exception: test verify HTTP status code + error code
