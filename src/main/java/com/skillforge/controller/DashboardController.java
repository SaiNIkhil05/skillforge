package com.skillforge.controller;

import com.skillforge.dto.ReadinessScoreDTO;
import com.skillforge.dto.RoadmapItemDTO;
import com.skillforge.model.CareerRole;
import com.skillforge.model.User;
import com.skillforge.model.UserRole;
import com.skillforge.repository.CareerRoleRepository;
import com.skillforge.service.ProfileService;
import com.skillforge.service.ReadinessScoreService;
import com.skillforge.service.RoadmapService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
public class DashboardController {

    private final ProfileService profileService;
    private final ReadinessScoreService readinessScoreService;
    private final RoadmapService roadmapService;
    private final CareerRoleRepository careerRoleRepository;

    public DashboardController(ProfileService profileService,
                               ReadinessScoreService readinessScoreService,
                               RoadmapService roadmapService,
                               CareerRoleRepository careerRoleRepository) {
        this.profileService = profileService;
        this.readinessScoreService = readinessScoreService;
        this.roadmapService = roadmapService;
        this.careerRoleRepository = careerRoleRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model, Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return "redirect:/login";
        }

        User user = profileService.getUserByEmail(auth.getName());
        if (user.getRole() == UserRole.ADMIN) {
            return "redirect:/admin/roles";
        }

        model.addAttribute("user", user);
        CareerRole targetRole = user.getTargetRole();
        model.addAttribute("targetRole", targetRole);

        if (targetRole != null) {
            ReadinessScoreDTO readiness = readinessScoreService.calculateReadiness(user);
            model.addAttribute("readiness", readiness);

            List<RoadmapItemDTO> roadmapItems = roadmapService.getGapDrivenRoadmap(user);
            model.addAttribute("roadmapPreview", roadmapItems.stream().limit(5).toList());
            model.addAttribute("totalRoadmapItems", roadmapItems.size());
        } else {
            List<CareerRole> availableRoles = careerRoleRepository.findAll();
            model.addAttribute("availableRoles", availableRoles);
        }

        model.addAttribute("skillsCount", profileService.getUserSkills(user).size());
        model.addAttribute("projectsCount", profileService.getUserProjects(user).size());

        return "dashboard";
    }

    @GetMapping("/api/dashboard")
    @ResponseBody
    public ResponseEntity<?> apiDashboard(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            return ResponseEntity.status(401).body(Map.of("error", "Unauthorized"));
        }

        User user = profileService.getUserByEmail(auth.getName());
        Map<String, Object> data = new HashMap<>();
        data.put("userId", user.getUserId());
        data.put("fullName", user.getFullName());
        data.put("email", user.getEmail());
        data.put("education", user.getEducation());
        data.put("role", user.getRole().name());

        if (user.getTargetRole() != null) {
            data.put("targetRoleId", user.getTargetRole().getRoleId());
            data.put("targetRoleName", user.getTargetRole().getRoleName());
            data.put("targetRoleDescription", user.getTargetRole().getDescription());

            ReadinessScoreDTO readiness = readinessScoreService.calculateReadiness(user);
            data.put("readiness", readiness);

            List<RoadmapItemDTO> roadmapItems = roadmapService.getGapDrivenRoadmap(user);
            data.put("roadmapPreview", roadmapItems.stream().limit(5).toList());
            data.put("totalRoadmapCount", roadmapItems.size());
        } else {
            data.put("targetRoleId", null);
            data.put("targetRoleName", null);
            data.put("availableRoles", careerRoleRepository.findAll());
        }

        data.put("skillsCount", profileService.getUserSkills(user).size());
        data.put("projectsCount", profileService.getUserProjects(user).size());

        return ResponseEntity.ok(data);
    }
}
