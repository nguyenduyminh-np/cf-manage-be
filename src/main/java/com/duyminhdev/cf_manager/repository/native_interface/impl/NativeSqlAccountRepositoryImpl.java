package com.duyminhdev.cf_manager.repository.native_interface.impl;

import com.duyminhdev.cf_manager.dto.db_result.native_sql.AccountSearchNativeResult;
import com.duyminhdev.cf_manager.dto.request.account.AccountSearchRequestDTO;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlAccountRepository;
import com.duyminhdev.cf_manager.utils.NativeSqlTupleUtils;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.Query;
import jakarta.persistence.Tuple;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.*;

@Repository
public class NativeSqlAccountRepositoryImpl implements NativeSqlAccountRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<AccountSearchNativeResult> search(AccountSearchRequestDTO request, int offset, int limit) {
        StringBuilder sql = new StringBuilder("""
                SELECT
                    a.id              AS id,
                    a.username        AS username,
                    a.full_name       AS fullName,
                    a.email           AS email,
                    a.phone_number    AS phoneNumber,
                    a.photo           AS photo,
                    a.is_active       AS isActive,
                    a.date_of_birth   AS dateOfBirth,
                    a.created_at      AS createdAt,
                    r.id              AS roleId,
                    r.role_name       AS roleName
                FROM account a
                LEFT JOIN role r ON r.id = a.role_id
                WHERE 1 = 1
                """);

        Map<String, Object> params = buildSearchParams(sql, request);

        String sortField = StringUtils.hasText(request.getSortField()) ? request.getSortField() : "createdAt";
        String sortDir = "desc".equalsIgnoreCase(request.getSortDir()) ? "DESC" : "ASC";
        switch (sortField) {
            case "username":
                sql.append(" ORDER BY a.username ").append(sortDir);
                break;
            case "fullName":
                sql.append(" ORDER BY a.full_name ").append(sortDir);
                break;
            case "email":
                sql.append(" ORDER BY a.email ").append(sortDir);
                break;
            case "createdAt":
            default:
                sql.append(" ORDER BY a.created_at ").append(sortDir);
                break;
        }

        sql.append(" LIMIT :limit OFFSET :offset");
        params.put("limit", limit);
        params.put("offset", offset);

        Query query = entityManager.createNativeQuery(sql.toString(), Tuple.class);
        params.forEach(query::setParameter);

        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        return tuples.stream().map(this::mapRow).toList();
    }

    @Override
    public long count(AccountSearchRequestDTO request) {
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM account a
                LEFT JOIN role r ON r.id = a.role_id
                WHERE 1 = 1
                """);
        Map<String, Object> params = buildSearchParams(sql, request);
        Query query = entityManager.createNativeQuery(sql.toString());
        params.forEach(query::setParameter);
        return ((Number) query.getSingleResult()).longValue();
    }

    @Override
    public Optional<AccountSearchNativeResult> findDetailById(Integer id) {
        String sql = """
                SELECT
                    a.id              AS id,
                    a.username        AS username,
                    a.full_name       AS fullName,
                    a.email           AS email,
                    a.phone_number    AS phoneNumber,
                    a.photo           AS photo,
                    a.is_active       AS isActive,
                    a.date_of_birth   AS dateOfBirth,
                    a.created_at      AS createdAt,
                    r.id              AS roleId,
                    r.role_name       AS roleName
                FROM account a
                LEFT JOIN role r ON r.id = a.role_id
                WHERE a.id = :id
                """;
        Query query = entityManager.createNativeQuery(sql, Tuple.class);
        query.setParameter("id", id);
        @SuppressWarnings("unchecked")
        List<Tuple> tuples = query.getResultList();
        if (tuples.isEmpty()) return Optional.empty();
        Tuple tuple = tuples.get(0);
        return Optional.of(mapRow(tuple));
    }

    private AccountSearchNativeResult mapRow(Tuple tuple) {
        return AccountSearchNativeResult.builder()
                .id(NativeSqlTupleUtils.getInteger(tuple, "id"))
                .username(NativeSqlTupleUtils.getString(tuple, "username"))
                .fullName(NativeSqlTupleUtils.getString(tuple, "fullName"))
                .email(NativeSqlTupleUtils.getString(tuple, "email"))
                .phoneNumber(NativeSqlTupleUtils.getString(tuple, "phoneNumber"))
                .photo(NativeSqlTupleUtils.getString(tuple, "photo"))
                .isActive(NativeSqlTupleUtils.getBoolean(tuple, "isActive"))
                .dateOfBirth(NativeSqlTupleUtils.getInstant(tuple, "dateOfBirth"))
                .createdAt(NativeSqlTupleUtils.getInstant(tuple, "createdAt"))
                .roleId(NativeSqlTupleUtils.getInteger(tuple, "roleId"))
                .roleName(NativeSqlTupleUtils.getString(tuple, "roleName"))
                .build();
    }

    private Map<String, Object> buildSearchParams(StringBuilder sql, AccountSearchRequestDTO request) {
        Map<String, Object> params = new HashMap<>();

        if (StringUtils.hasText(request.getUsername())) {
            sql.append(" AND a.username LIKE :username");
            params.put("username", "%" + request.getUsername().trim() + "%");
        }
        if (StringUtils.hasText(request.getFullName())) {
            sql.append(" AND a.full_name LIKE :fullName");
            params.put("fullName", "%" + request.getFullName().trim() + "%");
        }
        if (StringUtils.hasText(request.getEmail())) {
            sql.append(" AND a.email LIKE :email");
            params.put("email", "%" + request.getEmail().trim() + "%");
        }
        if (StringUtils.hasText(request.getPhoneNumber())) {
            sql.append(" AND a.phone_number LIKE :phoneNumber");
            params.put("phoneNumber", "%" + request.getPhoneNumber().trim() + "%");
        }
        if (request.getRoleId() != null) {
            sql.append(" AND a.role_id = :roleId");
            params.put("roleId", request.getRoleId());
        }
        if (request.getIsActive() != null) {
            sql.append(" AND a.is_active = :isActive");
            params.put("isActive", request.getIsActive());
        }
        if (request.getFromBirthDate() != null) {
            sql.append(" AND a.date_of_birth >= :fromBirthDate");
            params.put("fromBirthDate", request.getFromBirthDate());
        }
        if (request.getToBirthDate() != null) {
            sql.append(" AND a.date_of_birth <= :toBirthDate");
            params.put("toBirthDate", request.getToBirthDate());
        }
        return params;
    }
}