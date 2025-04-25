//package com.example.forum.Controller;
package tn.esprit.projet_pi.Controller;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tn.esprit.projet_pi.Service.EngagementService;

@RestController
@RequestMapping("/engagement")
public class EngagementController {

    private final EngagementService engagementService;

    @Autowired
    public EngagementController(EngagementService engagementService) {
        this.engagementService = engagementService;
    }

    @GetMapping("/check")
    public ResponseEntity<String> checkEngagement() {
        engagementService.checkPostEngagement();
        return ResponseEntity.ok("Engagement check executed.");
    }
}
