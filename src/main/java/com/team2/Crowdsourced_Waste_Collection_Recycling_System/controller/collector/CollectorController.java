package com.team2.Crowdsourced_Waste_Collection_Recycling_System.controller.collector;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/collector")
public class CollectorController {

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('COLLECTOR')")
    public ResponseEntity<String> getDashboard() {
        return ResponseEntity.ok("Hello Collector! This is your dashboard.");
    }
}
