package com.skillforge.service;

import com.skillforge.dto.ProjectRequest;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.model.*;
import com.skillforge.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final CareerRoleRepository careerRoleRepository;
    private final SkillRepository skillRepository;
    private final UserSkillRepository userSkillRepository;
    private final UserProjectRepository userProjectRepository;

    public ProfileService(
            UserRepository userRepository,
            CareerRoleRepository careerRoleRepository,
            SkillRepository skillRepository,
            UserSkillRepository userSkillRepository,
            UserProjectRepository userProjectRepository) {
        this.userRepository = userRepository;
        this.careerRoleRepository = careerRoleRepository;
        this.skillRepository = skillRepository;
        this.userSkillRepository = userSkillRepository;
        this.userProjectRepository = userProjectRepository;
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));
    }

    @Transactional
    public void updateEducation(Integer userId, String education) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        user.setEducation(education != null ? education.trim() : "");
        userRepository.save(user);
    }

    @Transactional
    public void setTargetRole(Integer userId, Integer roleId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        CareerRole role = careerRoleRepository.findById(roleId)
                .orElseThrow(() -> new ResourceNotFoundException("Role not found: " + roleId));
        user.setTargetRole(role);
        userRepository.save(user);
    }

    @Transactional
    public UserSkill addOrUpdateUserSkill(Integer userId, Integer skillId, SkillProficiency proficiency) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillId));

        Optional<UserSkill> existing = userSkillRepository.findByUserAndSkill(user, skill);
        UserSkill userSkill;
        if (existing.isPresent()) {
            userSkill = existing.get();
            userSkill.setProficiency(proficiency != null ? proficiency : SkillProficiency.BEGINNER);
        } else {
            userSkill = new UserSkill();
            userSkill.setUser(user);
            userSkill.setSkill(skill);
            userSkill.setProficiency(proficiency != null ? proficiency : SkillProficiency.BEGINNER);
        }
        return userSkillRepository.save(userSkill);
    }

    @Transactional
    public void removeUserSkill(Integer userId, Integer skillId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        Skill skill = skillRepository.findById(skillId)
                .orElseThrow(() -> new ResourceNotFoundException("Skill not found: " + skillId));
        userSkillRepository.deleteByUserAndSkill(user, skill);
    }

    @Transactional
    public UserProject addUserProject(Integer userId, ProjectRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        UserProject project = new UserProject();
        project.setUser(user);
        project.setTitle(request.getTitle().trim());
        project.setTechUsed(request.getTechUsed().trim());
        project.setProjectLink(request.getProjectLink() != null && !request.getProjectLink().isBlank() ? request.getProjectLink().trim() : null);
        return userProjectRepository.save(project);
    }

    @Transactional
    public void removeUserProject(Integer userId, Integer projectId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
        userProjectRepository.findByProjectIdAndUser(projectId, user).ifPresent(userProjectRepository::delete);
    }

    public List<UserSkill> getUserSkills(User user) {
        return userSkillRepository.findByUser(user);
    }

    public List<UserProject> getUserProjects(User user) {
        return userProjectRepository.findByUserOrderByProjectIdDesc(user);
    }
}
