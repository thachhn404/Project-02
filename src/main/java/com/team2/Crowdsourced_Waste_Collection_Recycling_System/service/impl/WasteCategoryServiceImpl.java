package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.impl;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.UpdateWasteCategoryRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.WasteCategoryRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteCategoryAdminResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.entity.WasteCategory;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.AppException;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception.ErrorCode;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.repository.waste.WasteCategoryRepository;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.WasteCategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class WasteCategoryServiceImpl implements WasteCategoryService {

    private final WasteCategoryRepository wasteCategoryRepository;

    @Override
    public List<WasteCategoryAdminResponse> getAllCategories() {
        return wasteCategoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Override
    public WasteCategoryAdminResponse getCategory(Integer id) {
        WasteCategory category = wasteCategoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WASTE_CATEGORY_NOT_FOUND));
        return toResponse(category);
    }

    @Override
    @Transactional
    public WasteCategoryAdminResponse createCategory(WasteCategoryRequest request) {
        wasteCategoryRepository.findByNameIgnoreCase(request.getName().trim()).ifPresent(existing -> {
            throw new AppException(ErrorCode.WASTE_CATEGORY_NAME_EXISTED);
        });

        WasteCategory category = new WasteCategory();
        category.setName(request.getName().trim());
        category.setDescription(request.getDescription());
        category.setUnit(request.getUnit());
        category.setPointPerUnit(request.getPointPerUnit());
        category.setCreatedAt(LocalDateTime.now());
        category.setUpdatedAt(LocalDateTime.now());

        return toResponse(wasteCategoryRepository.save(category));
    }

    @Override
    @Transactional
    public WasteCategoryAdminResponse updateCategory(Integer id, UpdateWasteCategoryRequest request) {
        WasteCategory category = wasteCategoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WASTE_CATEGORY_NOT_FOUND));

        if (request.getName() != null && !request.getName().isBlank()) {
            String newName = request.getName().trim();
            if (!newName.equalsIgnoreCase(category.getName())) {
                wasteCategoryRepository.findByNameIgnoreCase(newName).ifPresent(existing -> {
                    throw new AppException(ErrorCode.WASTE_CATEGORY_NAME_EXISTED);
                });
            }
            category.setName(newName);
        }

        if (request.getDescription() != null) {
            category.setDescription(request.getDescription());
        }

        if (request.getUnit() != null) {
            category.setUnit(request.getUnit());
        }

        if (request.getPointPerUnit() != null) {
            category.setPointPerUnit(request.getPointPerUnit());
        }

        category.setUpdatedAt(LocalDateTime.now());

        return toResponse(wasteCategoryRepository.save(category));
    }

    @Override
    @Transactional
    public void deleteCategory(Integer id) {
        WasteCategory category = wasteCategoryRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.WASTE_CATEGORY_NOT_FOUND));
        wasteCategoryRepository.delete(category);
    }

    private WasteCategoryAdminResponse toResponse(WasteCategory category) {
        return WasteCategoryAdminResponse.builder()
                .id(category.getId())
                .name(category.getName())
                .description(category.getDescription())
                .unit(category.getUnit() != null ? category.getUnit().name() : null)
                .pointPerUnit(category.getPointPerUnit())
                .createdAt(category.getCreatedAt())
                .updatedAt(category.getUpdatedAt())
                .build();
    }
}
