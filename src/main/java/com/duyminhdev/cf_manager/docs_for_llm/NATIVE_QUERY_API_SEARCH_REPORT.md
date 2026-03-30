# Technical Document: Native SQL Strategy for Dynamic API Search
> **Objective**: Analyze the implementation of Dynamic API Search using Native SQL in Spring Boot (`EntityManager`) for optimal LLM context parsing and comprehension.

This document serves as a standard reference for AI/LLM models to understand the pattern codebase for `NativeSQLParapaymentRepository.java`.

---

## 1. Architectural Model & Data Flow

**Pattern**: `Controller` ➝ `Service` ➝ `Custom Native Repository` ➝ `EntityManager (JPA)`

1. **Input**: `ParaPaymentChannelCriteriaDTO` containing dynamic filter fields, alongside pagination (`pageNo`, `pageSize`) and sorting (`sortField`, `sortDir`).
2. **Processing (`NativeSQLParapaymentRepository`)**: 
   - Dynamically constructs a base `SELECT` string and a `WHERE` clause.
   - Pushes parameter values into a `Map<String, Object>` for safe variable binding.
   - Executes two queries:
     - The Data Query: Uses `Tuple.class` projection + `.setFirstResult()` & `.setMaxResults()`.
     - The Count Query: Uses `COUNT(1)` without heavy `SELECT` subqueries (optimized).
3. **Output**: A constructed `PageResponse<List<NativePaymentChannelResult>>` returned to the Service layer for final DTO mapping.

---

## 2. Core Implementation Mechanisms (with Code Citations)

### 2.1. Parameterized Dynamic Query Building
To absolutely prevent **SQL Injection** while dynamically appending rules, a `StringBuilder` is used with parameterized placeholders `:paramName`. Parameters are stored in a map and bound later.

```java
// Snippet from NativeSQLParapaymentRepository.java -> buildWhere()
StringBuilder sql = new StringBuilder();
Map<String, Object> params = new HashMap<>();

if (hasText(criteria.getPaymentChannel())) {
    // Note: escapeLike() replaces '\', '%', '_' safely before wildcard matching.
    sql.append(" AND LOWER(p.PAYMENT_CHANNEL) LIKE :payment_channel ESCAPE '\\\\' ");
    params.put("payment_channel", escapeLike(criteria.getPaymentChannel().toLowerCase()) + "%");
}

if (criteria.getChannelStatus() != null) {
    sql.append(" AND p.CHANNEL_STATUS = :channelStatus ");
    params.put("channelStatus", criteria.getChannelStatus());
}
```
**LLM parsing note**: `escapeLike` is critical for Native `LIKE` queries to avoid malicious wildcard execution.

### 2.2. Handling JSON Data natively
When querying internal properties inside a JSON String/CLOB column, native DB functions like `JSON_VALUE` are used. Because JSON paths cannot be bound using prepared statements (e.g., `$.?`), the JSON Keys must be aggressively sanitized to prevent injection.

```java
// Snippet from NativeSQLParapaymentRepository.java -> buildWhere()
if (criteria.getJsonData() != null && !criteria.getJsonData().isEmpty()) {
    Map<String, Object> jsonMap = criteria.getJsonData();
    int idx = 0;
    String safePattern = "^[a-zA-Z0-9_]+$"; // 1. Strict Regex Whitelisting

    for (Map.Entry<String, Object> entry : jsonMap.entrySet()) {
        String key = entry.getKey();
        if (!key.matches(safePattern)) {
            throw new IllegalArgumentException("Invalid JSON Key detected: " + key);
        }
        
        String paramName = "jsonVal_" + idx;
        
        // 2. Safe concatenation of the JSON path
        sql.append(" AND JSON_VALUE(p.JSON_DATA, '$.")
           .append(key)
           .append("') = :")
           .append(paramName)
           .append(" ");
           
        // 3. Bind the value safely
        params.put(paramName, entry.getValue().toString());
        idx++;
    }
}
```

