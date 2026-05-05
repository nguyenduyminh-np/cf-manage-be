// AccountServiceImpl.java
package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.AccountSearchNativeResult;
import com.duyminhdev.cf_manager.dto.request.account.*;
import com.duyminhdev.cf_manager.dto.response.account.*;
import com.duyminhdev.cf_manager.entity.*;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.exceptions.DuplicatedUsernameException;
import com.duyminhdev.cf_manager.repository.*;
import com.duyminhdev.cf_manager.repository.native_interface.NativeSqlAccountRepository;
import com.duyminhdev.cf_manager.service.AccountService;
import com.duyminhdev.cf_manager.utils.PageUtils;
import com.duyminhdev.cf_manager.utils.ServiceSupport;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class AccountServiceImpl implements AccountService {

    private final AccountRepository accountRepository;
    private final RoleRepository roleRepository;
    private final NativeSqlAccountRepository nativeSqlAccountRepository;
    private final ServiceSupport serviceSupport;
    private final PasswordEncoder passwordEncoder;
    private final AccountTokenRepository accountTokenRepository; // để revoke token khi khóa

    @Override
    public PageResponse<List<AccountListItemDTO>> search(AccountSearchRequestDTO request) {
        int page = PageUtils.normalizePage(request.getPage());
        int limit = PageUtils.normalizeLimit(request.getLimit());
        int offset = page * limit;

        List<AccountSearchNativeResult> rows = nativeSqlAccountRepository.search(request, offset, limit);
        long total = nativeSqlAccountRepository.count(request);

        List<AccountListItemDTO> list = rows.stream()
                .map(r -> AccountListItemDTO.builder()
                        .id(r.getId())
                        .username(r.getUsername())
                        .fullName(r.getFullName())
                        .email(r.getEmail())
                        .phoneNumber(r.getPhoneNumber())
                        .photo(r.getPhoto())
                        .isActive(r.getIsActive())
                        .roleName(r.getRoleName())
                        .dateOfBirth(r.getDateOfBirth())
                        .createdAt(r.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        PageResponse<List<AccountListItemDTO>> resp = new PageResponse<>();
        resp.setRows(list);
        resp.setPageNo(page);
        resp.setPageSize(limit);
        resp.setTotalElements((int) total);
        resp.setTotalPages((int) Math.ceil((double) total / limit));
        return resp;
    }

    @Override
    public List<AccountExportDTO> exportData(AccountSearchRequestDTO request) {
        long total = nativeSqlAccountRepository.count(request);
        if (total <= 0) return List.of();
        List<AccountSearchNativeResult> rows = nativeSqlAccountRepository.search(request, 0, (int) total);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
                .withZone(ZoneId.systemDefault());

        return rows.stream()
                .map(r -> AccountExportDTO.builder()
                        .username(r.getUsername())
                        .fullName(r.getFullName())
                        .email(r.getEmail() != null ? r.getEmail() : "")
                        .phoneNumber(r.getPhoneNumber() != null ? r.getPhoneNumber() : "")
                        .roleName(r.getRoleName())
                        .isActive(Boolean.TRUE.equals(r.getIsActive()) ? "Hoạt động" : "Khóa")
                        .dateOfBirth(r.getDateOfBirth() != null ? formatter.format(r.getDateOfBirth()) : "")
                        .createdAt(r.getCreatedAt() != null ? formatter.format(r.getCreatedAt()) : "")
                        .build())
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public AccountDetailDTO create(AccountCreateRequestDTO request) {
        String username = request.getUsername().trim();
        String email = StringUtils.hasText(request.getEmail()) ? request.getEmail().trim() : null;

        // Giống flow /register: chặn username trùng trước khi build account
        if (accountRepository.existsByUsername(username)) {
            throw new DuplicatedUsernameException(username);
        }

        // Giống flow /register: account được tạo từ một base builder rồi mới gắn thêm dữ liệu riêng của API này
        if (StringUtils.hasText(email) && accountRepository.existsByEmail(email)) {
            throw new InvalidDataException("Email đã được sử dụng");
        }

        // Riêng API admin create vẫn giữ kiểm tra role theo roleId
        Role role = roleRepository.findById(request.getRoleId())
            .filter(r -> Boolean.TRUE.equals(r.getActive()))
                .orElseThrow(() -> new InvalidDataException("Vai trò không tồn tại hoặc đã bị vô hiệu hóa"));

        Account account = Account.builder()
                .accountCode(StringUtils.hasText(request.getAccountCode()) ? request.getAccountCode().trim() : null)
                .username(username)
                .password(passwordEncoder.encode(request.getPassword()))
                .fullName(request.getFullName().trim())
                .email(email)
                .photo(StringUtils.hasText(request.getPhoto()) ? request.getPhoto().trim() : "images/userdefault.jpg")
                .dob(request.getDateOfBirth())
                .phoneNumber(request.getPhoneNumber())
                .createdTime(Instant.now())
                .active(true)
                .role(role)
                .build();

        account = accountRepository.save(account);

        return mapToDetail(account);
    }

    @Override
    @Transactional
    public AccountDetailDTO update(AccountUpdateRequestDTO request) {
        Account account = accountRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Tài khoản không tồn tại"));

        // Cập nhật các trường nếu có
        if (StringUtils.hasText(request.getUsername())) {
            String newUsername = request.getUsername().trim();
            if (!newUsername.equals(account.getUsername()) && accountRepository.existsByUsername(newUsername)) {
                throw new InvalidDataException("Tên đăng nhập đã tồn tại");
            }
            account.setUsername(newUsername);
        }
        if (request.getPassword() != null) {
            account.setPassword(passwordEncoder.encode(request.getPassword()));
        }
        if (request.getFullName() != null) {
            account.setFullName(request.getFullName().trim());
        }
        if (request.getEmail() != null) {
            String newEmail = request.getEmail().trim();
            if (!newEmail.equals(account.getEmail()) && accountRepository.existsByEmail(newEmail)) {
                throw new InvalidDataException("Email đã được sử dụng");
            }
            account.setEmail(StringUtils.hasText(newEmail) ? newEmail : null);
        }
        if (request.getPhoto() != null) {
            account.setPhoto(request.getPhoto());
        }
        if (request.getDateOfBirth() != null) {
            account.setDob(request.getDateOfBirth());
        }
        if (request.getPhoneNumber() != null) {
            account.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getRoleId() != null) {
            Role role = roleRepository.findById(request.getRoleId())
                    .filter(r -> Boolean.TRUE.equals(r.getActive()))
                    .orElseThrow(() -> new InvalidDataException("Vai trò không tồn tại hoặc đã bị vô hiệu hóa"));
            account.setRole(role);
        }
        if (request.getIsActive() != null) {
            account.setActive(request.getIsActive());
            // Nếu khóa tài khoản (isActive = false) thì revoke tất cả token
            if (!request.getIsActive()) {
                accountTokenRepository.revokeAllByAccountId(account.getId());
            }
        }
        if (request.getAccountCode() != null) {
            account.setAccountCode(request.getAccountCode());
        }

        account = accountRepository.save(account);
        return mapToDetail(account);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Account account = accountRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Tài khoản không tồn tại"));
        if (Boolean.FALSE.equals(account.getActive())) {
            throw new InvalidDataException("Tài khoản đã bị khóa trước đó");
        }
        account.setActive(false);
        accountRepository.save(account);

        // Revoke token
        accountTokenRepository.revokeAllByAccountId(account.getId());
    }

    @Override
    public AccountDetailDTO getDetail(Integer id) {
        AccountSearchNativeResult result = nativeSqlAccountRepository.findDetailById(id)
                .orElseThrow(() -> new InvalidDataException("Tài khoản không tồn tại"));

        return AccountDetailDTO.builder()
                .id(result.getId())
                .username(result.getUsername())
                .fullName(result.getFullName())
                .email(result.getEmail())
                .photo(result.getPhoto())
                .dateOfBirth(result.getDateOfBirth())
                .phoneNumber(result.getPhoneNumber())
                .isActive(result.getIsActive())
                .roleId(result.getRoleId())
                .roleName(result.getRoleName())
                .createdAt(result.getCreatedAt())
                .build();
    }

    private AccountDetailDTO mapToDetail(Account account) {
        return AccountDetailDTO.builder()
                .id(account.getId())
                .accountCode(account.getAccountCode())
                .username(account.getUsername())
                .fullName(account.getFullName())
                .email(account.getEmail())
                .photo(account.getPhoto())
            .dateOfBirth(account.getDob())
                .phoneNumber(account.getPhoneNumber())
            .isActive(account.getActive())
                .roleId(account.getRole() != null ? account.getRole().getId() : null)
                .roleName(account.getRole() != null ? account.getRole().getRoleName() : null)
            .createdAt(account.getCreatedTime())
                .build();
    }
}