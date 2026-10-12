package com.skillforge.controller;

import com.skillforge.dto.ReadinessScoreDTO;
import com.skillforge.dto.RoadmapItemDTO;
import com.skillforge.model.ProgressStatus;
import com.skillforge.model.User;
import com.skillforge.model.UserProgress;
import com.skillforge.service.ProfileService;
import com.skillforge.service.ReadinessScoreService;
import com.skillforge.service.RoadmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/roadmap")
public class RoadmapController {

    private final ProfileService profileService;
    private final RoadmapService roadmapService;
    private final ReadinessScoreService readinessScoreService;

    public RoadmapController(ProfileService profileService,
                             RoadmapService roadmapService,
                             ReadinessScoreService readinessScoreService) {
        this.profileService = profileService;
        this.roadmapService = roadmapService;
        this.readinessScoreService = readinessScoreService;
    }

    @GetMapping
    public String roadmapPage(Model model, Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        model.addAttribute("user", user);

        List<RoadmapItemDTO> items = roadmapService.getGapDrivenRoadmap(user);
        model.addAttribute("roadmapItems", items);

        ReadinessScoreDTO readiness = readinessScoreService.calculateReadiness(user);
        model.addAttribute("readiness", readiness);

        return "roadmap";
    }

    @PostMapping("/update")
    @ResponseBody
    public ResponseEntity<?> updateRoadmapStatus(@RequestParam("roadmapId") Integer roadmapId,
                                                @RequestParam("status") ProgressStatus status,
                                                Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        User user = profileService.getUserByEmail(auth.getName());
        UserProgress progress = roadmapService.updateProgress(user, roadmapId, status);

        // Recalculate score immediately
        ReadinessScoreDTO updatedScore = readinessScoreService.calculateReadiness(user);

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("progressId", progress.getProgressId());
        response.put("status", progress.getStatus().name());
        response.put("completedAt", progress.getCompletedAt());
        response.put("updatedScore", updatedScore.getScore());
        response.put("statusMessage", updatedScore.getStatusMessage());
        response.put("missingCoreCount", updatedScore.getMissingCoreCount());
        response.put("missingGoodToHaveCount", updatedScore.getMissingGoodToHaveCount());

        return ResponseEntity.ok(response);
    }
}
