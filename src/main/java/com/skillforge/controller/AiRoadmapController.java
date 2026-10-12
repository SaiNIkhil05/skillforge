package com.skillforge.controller;

import com.skillforge.dto.*;
import com.skillforge.model.CustomRoadmap;
import com.skillforge.model.ProgressStatus;
import com.skillforge.model.User;
import com.skillforge.service.AiRoadmapService;
import com.skillforge.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/roadmap/ai")
public class AiRoadmapController {

    private final AiRoadmapService aiRoadmapService;
    private final ProfileService profileService;

    public AiRoadmapController(AiRoadmapService aiRoadmapService, ProfileService profileService) {
        this.aiRoadmapService = aiRoadmapService;
        this.profileService = profileService;
    }

    /**
     * Interactive conversational AI chatbot endpoint (like ChatGPT / Gemini).
     * Chats naturally about technology and generates roadmaps on demand.
     */
    @PostMapping("/chat")
    public ResponseEntity<ChatResponseDTO> chatWithAi(
            @RequestBody ChatRequestDTO request,
            Authentication auth) {
        User user = null;
        if (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser")) {
            user = profileService.getUserByEmail(auth.getName());
        }

        ChatResponseDTO response = aiRoadmapService.chatWithAi(user, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Dynamically generates an AI-powered technical roadmap tailored to the requested tech stack,
     * timeframe, and student's profile competencies.
     */
    @PostMapping("/generate")
    public ResponseEntity<AiRoadmapResponse> generateRoadmap(
            @Valid @RequestBody GenerateRoadmapRequest request,
            Authentication auth) {
        User user = null;
        if (auth != null && auth.isAuthenticated() && !auth.getName().equalsIgnoreCase("anonymousUser")) {
            user = profileService.getUserByEmail(auth.getName());
        }

        AiRoadmapResponse response = aiRoadmapService.generateRoadmap(user, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Saves a generated AI roadmap to the authenticated user's portfolio.
     */
    @PostMapping("/save")
    public ResponseEntity<CustomRoadmap> saveRoadmap(
            @RequestBody AiRoadmapResponse roadmapResponse,
            Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        CustomRoadmap saved = aiRoadmapService.saveRoadmap(user, roadmapResponse);
        return ResponseEntity.ok(saved);
    }

    /**
     * Retrieves all saved custom roadmaps for the authenticated user.
     */
    @GetMapping("/saved")
    public ResponseEntity<List<CustomRoadmap>> getSavedRoadmaps(Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        List<CustomRoadmap> roadmaps = aiRoadmapService.getUserRoadmaps(user);
        return ResponseEntity.ok(roadmaps);
    }

    /**
     * Retrieves a single saved roadmap by ID.
     */
    @GetMapping("/saved/{id}")
    public ResponseEntity<CustomRoadmap> getSavedRoadmap(
            @PathVariable("id") Long id,
            Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        CustomRoadmap roadmap = aiRoadmapService.getRoadmapById(id, user);
        return ResponseEntity.ok(roadmap);
    }

    /**
     * Updates an individual milestone's progress status (NOT_STARTED, IN_PROGRESS, COMPLETED)
     * and automatically recalculates overall completion metrics.
     */
    @PostMapping("/{id}/milestones/{milestoneId}/status")
    public ResponseEntity<CustomRoadmap> updateMilestoneStatus(
            @PathVariable("id") Long id,
            @PathVariable("milestoneId") String milestoneId,
            @RequestParam("status") ProgressStatus status,
            Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        CustomRoadmap updated = aiRoadmapService.updateMilestoneStatus(user, id, milestoneId, status);
        return ResponseEntity.ok(updated);
    }

    /**
     * Deletes a saved custom roadmap.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteRoadmap(
            @PathVariable("id") Long id,
            Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        aiRoadmapService.deleteRoadmap(user, id);
        return ResponseEntity.ok(Map.of("message", "Roadmap removed successfully", "id", id));
    }
}
