package com.skillforge.controller;

import com.skillforge.dto.RequiredSkillAddRequest;
import com.skillforge.dto.RoadmapItemAddRequest;
import com.skillforge.dto.RoleCreateRequest;
import com.skillforge.model.*;
import com.skillforge.service.AdminService;
import com.skillforge.service.ProfileService;
import com.skillforge.service.SkillService;
import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final AdminService adminService;
    private final SkillService skillService;
    private final ProfileService profileService;

    public AdminController(AdminService adminService,
                           SkillService skillService,
                           ProfileService profileService) {
        this.adminService = adminService;
        this.skillService = skillService;
        this.profileService = profileService;
    }

    @ModelAttribute
    public void addAdminAttributes(Model model, Authentication auth) {
        if (auth != null && auth.isAuthenticated()) {
            User user = profileService.getUserByEmail(auth.getName());
            model.addAttribute("adminUser", user);
            model.addAttribute("user", user);
        }
    }

    // ==========================================
    // CAREER ROLES CRUD
    // ==========================================

    @GetMapping("/roles")
    public String rolesList(Model model) {
        model.addAttribute("roles", adminService.getAllRoles());
        model.addAttribute("newRole", new RoleCreateRequest());
        model.addAttribute("totalStudents", adminService.getTotalStudentsCount());
        model.addAttribute("totalSkills", skillService.getTotalSkillsCount());
        return "admin/roles";
    }

    @PostMapping("/roles/create")
    public String createRole(@Valid @ModelAttribute("newRole") RoleCreateRequest request,
                             BindingResult result,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            redirectAttributes.addFlashAttribute("errorMessage", "Role name is required.");
            return "redirect:/admin/roles";
        }
        adminService.createRole(request);
        redirectAttributes.addFlashAttribute("successMessage", "Career role '" + request.getRoleName() + "' created.");
        return "redirect:/admin/roles";
    }

    // ==========================================
    // REQUIRED SKILLS PER ROLE
    // ==========================================

    @GetMapping("/roles/{roleId}/skills")
    public String roleSkills(@PathVariable("roleId") Integer roleId, Model model) {
        CareerRole role = adminService.getRoleById(roleId);
        List<RoleRequiredSkill> requiredSkills = adminService.getRequiredSkillsForRole(roleId);
        List<Skill> allSkills = skillService.getAllSkills();

        model.addAttribute("role", role);
        model.addAttribute("requiredSkills", requiredSkills);
        model.addAttribute("allSkills", allSkills);
        model.addAttribute("priorities", SkillPriority.values());
        model.addAttribute("reqSkillRequest", new RequiredSkillAddRequest());
        return "admin/role-skills";
    }

    @PostMapping("/roles/{roleId}/skills/add")
    public String addRequiredSkill(@PathVariable("roleId") Integer roleId,
                                   @Valid @ModelAttribute("reqSkillRequest") RequiredSkillAddRequest request,
                                   RedirectAttributes redirectAttributes) {
        adminService.addRequiredSkill(roleId, request);
        redirectAttributes.addFlashAttribute("successMessage", "Required skill configured for role.");
        return "redirect:/admin/roles/" + roleId + "/skills";
    }

    @PostMapping("/roles/{roleId}/skills/delete")
    public String deleteRequiredSkill(@PathVariable("roleId") Integer roleId,
                                      @RequestParam("id") Integer reqSkillId,
                                      RedirectAttributes redirectAttributes) {
        adminService.deleteRequiredSkill(reqSkillId);
        redirectAttributes.addFlashAttribute("successMessage", "Required skill removed from role.");
        return "redirect:/admin/roles/" + roleId + "/skills";
    }

    // ==========================================
    // ROADMAP ITEMS PER ROLE
    // ==========================================

    @GetMapping("/roles/{roleId}/roadmap")
    public String roleRoadmap(@PathVariable("roleId") Integer roleId, Model model) {
        CareerRole role = adminService.getRoleById(roleId);
        List<RoadmapItem> items = adminService.getRoadmapItemsForRole(roleId);
        List<Skill> allSkills = skillService.getAllSkills();

        model.addAttribute("role", role);
        model.addAttribute("roadmapItems", items);
        model.addAttribute("allSkills", allSkills);
        model.addAttribute("roadmapRequest", new RoadmapItemAddRequest());
        return "admin/roadmap-items";
    }

    @PostMapping("/roles/{roleId}/roadmap/add")
    public String addRoadmapItem(@PathVariable("roleId") Integer roleId,
                                 @Valid @ModelAttribute("roadmapRequest") RoadmapItemAddRequest request,
                                 RedirectAttributes redirectAttributes) {
        adminService.addRoadmapItem(roleId, request);
        redirectAttributes.addFlashAttribute("successMessage", "Roadmap milestone added to role.");
        return "redirect:/admin/roles/" + roleId + "/roadmap";
    }

    @PostMapping("/roles/{roleId}/roadmap/delete")
    public String deleteRoadmapItem(@PathVariable("roleId") Integer roleId,
                                    @RequestParam("roadmapId") Integer roadmapId,
                                    RedirectAttributes redirectAttributes) {
        adminService.deleteRoadmapItem(roadmapId);
        redirectAttributes.addFlashAttribute("successMessage", "Roadmap milestone removed.");
        return "redirect:/admin/roles/" + roleId + "/roadmap";
    }

    // ==========================================
    // MASTER SKILLS CATALOG CRUD
    // ==========================================

    @GetMapping("/skills")
    public String skillsCatalog(Model model) {
        model.addAttribute("skills", skillService.getAllSkills());
        model.addAttribute("categories", SkillCategory.values());
        return "admin/skills";
    }

    @PostMapping("/skills/create")
    public String createSkill(@RequestParam("skillName") String skillName,
                              @RequestParam("category") SkillCategory category,
                              RedirectAttributes redirectAttributes) {
        try {
            skillService.createSkill(skillName, category);
            redirectAttributes.addFlashAttribute("successMessage", "Skill '" + skillName + "' added to master catalog.");
        } catch (Exception ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }
        return "redirect:/admin/skills";
    }
}
