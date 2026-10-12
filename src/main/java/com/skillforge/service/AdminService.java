package com.skillforge.service;

import com.skillforge.dto.RequiredSkillAddRequest;
import com.skillforge.dto.RoadmapItemAddRequest;
import com.skillforge.dto.RoleCreateRequest;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.model.*;
import com.skillforge.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class AdminService {

    private final CareerRoleRepository careerRoleRepository;
    private final SkillRepository skillRepository;
    private final RoleRequiredSkillRepository roleRequiredSkillRepository;
    private final RoadmapItemRepository roadmapItemRepository;
    private final UserRepository userRepository;

    public AdminService(
            CareerRoleRepository careerRoleRepository,
            SkillRepository skillRepository,
            RoleRequiredSkillRepository roleRequiredSkillRepository,
            RoadmapItemRepository roadmapItemRepository,
            UserRepository userRepository) {
        this.careerRoleRepository = careerRoleRepository;
        this.skillRepository = skillRepository;
        this.roleRequiredSkillRepository = roleRequiredSkillRepository;
        this.roadmapItemRepository = roadmapItemRepository;
        this.userRepository = userRepository;
    }

    public List<CareerRole> getAllRoles() {
        return careerRoleRepository.findAll();
    }

    public CareerRole getRoleById(Integer id) {
        return careerRoleRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Career role not found: " + id));
    }

    @Transactional
    public CareerRole createRole(RoleCreateRequest request) {
        CareerRole role = new CareerRole();
        role.setRoleName(request.getRoleName().trim());
        role.setDescription(request.getDescription() != null ? request.getDescription().trim() : "");
        return careerRoleRepository.save(role);
    }

    @Transactional
    public RoleRequiredSkill addRequiredSkill(Integer roleId, RequiredSkillAddRequest request) {
        CareerRole role = getRoleById(roleId);
        Skill skill = skillRepository.findById(request.getSkillId())
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + request.getSkillId()));

        return roleRequiredSkillRepository.findByCareerRoleAndSkill(role, skill)
                .map(existing -> {
                    existing.setPriority(request.getPriority());
                    existing.setWeight(request.getWeight());
                    return roleRequiredSkillRepository.save(existing);
                })
                .orElseGet(() -> {
                    RoleRequiredSkill rrs = new RoleRequiredSkill();
                    rrs.setCareerRole(role);
                    rrs.setSkill(skill);
                    rrs.setPriority(request.getPriority());
                    rrs.setWeight(request.getWeight());
                    return roleRequiredSkillRepository.save(rrs);
                });
    }

    @Transactional
    public void deleteRequiredSkill(Integer reqId) {
        roleRequiredSkillRepository.deleteById(reqId);
    }

    @Transactional
    public RoadmapItem addRoadmapItem(Integer roleId, RoadmapItemAddRequest request) {
        CareerRole role = getRoleById(roleId);
        Skill skill = (request.getSkillId() != null)
                ? skillRepository.findById(request.getSkillId()).orElse(null)
                : null;

        RoadmapItem item = new RoadmapItem();
        item.setCareerRole(role);
        item.setSkill(skill);
        item.setTitle(request.getTitle().trim());
        item.setOrderIndex(request.getOrderIndex());
        return roadmapItemRepository.save(item);
    }

    @Transactional
    public void deleteRoadmapItem(Integer roadmapId) {
        roadmapItemRepository.deleteById(roadmapId);
    }

    public List<RoleRequiredSkill> getRequiredSkillsForRole(Integer roleId) {
        return roleRequiredSkillRepository.findByCareerRoleRoleId(roleId);
    }

    public List<RoadmapItem> getRoadmapItemsForRole(Integer roleId) {
        return roadmapItemRepository.findByCareerRoleRoleIdOrderByOrderIndexAsc(roleId);
    }

    public long getTotalRolesCount() {
        return careerRoleRepository.count();
    }

    public long getTotalStudentsCount() {
        return userRepository.findAll().stream().filter(u -> u.getRole() == UserRole.STUDENT).count();
    }
}
