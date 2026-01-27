package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/citizen")
public class CitizenController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('CITIZEN')")
    public ResponseEntity<String> getDashboard() {
        return ResponseEntity.ok("Hello Citizen! This is your dashboard.");
    }
}
