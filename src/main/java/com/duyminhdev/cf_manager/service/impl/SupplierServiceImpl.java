package com.duyminhdev.cf_manager.service.impl;

import com.duyminhdev.cf_manager.dto.base.PageResponse;
import com.duyminhdev.cf_manager.dto.request.supplier.*;
import com.duyminhdev.cf_manager.dto.response.supplier.*;
import com.duyminhdev.cf_manager.entity.Supplier;
import com.duyminhdev.cf_manager.exceptions.InvalidDataException;
import com.duyminhdev.cf_manager.repository.SupplierRepository;
import com.duyminhdev.cf_manager.repository.spec.SupplierSpec;
import com.duyminhdev.cf_manager.service.SupplierService;
import com.duyminhdev.cf_manager.utils.PageUtils;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SupplierServiceImpl implements SupplierService {

    private final SupplierRepository supplierRepository;

    @Override
    public PageResponse<List<SupplierListItemDTO>> search(SupplierSearchRequestDTO request) {
        SupplierSearchRequestDTO safeRequest = request != null ? request : new SupplierSearchRequestDTO();

        int pageNo = PageUtils.normalizePage(safeRequest.getPage());
        int pageSize = PageUtils.normalizeLimit(safeRequest.getLimit());

        Pageable pageable = PageRequest.of(
            pageNo,
            pageSize,
            SupplierSpec.resolveSort(safeRequest.getSortField(), safeRequest.getSortDir())
        );

        org.springframework.data.jpa.domain.Specification<Supplier> spec = SupplierSpec.byCriteria(safeRequest);
        Page<Supplier> page = supplierRepository.findAll(spec, pageable);

        List<SupplierListItemDTO> rows = page.getContent().stream()
                .map(this::mapToListItem)
                .collect(Collectors.toList());

        PageResponse<List<SupplierListItemDTO>> response = new PageResponse<>();
        response.setRows(rows);
        response.setPageNo(page.getNumber());
        response.setPageSize(page.getSize());
        response.setTotalElements((int) page.getTotalElements());
        response.setTotalPages(page.getTotalPages());
        return response;
    }

        @Override
        public List<SupplierExportDTO> exportData(SupplierSearchRequestDTO request) {
        SupplierSearchRequestDTO safeRequest = request != null ? request : new SupplierSearchRequestDTO();

        org.springframework.data.jpa.domain.Specification<Supplier> spec = SupplierSpec.byCriteria(safeRequest);
        List<Supplier> suppliers = supplierRepository.findAll(spec, SupplierSpec.resolveSort(safeRequest.getSortField(), safeRequest.getSortDir()));

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss")
            .withZone(ZoneId.systemDefault());

        return suppliers.stream()
            .map(supplier -> SupplierExportDTO.builder()
                .supplierCode(supplier.getSupplierCode())
                .supplierName(supplier.getSupplierName())
                .contactInfo(supplier.getContactInfo())
                .address(supplier.getAddress())
                .createdTime(supplier.getCreatedTime() != null ? formatter.format(supplier.getCreatedTime()) : null)
                .active(Boolean.TRUE.equals(supplier.getActive()) ? "Hoạt động" : "Không hoạt động")
                .build())
            .collect(Collectors.toList());
        }

    @Override
    @Transactional
    public SupplierDetailResponseDTO create(SupplierCreateRequestDTO request) {
        Supplier supplier = new Supplier();
        supplier.setSupplierCode(request.getSupplierCode());
        supplier.setSupplierName(request.getSupplierName());
        supplier.setContactInfo(request.getContactInfo());
        supplier.setAddress(request.getAddress());
        supplier.setCreatedTime(Instant.now());
        supplier.setActive(true);

        supplier = supplierRepository.save(supplier);
        return mapToDetail(supplier);
    }

    @Override
    @Transactional
    public SupplierDetailResponseDTO update(SupplierUpdateRequestDTO request) {
        Supplier supplier = supplierRepository.findById(request.getId())
                .orElseThrow(() -> new InvalidDataException("Nhà cung cấp không tồn tại"));

        if (request.getSupplierCode() != null) {
            supplier.setSupplierCode(request.getSupplierCode());
        }
        if (request.getSupplierName() != null && !request.getSupplierName().isBlank()) {
            supplier.setSupplierName(request.getSupplierName());
        }
        if (request.getContactInfo() != null) {
            supplier.setContactInfo(request.getContactInfo());
        }
        if (request.getAddress() != null) {
            supplier.setAddress(request.getAddress());
        }
        if (request.getActive() != null) {
            supplier.setActive(request.getActive());
        }

        supplier = supplierRepository.save(supplier);
        return mapToDetail(supplier);
    }

    @Override
    @Transactional
    public void delete(Integer id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Nhà cung cấp không tồn tại"));
        supplier.setActive(false);
        supplierRepository.save(supplier);
    }

    @Override
    public SupplierDetailResponseDTO getDetail(Integer id) {
        Supplier supplier = supplierRepository.findById(id)
                .orElseThrow(() -> new InvalidDataException("Nhà cung cấp không tồn tại"));
        return mapToDetail(supplier);
    }

    @Override
    public List<SupplierOptionDTO> getOptions() {
        List<Supplier> suppliers = supplierRepository.findAllByActiveTrueOrderBySupplierNameAsc();
        return suppliers.stream()
                .map(s -> SupplierOptionDTO.builder()
                        .id(s.getId())
                        .supplierCode(s.getSupplierCode())
                        .supplierName(s.getSupplierName())
                        .build())
                .collect(Collectors.toList());
    }

    private SupplierListItemDTO mapToListItem(Supplier s) {
        return SupplierListItemDTO.builder()
                .id(s.getId())
                .supplierCode(s.getSupplierCode())
                .supplierName(s.getSupplierName())
                .contactInfo(s.getContactInfo())
                .address(s.getAddress())
                .createdTime(s.getCreatedTime())
                .active(s.getActive())
                .build();
    }

    private SupplierDetailResponseDTO mapToDetail(Supplier s) {
        return SupplierDetailResponseDTO.builder()
                .id(s.getId())
                .supplierCode(s.getSupplierCode())
                .supplierName(s.getSupplierName())
                .contactInfo(s.getContactInfo())
                .address(s.getAddress())
                .createdTime(s.getCreatedTime())
                .active(s.getActive())
                .build();
    }
}