### 2.3. Safe ORDER BY with Whitelist Mapping
Since `ORDER BY` columns cannot be parameterized (using `:param`), directly concatenating user-input `sortField` is an injection vector. The implementation maps the API string representation to exact internal Database Column strings via `switch` expression.

```java
// Snippet from NativeSQLParapaymentRepository.java -> mapSortFieldToDbColumn()
private String mapSortFieldToDbColumn(String fieldName) {
    return switch (fieldName) {
        case "id" -> "p.ID";
        case "paymentChannel" -> "p.PAYMENT_CHANNEL";
        case "currencyName" -> "currencyName"; // Aliased scalar subquery
        case "activeStatus" -> "p.ACTIVE_STATUS";
        default -> null; // Blocks undefined columns
    };
}
```

### 2.4. Decoupled Pagination & Counting
Unlike JPA Interface projection, Native Query must handle counting explicitly. The implementation creates two native queries from the same `WHERE` condition. 

```java
// 1. Data Query Processing
Query query = em.createNativeQuery(sql.toString(), Tuple.class);
params.forEach(query::setParameter);

int offset = pageNo * pageSize;
query.setFirstResult(offset);
query.setMaxResults(pageSize);

// 2. Optimized Count Query 
// Uses "FROM_WHERE" directly, bypassing the heavy "SELECT_COLUMNS" (which contains scalar subqueries)
public long countTotalElements(ParaPaymentChannelCriteriaDTO criteria) {
    StringBuilder sql = new StringBuilder("SELECT COUNT(1) " + FROM_WHERE);
    Map<String, Object> params = new HashMap<>();

    sql.append(buildWhere(criteria, params));

    Query query = em.createNativeQuery(sql.toString());
    params.forEach(query::setParameter);

    return ((Number) query.getSingleResult()).longValue();
}
```

### 2.5. Data Mapping using JPA `Tuple`
Rather than resolving `Object[]` dynamically by index (which causes bugs if SQL select order shifts), JPA's `Tuple` lets developers map the result natively using aliases provided in the SQL `SELECT` string.

```java
// Snippet from NativeSQLParapaymentRepository.java -> mapTupleToDto()
private NativePaymentChannelResult mapTupleToDto(Tuple t) {
    NativePaymentChannelResult dto = new NativePaymentChannelResult();

    // Map by column alias
    dto.setId(getLong(t, "id")); 
    dto.setPaymentChannel((String) t.get("paymentChannel"));
    dto.setCurrencyName((String) t.get("currencyName")); // Mapped from Scalar Subquery
    
    return dto;
}
```

---

## 3. Recommended Prompts/Rules when Extending this code

If an LLM or Developer is tasked to ADD A NEW FIELD to this API, strictly follow this procedure:
1. **Update `SELECT_COLUMNS`**: Add the alias `p.NEW_COLUMN AS newColumn`.
2. **Update `buildWhere`**: If it's a filter, add `if(criteria.getNewColumn() != null) { ... }`. Register the parameter in the `params` hashmap.
3. **Update `mapSortFieldToDbColumn`**: Add the sort case `case "newColumn" -> "p.NEW_COLUMN";`.
4. **Update `mapTupleToDto`**: Call `dto.setNewColumn((Type) t.get("newColumn"));` using helper parsing methods like `getLong`/`getInteger`.
5. **DO NOT** use `.setParameter()` indices (e.g., `?1`). Always use named parameters `:paramName`.
6. **DO NOT** concatenate variable values directly into `sql.append()`.

> [!NOTE] 
> System optimizations explicitly avoid `JOIN`ing the whole translation table (`PARA_CURRENCY_RATE`) to keep `COUNT(1)` extremely light. Instead, `currencyName` relies on a scalar subquery `(SELECT cr.CURRENCY_NAME ... ROWNUM = 1) AS currencyName`. Do not break this optimization.
