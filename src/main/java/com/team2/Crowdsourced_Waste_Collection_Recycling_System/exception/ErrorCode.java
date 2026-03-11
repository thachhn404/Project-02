package com.team2.Crowdsourced_Waste_Collection_Recycling_System.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import lombok.Getter;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    INVALID_KEY(1001, "Uncategorized error", HttpStatus.BAD_REQUEST),
    USER_EXISTED(1002, "User existed", HttpStatus.BAD_REQUEST),
    USERNAME_INVALID(1003, "Username must be at least 3 characters", HttpStatus.BAD_REQUEST),
    INVALID_PASSWORD(1004, "Password must be at least 8 characters", HttpStatus.BAD_REQUEST),
    USER_NOT_EXISTED(1005, "User not existed", HttpStatus.NOT_FOUND),
    UNAUTHENTICATED(1006, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    UNAUTHORIZED(1007, "You do not have permission", HttpStatus.FORBIDDEN),
    INVALID_DOB(1008, "Your age must be at least {min}", HttpStatus.BAD_REQUEST),
    WASTE_TYPE_NOT_FOUND(1009, "Waste type not found", HttpStatus.BAD_REQUEST),
    DAILY_REPORT_LIMIT_EXCEEDED(1010, "Daily report limit exceeded (5 reports/day)", HttpStatus.TOO_MANY_REQUESTS),
    CITIZEN_NOT_FOUND(1011, "Citizen profile not found", HttpStatus.NOT_FOUND),
    IMAGE_UPLOAD_FAILED(1012, "Failed to upload image", HttpStatus.INTERNAL_SERVER_ERROR),
    WASTE_REPORT_NOT_FOUND(1013, "Waste report not found", HttpStatus.NOT_FOUND),
    INVALID_REQUEST(1014, "Invalid request", HttpStatus.BAD_REQUEST),
    ROLE_NOT_FOUND(1015, "Role not found", HttpStatus.NOT_FOUND),
    USER_ALREADY_SUSPENDED(1016, "User is already suspended", HttpStatus.BAD_REQUEST),
    USER_ALREADY_ACTIVE(1017, "User is already active", HttpStatus.BAD_REQUEST),
    CANNOT_SUSPEND_SELF(1018, "Admin cannot suspend themselves", HttpStatus.FORBIDDEN),
    ENTERPRISE_REQUIRED_FOR_COLLECTOR(1019, "Enterprise ID is required when creating a COLLECTOR account",
            HttpStatus.BAD_REQUEST),
    USER_SUSPENDED(1020, "Your account has been suspended", HttpStatus.FORBIDDEN),
    COLLECTION_REQUEST_NOT_FOUND(1021, "Collection request not found", HttpStatus.NOT_FOUND),
    COLLECTOR_NOT_FOUND(1022, "Collector not found", HttpStatus.NOT_FOUND),
    LOCATION_TOO_FAR(1023, "Location is too far from report location", HttpStatus.BAD_REQUEST),
    VOUCHER_NOT_FOUND(1024, "Voucher not found", HttpStatus.NOT_FOUND),
    VOUCHER_INACTIVE(1025, "Voucher is inactive", HttpStatus.BAD_REQUEST),
    VOUCHER_EXPIRED(1026, "Voucher is expired", HttpStatus.BAD_REQUEST),
    VOUCHER_OUT_OF_STOCK(1027, "Voucher is out of stock", HttpStatus.BAD_REQUEST),
    INSUFFICIENT_POINTS(1028, "Insufficient points", HttpStatus.BAD_REQUEST),
    WASTE_CATEGORY_NOT_FOUND(1029, "Waste category not found", HttpStatus.NOT_FOUND),
    WASTE_CATEGORY_NAME_EXISTED(1030, "Waste category name already exists", HttpStatus.BAD_REQUEST),
    ;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }

    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;
}
