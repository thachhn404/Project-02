package com.team2.Crowdsourced_Waste_Collection_Recycling_System.service;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.UpdateWasteCategoryRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.WasteCategoryRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.WasteCategoryAdminResponse;

import java.util.List;

public interface WasteCategoryService {

    List<WasteCategoryAdminResponse> getAllCategories();

    WasteCategoryAdminResponse getCategory(Integer id);

    WasteCategoryAdminResponse createCategory(WasteCategoryRequest request);

    WasteCategoryAdminResponse updateCategory(Integer id, UpdateWasteCategoryRequest request);

    void deleteCategory(Integer id);
}
