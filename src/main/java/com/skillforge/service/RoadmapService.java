package com.skillforge.service;

import com.skillforge.dto.RoadmapItemDTO;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.model.*;
import com.skillforge.repository.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RoadmapService {

    private final RoadmapItemRepository roadmapItemRepository;
    private final UserProgressRepository userProgressRepository;
    private final UserSkillRepository userSkillRepository;
    private final RoleRequiredSkillRepository roleRequiredSkillRepository;

    public RoadmapService(
            RoadmapItemRepository roadmapItemRepository,
            UserProgressRepository userProgressRepository,
            UserSkillRepository userSkillRepository,
            RoleRequiredSkillRepository roleRequiredSkillRepository) {
        this.roadmapItemRepository = roadmapItemRepository;
        this.userProgressRepository = userProgressRepository;
        this.userSkillRepository = userSkillRepository;
        this.roleRequiredSkillRepository = roleRequiredSkillRepository;
    }

    public List<RoadmapItemDTO> getGapDrivenRoadmap(User user) {
        CareerRole targetRole = user.getTargetRole();
        if (targetRole == null) {
            return Collections.emptyList();
        }

        List<RoadmapItem> allItems = roadmapItemRepository.findByCareerRoleOrderByOrderIndexAsc(targetRole);
        List<UserSkill> userSkills = userSkillRepository.findByUser(user);
        Set<Integer> userSkillIds = userSkills.stream()
                .map(us -> us.getSkill().getSkillId())
                .collect(Collectors.toSet());

        List<RoleRequiredSkill> requirements = roleRequiredSkillRepository.findByCareerRole(targetRole);
        Map<Integer, SkillPriority> requirementPriorityMap = requirements.stream()
                .collect(Collectors.toMap(r -> r.getSkill().getSkillId(), RoleRequiredSkill::getPriority, (a, b) -> a));

        List<UserProgress> userProgressList = userProgressRepository.findByUser(user);
        Map<Integer, UserProgress> progressMap = userProgressList.stream()
                .collect(Collectors.toMap(up -> up.getRoadmapItem().getRoadmapId(), up -> up, (a, b) -> a));

        // Filter items: return items whose skill is missing (gap) OR items user has recorded progress on
        List<RoadmapItem> filteredItems = allItems.stream()
                .filter(item -> item.getSkill() == null
                        || !userSkillIds.contains(item.getSkill().getSkillId())
                        || progressMap.containsKey(item.getRoadmapId()))
                .collect(Collectors.toList());

        // Sort: CORE skills first, then GOOD_TO_HAVE, then by order_index
        filteredItems.sort((a, b) -> {
            int priorityA = getPriorityRank(a.getSkill(), requirementPriorityMap);
            int priorityB = getPriorityRank(b.getSkill(), requirementPriorityMap);
            if (priorityA != priorityB) {
                return Integer.compare(priorityA, priorityB);
            }
            return Integer.compare(a.getOrderIndex(), b.getOrderIndex());
        });

        List<RoadmapItemDTO> result = new ArrayList<>();
        for (RoadmapItem item : filteredItems) {
            UserProgress progress = progressMap.get(item.getRoadmapId());
            ProgressStatus status = (progress != null) ? progress.getStatus() : ProgressStatus.NOT_STARTED;
            LocalDateTime completedAt = (progress != null) ? progress.getCompletedAt() : null;
            boolean isMissing = item.getSkill() != null && !userSkillIds.contains(item.getSkill().getSkillId());
            SkillPriority priority = (item.getSkill() != null) ? requirementPriorityMap.get(item.getSkill().getSkillId()) : null;

            result.add(new RoadmapItemDTO(item, status, completedAt, isMissing, priority));
        }

        return result;
    }

    private int getPriorityRank(Skill skill, Map<Integer, SkillPriority> priorityMap) {
        if (skill == null) {
            return 3; // No skill link (general milestone) placed after
        }
        SkillPriority p = priorityMap.get(skill.getSkillId());
        if (p == SkillPriority.CORE) return 1;
        if (p == SkillPriority.GOOD_TO_HAVE) return 2;
        return 3;
    }

    @Transactional
    public UserProgress updateProgress(User user, Integer roadmapId, ProgressStatus newStatus) {
        RoadmapItem item = roadmapItemRepository.findById(roadmapId)
                .orElseThrow(() -> new ResourceNotFoundException("Roadmap item not found: " + roadmapId));

        Optional<UserProgress> existing = userProgressRepository.findByUserAndRoadmapItem(user, item);
        UserProgress progress;

        if (existing.isPresent()) {
            progress = existing.get();
        } else {
            progress = new UserProgress();
            progress.setUser(user);
            progress.setRoadmapItem(item);
        }

        progress.setStatus(newStatus);

        if (newStatus == ProgressStatus.COMPLETED) {
            progress.setCompletedAt(LocalDateTime.now());

            // If the item is linked to a skill, add that skill to user_skills as BEGINNER if not already present
            if (item.getSkill() != null) {
                Skill skill = item.getSkill();
                Optional<UserSkill> userSkillOpt = userSkillRepository.findByUserAndSkill(user, skill);
                if (userSkillOpt.isEmpty()) {
                    UserSkill userSkill = new UserSkill();
                    userSkill.setUser(user);
                    userSkill.setSkill(skill);
                    userSkill.setProficiency(SkillProficiency.BEGINNER);
                    userSkillRepository.save(userSkill);
                }
            }
        } else {
            // Moving back from COMPLETED clears completed_at
            progress.setCompletedAt(null);
        }

        return userProgressRepository.save(progress);
    }
}
