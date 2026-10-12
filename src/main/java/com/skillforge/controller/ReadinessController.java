package com.skillforge.controller;

import com.skillforge.dto.ReadinessScoreDTO;
import com.skillforge.dto.RoadmapItemDTO;
import com.skillforge.model.User;
import com.skillforge.service.ProfileService;
import com.skillforge.service.ReadinessScoreService;
import com.skillforge.service.RoadmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/readiness")
public class ReadinessController {

    private final ProfileService profileService;
    private final ReadinessScoreService readinessScoreService;
    private final RoadmapService roadmapService;

    public ReadinessController(ProfileService profileService,
                               ReadinessScoreService readinessScoreService,
                               RoadmapService roadmapService) {
        this.profileService = profileService;
        this.readinessScoreService = readinessScoreService;
        this.roadmapService = roadmapService;
    }

    @GetMapping
    public String readinessPage(Model model, Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        model.addAttribute("user", user);

        ReadinessScoreDTO readiness = readinessScoreService.calculateReadiness(user);
        model.addAttribute("readiness", readiness);

        List<RoadmapItemDTO> roadmap = roadmapService.getGapDrivenRoadmap(user);
        model.addAttribute("roadmap", roadmap);

        return "readiness";
    }

    @GetMapping("/api")
    @ResponseBody
    public ResponseEntity<?> apiReadiness(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        User user = profileService.getUserByEmail(auth.getName());
        ReadinessScoreDTO readiness = readinessScoreService.calculateReadiness(user);
        return ResponseEntity.ok(readiness);
    }
}
