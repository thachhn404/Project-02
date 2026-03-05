package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.enterprise;

import com.team2.Crowdsourced_Waste_Collection_Recycling_System.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.server.ResponseStatusException;

abstract class EnterpriseControllerSupport {
    protected Integer extractEnterpriseId(Jwt jwt) {
        return extractEnterpriseId(jwt, "User hiện tại không phải Enterprise");
    }

    protected Integer extractEnterpriseId(Jwt jwt, String forbiddenMessage) {
        if (jwt == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Thiếu token");
        }
        Object value = jwt.getClaims().get("enterpriseId");
        if (value == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, forbiddenMessage);
        }
        if (value instanceof Number number) {
            return number.intValue();
        }
        throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "enterpriseId không hợp lệ");
    }

    protected static <T> ApiResponse<T> ok(T result) {
        return ApiResponse.<T>builder().result(result).build();
    }

    protected static <T> ApiResponse<T> ok(T result, String message) {
        return ApiResponse.<T>builder().result(result).message(message).build();
    }

    protected static ApiResponse<Void> ok(String message) {
        return ApiResponse.<Void>builder().message(message).build();
    }

    protected static <T> ResponseEntity<ApiResponse<T>> okEntity(T result, String message) {
        return ResponseEntity.ok(ok(result, message));
    }
}

