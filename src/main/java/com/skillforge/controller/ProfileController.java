package com.skillforge.controller;

import com.skillforge.dto.ProjectRequest;
import com.skillforge.dto.SkillAddRequest;
import com.skillforge.model.*;
import com.skillforge.repository.CareerRoleRepository;
import com.skillforge.repository.SkillRepository;
import com.skillforge.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final ProfileService profileService;
    private final CareerRoleRepository careerRoleRepository;
    private final SkillRepository skillRepository;

    public ProfileController(ProfileService profileService,
                             CareerRoleRepository careerRoleRepository,
                             SkillRepository skillRepository) {
        this.profileService = profileService;
        this.careerRoleRepository = careerRoleRepository;
        this.skillRepository = skillRepository;
    }

    @GetMapping
    public String profilePage(Model model, Authentication auth) {
        User user = profileService.getUserByEmail(auth.getName());
        model.addAttribute("user", user);
        model.addAttribute("userSkills", profileService.getUserSkills(user));
        model.addAttribute("userProjects", profileService.getUserProjects(user));
        model.addAttribute("careerRoles", careerRoleRepository.findAll());
        model.addAttribute("allSkills", skillRepository.findAllByOrderBySkillNameAsc());
        model.addAttribute("categories", SkillCategory.values());
        model.addAttribute("proficiencies", SkillProficiency.values());
        model.addAttribute("projectRequest", new ProjectRequest());
        return "profile";
    }

    @PostMapping("/education")
    public String updateEducation(@RequestParam("education") String education,
                                  Authentication auth,
                                  RedirectAttributes redirectAttributes) {
        User user = profileService.getUserByEmail(auth.getName());
        profileService.updateEducation(user.getUserId(), education);
        redirectAttributes.addFlashAttribute("successMessage", "Education details updated.");
        return "redirect:/profile";
    }

    @PostMapping("/target-role")
    public String updateTargetRole(@RequestParam("targetRoleId") Integer roleId,
                                   Authentication auth,
                                   RedirectAttributes redirectAttributes) {
        User user = profileService.getUserByEmail(auth.getName());
        profileService.setTargetRole(user.getUserId(), roleId);
        redirectAttributes.addFlashAttribute("successMessage", "Target role updated successfully. Readiness score recalculated.");
        return "redirect:/profile";
    }

    @PostMapping("/skills/add")
    public String addSkill(@RequestParam("skillId") Integer skillId,
                           @RequestParam("proficiency") SkillProficiency proficiency,
                           Authentication auth,
                           RedirectAttributes redirectAttributes) {
        User user = profileService.getUserByEmail(auth.getName());
        profileService.addOrUpdateUserSkill(user.getUserId(), skillId, proficiency);
        redirectAttributes.addFlashAttribute("successMessage", "Skill added to profile.");
        return "redirect:/profile";
    }

    @PostMapping("/skills/remove")
    public String removeSkill(@RequestParam("skillId") Integer skillId,
                              Authentication auth,
                              RedirectAttributes redirectAttributes) {
        User user = profileService.getUserByEmail(auth.getName());
        profileService.removeUserSkill(user.getUserId(), skillId);
        redirectAttributes.addFlashAttribute("successMessage", "Skill removed from profile.");
        return "redirect:/profile";
    }

    @PostMapping("/projects/add")
    public String addProject(@Valid @ModelAttribute("projectRequest") ProjectRequest request,
                             Authentication auth,
                             RedirectAttributes redirectAttributes) {
        User user = profileService.getUserByEmail(auth.getName());
        profileService.addUserProject(user.getUserId(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Project added successfully.");
        return "redirect:/profile";
    }

    @PostMapping("/projects/delete")
    public String deleteProject(@RequestParam("projectId") Integer projectId,
                                Authentication auth,
                                RedirectAttributes redirectAttributes) {
        User user = profileService.getUserByEmail(auth.getName());
        profileService.removeUserProject(user.getUserId(), projectId);
        redirectAttributes.addFlashAttribute("successMessage", "Project deleted.");
        return "redirect:/profile";
    }
}
