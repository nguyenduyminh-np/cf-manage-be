# Technical Document: JPA Criteria Strategy for Dynamic API Search
> **Objective**: Analyze the `/jpa-criteria-search` implementation for optimal LLM context parsing. This pattern relies on `EntityManager.getCriteriaBuilder()` for object-oriented query construction, contrasting with raw Native SQL.

---

## 1. DTO Structure & Security Boundaries
**File:** `ParaPaymentChannelCriteriaDTO.java`

The ingress point enforces strict input protection via `jakarta.validation.constraints`. This offloads type/boundary checking from the database layer to the application layer.

```java
// Snippet: Pagination and Sort boundaries
@Min(value = 0, message = "pageNo must be >= 0")
private int pageNo = 0;

@Min(value = 1, message = "pageSize must be >= 1")
@Max(value = 100, message = "pageSize must be <= 100") // Prevents memory exhaustion attacks
private int pageSize = 20;

@Pattern(regexp = ValidateValueConstants.SORT_FIELD, message = "Invalid sortField")
private String sortField; // Regex sanitization to prevent format exploitation

@Pattern(regexp = ValidateValueConstants.SORT_DIR, message = "sortDir must be asc or desc")
private String sortDir;

// Dynamic filter fields...
private String paymentChannel;
private Map<String,Object> jsonData; 
```
**LLM parsing note:** The `jsonData` is received as an Object Map, but how it's queried differentiates this JPA pattern from the Native SQL pattern.

---

## 2. Core Mechanisms (with Code Citations)
**File:** `JpaCriteriaParaPaymentRepository.java`

### 2.1 Object-Oriented Predicate Construction
Instead of manually concatenating `StringBuilder`, dynamic conditions are added to a `List<Predicate>`, deferring parameter binding to Hibernate/JPA safely.

```java
// Snippet: Safe escaping and matching
List<Predicate> predicates = new ArrayList<>();

if (StringUtils.hasText(criteria.getPaymentChannel())) {
    // 1. Manually escape wildcards (\, %, _) to prevent malicious matching scopes
    String escapedValue = escapeLikeSpecialCharacters(criteria.getPaymentChannel()).toLowerCase() + "%";
    
    // 2. Map to strongly-typed Root attributes
    predicates.add(cb.like(cb.lower(root.get("paymentChannel")), escapedValue));
}
```

### 2.2 JSON Data Handling (Text-Based `LIKE` Strategy)
**Critical Difference:** Because JPA Criteria lacks native vendor-agnostic functions for JSON node traversal (like `JSON_VALUE`), the system serializes the `Map<String,Object>` input back into a plain String and performs a substring/like search against the CLOB/VARCHAR column.

```java
// Snippet: JSON Fallback Strategy
if (criteria.getJsonData() != null && !criteria.getJsonData().isEmpty()) {
    // Convert Map -> String
    String jsonStr = objectMapper.writeValueAsString(criteria.getJsonData());
    
    // Perform loose text search (escaped)
    String escapedValue = escapeLikeSpecialCharacters(jsonStr).toLowerCase() + "%";
    predicates.add(cb.like(cb.lower(root.get("jsonData")), escapedValue));
}
```
> [!WARNING]
> This text-based `LIKE` matching on JSON means searching for `"status":"Active"` might accidentally match unrelated substrings in complex nested JSONs. For exact key-value JSON matching, the Native SQL `JSON_VALUE` strategy (seen in `/native-search`) is strictly superior.

### 2.3 Sorting Security (Strict Target Whitelist)
Validating sort directions is not enough. The `sortField` mapping must be checked against an internal constant Set to prevent `PropertyReferenceException` crashing the API if a random property is requested.

```java
// Snippet: Set-based Whitelisting
private static final Set<String> ALLOWED_FIELDS = Set.of(
    "id", "paymentChannel", "connectionName", /* ... */ "jsonData"
);

if (StringUtils.hasLength(sortField) && ALLOWED_FIELDS.contains(sortField)) {
    // Asc/Desc resolution here
    query.orderBy("asc".equalsIgnoreCase(sortDir) ? 
        criteriaBuilder.asc(root.get(sortField)) : criteriaBuilder.desc(root.get(sortField)));
} else {
    // Fallback order (Secures against unauthorized or empty sort requests)
    query.orderBy(criteriaBuilder.desc(root.get("id")));
}
```

### 2.4 Decoupled Pagination (Shared Predicates)
Similar to Native SQL, the repository avoids Spring Data's implicit `Pageable` proxy routing by constructing two distinct explicit queries, reusing the generated `Predicate` list.

```java
// Query 1: Data Fetching
List<ParaPaymentChannel> list = entityManager.createQuery(query)
    .setFirstResult(offset)
    .setMaxResults(pageSize)
    .getResultList();

// Query 2: Aggregate Count 
// Re-applies the same root and predicate, but overrides the SELECT clause with count()
query.select(criteriaBuilder.count(root)).where(predicate);
Long totalElements = entityManager.createQuery(query).getSingleResult();
```

---

## 3. Recommended Prompts/Rules when Extending this code

If an LLM or Developer needs to append a new filter to this `/jpa-criteria-search` API:
1. **Update the DTO**: Add the property to `ParaPaymentChannelCriteriaDTO`.
2. **Update the Validation**: Ensure it has appropriate `@Constraints` over data boundaries.
3. **Update the Repository Whitelist**: If it's sortable, explicitly add `"newField"` to the `ALLOWED_FIELDS` constant `Set`.
4. **Append the Predicate**: In `createSearchPredicate`, add a standard `if (criteria.getNewField() != null)` block and utilize the correct CriteriaBuilder abstraction (`cb.equal()`, `cb.greaterThan()`, etc).
