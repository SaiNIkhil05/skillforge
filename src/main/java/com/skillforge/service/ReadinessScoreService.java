package com.skillforge.service;

import com.skillforge.dto.MatchedSkillDTO;
import com.skillforge.dto.ReadinessScoreDTO;
import com.skillforge.model.*;
import com.skillforge.repository.RoadmapItemRepository;
import com.skillforge.repository.RoleRequiredSkillRepository;
import com.skillforge.repository.UserProgressRepository;
import com.skillforge.repository.UserSkillRepository;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class ReadinessScoreService {

    private final RoleRequiredSkillRepository roleRequiredSkillRepository;
    private final UserSkillRepository userSkillRepository;
    private final RoadmapItemRepository roadmapItemRepository;
    private final UserProgressRepository userProgressRepository;

    public ReadinessScoreService(
            RoleRequiredSkillRepository roleRequiredSkillRepository,
            UserSkillRepository userSkillRepository,
            RoadmapItemRepository roadmapItemRepository,
            UserProgressRepository userProgressRepository) {
        this.roleRequiredSkillRepository = roleRequiredSkillRepository;
        this.userSkillRepository = userSkillRepository;
        this.roadmapItemRepository = roadmapItemRepository;
        this.userProgressRepository = userProgressRepository;
    }

    public ReadinessScoreDTO calculateReadiness(User user) {
        ReadinessScoreDTO dto = new ReadinessScoreDTO();

        CareerRole targetRole = user.getTargetRole();
        if (targetRole == null) {
            dto.setScore(0);
            dto.setStatusMessage("Select a target career role to calculate your readiness score.");
            dto.setRoleName("No target role selected");
            return dto;
        }

        dto.setRoleId(targetRole.getRoleId());
        dto.setRoleName(targetRole.getRoleName());

        List<RoleRequiredSkill> requiredSkills = roleRequiredSkillRepository.findByCareerRole(targetRole);
        List<UserSkill> userSkills = userSkillRepository.findByUser(user);
        List<RoadmapItem> allRoadmapItems = roadmapItemRepository.findByCareerRoleOrderByOrderIndexAsc(targetRole);
        List<UserProgress> userProgressList = userProgressRepository.findByUser(user);

        // Map user skills by skill ID
        Map<Integer, UserSkill> userSkillMap = userSkills.stream()
                .collect(Collectors.toMap(us -> us.getSkill().getSkillId(), us -> us, (a, b) -> a));

        // Count completed roadmap items
        int completedRoadmapCount = (int) userProgressList.stream()
                .filter(up -> up.getStatus() == ProgressStatus.COMPLETED)
                .count();

        dto.setTotalRequiredCount(requiredSkills.size());
        dto.setTotalRoadmapCount(allRoadmapItems.size());
        dto.setCompletedRoadmapCount(completedRoadmapCount);

        int totalWeight = 0;
        double earnedWeight = 0;
        int acquiredCount = 0;

        List<RoleRequiredSkill> missingCore = new ArrayList<>();
        List<RoleRequiredSkill> missingGoodToHave = new ArrayList<>();
        List<MatchedSkillDTO> matchedSkills = new ArrayList<>();

        for (RoleRequiredSkill req : requiredSkills) {
            int weight = req.getWeight();
            totalWeight += weight;

            Integer skillId = req.getSkill().getSkillId();
            if (userSkillMap.containsKey(skillId)) {
                acquiredCount++;
                UserSkill us = userSkillMap.get(skillId);
                SkillProficiency proficiency = us.getProficiency();
                double multiplier = ProficiencyMultipliers.getMultiplier(proficiency);
                double points = weight * multiplier;
                earnedWeight += points;
                matchedSkills.add(new MatchedSkillDTO(req, proficiency, Math.round(points * 10.0) / 10.0));
            } else {
                if (req.getPriority() == SkillPriority.CORE) {
                    missingCore.add(req);
                } else {
                    missingGoodToHave.add(req);
                }
            }
        }

        dto.setTotalWeight(totalWeight);
        dto.setEarnedWeight(Math.round(earnedWeight * 10.0) / 10.0);
        dto.setAcquiredCount(acquiredCount);
        dto.setMissingCoreCount(missingCore.size());
        dto.setMissingGoodToHaveCount(missingGoodToHave.size());
        dto.setMissingCoreSkills(missingCore);
        dto.setMissingGoodToHaveSkills(missingGoodToHave);
        dto.setMatchedSkills(matchedSkills);

        int finalScore = 0;
        if (totalWeight > 0) {
            finalScore = (int) Math.round((earnedWeight / (double) totalWeight) * 100);
            finalScore = Math.min(100, Math.max(0, finalScore));
        }
        dto.setScore(finalScore);

        // Human, specific copy as requested in Section 9
        if (missingCore.isEmpty()) {
            if (missingGoodToHave.isEmpty()) {
                dto.setStatusMessage("You have met 100% of required competencies for " + targetRole.getRoleName() + ".");
            } else {
                dto.setStatusMessage("All core skills met. You have " + missingGoodToHave.size() + " good-to-have skill gaps remaining for " + targetRole.getRoleName() + ".");
            }
        } else {
            dto.setStatusMessage("You're missing " + missingCore.size() + " core skills for " + targetRole.getRoleName() + ".");
        }

        return dto;
    }
}
