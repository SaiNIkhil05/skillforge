package com.skillforge.controller;

import com.skillforge.dto.*;
import com.skillforge.model.*;
import com.skillforge.service.*;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class SkillApiController {

    private final SkillService skillService;
    private final ProfileService profileService;
    private final ReadinessScoreService readinessScoreService;
    private final RoadmapService roadmapService;
    private final AdminService adminService;
    private final ResumeParserService resumeParserService;
    private final AdminAnalyticsService adminAnalyticsService;

    public SkillApiController(SkillService skillService,
                              ProfileService profileService,
                              ReadinessScoreService readinessScoreService,
                              RoadmapService roadmapService,
                              AdminService adminService,
                              ResumeParserService resumeParserService,
                              AdminAnalyticsService adminAnalyticsService) {
        this.skillService = skillService;
        this.profileService = profileService;
        this.readinessScoreService = readinessScoreService;
        this.roadmapService = roadmapService;
        this.adminService = adminService;
        this.resumeParserService = resumeParserService;
        this.adminAnalyticsService = adminAnalyticsService;
    }

    // ==========================================
    // SKILLS CATALOG (Search & Filter)
    // ==========================================

    @GetMapping("/skills")
    public ResponseEntity<List<Skill>> getSkills(@RequestParam(value = "category", required = false) SkillCategory category,
                                                 @RequestParam(value = "search", required = false) String search) {
        List<Skill> skills;
        if (category != null) {
            skills = skillService.getSkillsByCategory(category);
        } else {
            skills = skillService.getAllSkills();
        }

        if (search != null && !search.isBlank()) {
            String query = search.trim().toLowerCase();
            skills = skills.stream()
                    .filter(s -> s.getSkillName().toLowerCase().contains(query))
                    .collect(Collectors.toList());
        }

        return ResponseEntity.ok(skills);
    }

    // ==========================================
    // CAREER ROLES (Public or Auth)
    // ==========================================

    @GetMapping("/roles")
    public ResponseEntity<List<CareerRole>> getAllRoles() {
        return ResponseEntity.ok(adminService.getAllRoles());
    }

    @GetMapping("/roles/{roleId}")
    public ResponseEntity<CareerRole> getRoleById(@PathVariable("roleId") Integer roleId) {
        return ResponseEntity.ok(adminService.getRoleById(roleId));
    }

    // ==========================================
    // USER PROFILE ENDPOINTS
    // ==========================================

    @GetMapping("/profile")
    public ResponseEntity<?> getProfile(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }
        User user = profileService.getUserByEmail(auth.getName());
        Map<String, Object> data = new HashMap<>();
        data.put("user", user);
        data.put("skills", profileService.getUserSkills(user));
        data.put("projects", profileService.getUserProjects(user));
        return ResponseEntity.ok(data);
    }

    @PostMapping("/profile/skills")
    public ResponseEntity<?> addUserSkill(@Valid @RequestBody SkillAddRequest request, Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        UserSkill saved = profileService.addOrUpdateUserSkill(user.getUserId(), request.getSkillId(), request.getProficiency());

        ReadinessScoreDTO updatedScore = readinessScoreService.calculateReadiness(user);
        Map<String, Object> resp = new HashMap<>();
        resp.put("userSkill", saved);
        resp.put("readiness", updatedScore);
        return ResponseEntity.ok(resp);
    }

    @DeleteMapping("/profile/skills/{skillId}")
    public ResponseEntity<?> removeUserSkill(@PathVariable("skillId") Integer skillId, Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        profileService.removeUserSkill(user.getUserId(), skillId);

        ReadinessScoreDTO updatedScore = readinessScoreService.calculateReadiness(user);
        Map<String, Object> resp = new HashMap<>();
        resp.put("removedSkillId", skillId);
        resp.put("readiness", updatedScore);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/profile/projects")
    public ResponseEntity<?> addProject(@Valid @RequestBody ProjectRequest request, Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        UserProject project = profileService.addUserProject(user.getUserId(), request);
        return ResponseEntity.ok(project);
    }

    @DeleteMapping("/profile/projects/{projectId}")
    public ResponseEntity<?> removeProject(@PathVariable("projectId") Integer projectId, Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        profileService.removeUserProject(user.getUserId(), projectId);
        return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
    }

    @PostMapping("/profile/target-role")
    public ResponseEntity<?> updateTargetRole(@RequestParam("roleId") Integer roleId, Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        profileService.setTargetRole(user.getUserId(), roleId);

        ReadinessScoreDTO updatedScore = readinessScoreService.calculateReadiness(user);
        return ResponseEntity.ok(updatedScore);
    }

    @PostMapping("/profile/education")
    public ResponseEntity<?> updateEducation(@RequestParam("education") String education, Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        profileService.updateEducation(user.getUserId(), education);
        return ResponseEntity.ok(Map.of("message", "Education updated", "education", education));
    }

    @PostMapping(value = "/profile/upload-resume", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ResumeParseResponse> uploadResume(
            @RequestParam("file") MultipartFile file,
            Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        return ResponseEntity.ok(resumeParserService.parseResume(file, user));
    }

    @PostMapping("/profile/apply-resume-skills")
    public ResponseEntity<?> applyResumeSkills(
            @RequestBody ApplyResumeSkillsRequest request,
            Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        Map<String, Object> result = resumeParserService.applyResumeSkills(user, request.getSkills());
        return ResponseEntity.ok(result);
    }

    // ==========================================
    // READINESS & ROADMAP ENDPOINTS
    // ==========================================

    @GetMapping("/readiness")
    public ResponseEntity<ReadinessScoreDTO> getReadiness(Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        return ResponseEntity.ok(readinessScoreService.calculateReadiness(user));
    }

    @GetMapping("/roadmap")
    public ResponseEntity<List<RoadmapItemDTO>> getRoadmap(Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        return ResponseEntity.ok(roadmapService.getGapDrivenRoadmap(user));
    }

    @PostMapping("/roadmap/update")
    public ResponseEntity<?> toggleRoadmapStatus(@RequestParam("roadmapId") Integer roadmapId,
                                                 @RequestParam("status") ProgressStatus status,
                                                 Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        UserProgress progress = roadmapService.updateProgress(user, roadmapId, status);
        ReadinessScoreDTO updatedScore = readinessScoreService.calculateReadiness(user);

        Map<String, Object> resp = new HashMap<>();
        resp.put("progress", progress);
        resp.put("readiness", updatedScore);
        return ResponseEntity.ok(resp);
    }

    // ==========================================
    // ADMIN ENDPOINTS
    // ==========================================

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/roles")
    public ResponseEntity<CareerRole> createRole(@Valid @RequestBody RoleCreateRequest request) {
        return ResponseEntity.ok(adminService.createRole(request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/roles/{roleId}/skills")
    public ResponseEntity<RoleRequiredSkill> addRequiredSkill(@PathVariable("roleId") Integer roleId,
                                                              @Valid @RequestBody RequiredSkillAddRequest request) {
        return ResponseEntity.ok(adminService.addRequiredSkill(roleId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/roles/skills/{id}")
    public ResponseEntity<?> deleteRequiredSkill(@PathVariable("id") Integer id) {
        adminService.deleteRequiredSkill(id);
        return ResponseEntity.ok(Map.of("message", "Required skill deleted"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/roles/{roleId}/roadmap")
    public ResponseEntity<RoadmapItem> addRoadmapItem(@PathVariable("roleId") Integer roleId,
                                                      @Valid @RequestBody RoadmapItemAddRequest request) {
        return ResponseEntity.ok(adminService.addRoadmapItem(roleId, request));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/roadmap/{id}")
    public ResponseEntity<?> deleteRoadmapItem(@PathVariable("id") Integer id) {
        adminService.deleteRoadmapItem(id);
        return ResponseEntity.ok(Map.of("message", "Roadmap item deleted"));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/admin/skills")
    public ResponseEntity<Skill> createSkill(@RequestParam("skillName") String skillName,
                                             @RequestParam("category") SkillCategory category) {
        return ResponseEntity.ok(skillService.createSkill(skillName, category));
    }

    // ==========================================
    // PLACEMENT ADMIN BATCH ANALYTICS & HEATMAP
    // ==========================================

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/analytics/batch-summary")
    public ResponseEntity<BatchSummaryDTO> getBatchSummary() {
        return ResponseEntity.ok(adminAnalyticsService.getBatchSummary());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/analytics/heatmap")
    public ResponseEntity<List<HeatmapCellDTO>> getHeatmap() {
        return ResponseEntity.ok(adminAnalyticsService.getHeatmapData());
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/analytics/candidates")
    public ResponseEntity<List<CandidateRosterDTO>> getCandidateRoster(
            @RequestParam(value = "minScore", required = false) Integer minScore,
            @RequestParam(value = "roleId", required = false) Integer roleId) {
        return ResponseEntity.ok(adminAnalyticsService.getCandidateRoster(minScore, roleId));
    }

    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping(value = "/admin/export/candidates", produces = "text/csv")
    public ResponseEntity<String> exportCandidates(
            @RequestParam(value = "minScore", required = false) Integer minScore,
            @RequestParam(value = "roleId", required = false) Integer roleId) {
        String csv = adminAnalyticsService.generateCandidatesCsv(minScore, roleId);
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"skillforge_candidate_roster.csv\"")
                .body(csv);
    }
}
