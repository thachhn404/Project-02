package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.enterprise;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.request.EnterpriseFeedbackResolveRequest;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.EnterpriseFeedbackResponse;
import com.team2.Crowdsourced_Waste_Collection_Recycling_System.service.EnterpriseFeedbackService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enterprise/feedbacks")
@RequiredArgsConstructor
@Tag(name = "Enterprise Feedbacks", description = "Quản lý khiếu nại/feedback từ người dân")
public class EnterpriseFeedbackController extends EnterpriseControllerSupport {

    private final EnterpriseFeedbackService enterpriseFeedbackService;

    @GetMapping
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Operation(summary = "Danh sách khiếu nại", description = "Lấy danh sách feedback của enterprise")
    public ResponseEntity<ApiResponse<List<EnterpriseFeedbackResponse>>> getFeedbacks(
            @AuthenticationPrincipal Jwt jwt) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        List<EnterpriseFeedbackResponse> result = enterpriseFeedbackService.getFeedbacks(enterpriseId);
        return okEntity(result, "Lấy danh sách feedback thành công");
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Operation(summary = "Chi tiết khiếu nại", description = "Xem chi tiết feedback và report của collector")
    public ResponseEntity<ApiResponse<EnterpriseFeedbackResponse>> getFeedbackDetail(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        EnterpriseFeedbackResponse result = enterpriseFeedbackService.getFeedbackDetail(enterpriseId, id);
        return okEntity(result, "Lấy chi tiết feedback thành công");
    }

    @PostMapping("/{id}/resolve")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    @Operation(summary = "Giải quyết khiếu nại", description = "Enterprise resolve hoặc reject feedback")
    public ResponseEntity<ApiResponse<Void>> resolveFeedback(
            @AuthenticationPrincipal Jwt jwt,
            @PathVariable Integer id,
            @Valid @RequestBody EnterpriseFeedbackResolveRequest request) {
        Integer enterpriseId = extractEnterpriseId(jwt);
        enterpriseFeedbackService.resolveFeedback(enterpriseId, id, request);
        return okEntity(null, "Giải quyết feedback thành công");
    }
}
