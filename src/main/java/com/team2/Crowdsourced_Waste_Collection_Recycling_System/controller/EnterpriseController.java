package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/enterprise")
public class EnterpriseController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasAnyRole('ENTERPRISE', 'ENTERPRISE_ADMIN')")
    public ResponseEntity<String> getDashboard() {
        return ResponseEntity.ok("Hello Enterprise! This is your dashboard.");
    }
}
