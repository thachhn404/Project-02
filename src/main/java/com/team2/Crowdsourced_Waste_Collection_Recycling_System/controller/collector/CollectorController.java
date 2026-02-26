package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.collector;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

@RestController
@RequestMapping("/api/collector")
@Tag(name = "Collector", description = "Endpoint dành cho người thu gom")
public class CollectorController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('COLLECTOR')")
    @Operation(summary = "Trang tổng quan Collector", description = "Thông tin tổng quan nhanh cho Collector")
    public ResponseEntity<String> getDashboard() {
        return ResponseEntity.ok("Hello Collector! This is your dashboard.");
    }
}
