// AccountService.java
package com.duyminhdev.cf_manager.service;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.account.*;
import com.duyminhdev.cf_manager.dto.response.account.*;

import java.util.List;

public interface AccountService {
    PageResponse<List<AccountListItemDTO>> search(AccountSearchRequestDTO request);
    List<AccountExportDTO> exportData(AccountSearchRequestDTO request);
    AccountDetailDTO create(AccountCreateRequestDTO request);
    AccountDetailDTO update(AccountUpdateRequestDTO request);
    void delete(Integer id);
    AccountDetailDTO getDetail(Integer id);
}