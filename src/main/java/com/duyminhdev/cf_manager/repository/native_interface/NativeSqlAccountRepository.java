// NativeSqlAccountRepository.java
package com.duyminhdev.cf_manager.repository.native_interface;

import com.duyminhdev.cf_manager.dto.request.account.AccountSearchRequestDTO;
import com.duyminhdev.cf_manager.dto.db_result.native_sql.AccountSearchNativeResult;

import java.util.List;
import java.util.Optional;

public interface NativeSqlAccountRepository {
    List<AccountSearchNativeResult> search(AccountSearchRequestDTO request, int offset, int limit);
    long count(AccountSearchRequestDTO request);
    Optional<AccountSearchNativeResult> findDetailById(Integer id);
}