package com.skillforge.dto;

import com.skillforge.model.ProgressStatus;
import com.skillforge.model.RoadmapItem;
import com.skillforge.model.SkillPriority;

import java.time.LocalDateTime;

public class RoadmapItemDTO {
    private RoadmapItem item;
    private Integer roadmapId;
    private String title;
    private Integer orderIndex;
    private Integer skillId;
    private String skillName;
    private ProgressStatus status = ProgressStatus.NOT_STARTED;
    private LocalDateTime completedAt;
    private boolean isMissingSkillForTargetRole;
    private SkillPriority priority;

    public RoadmapItemDTO() {
    }

    public RoadmapItemDTO(RoadmapItem item, ProgressStatus status, LocalDateTime completedAt, boolean isMissingSkillForTargetRole, SkillPriority priority) {
        this.item = item;
        if (item != null) {
            this.roadmapId = item.getRoadmapId();
            this.title = item.getTitle();
            this.orderIndex = item.getOrderIndex();
            if (item.getSkill() != null) {
                this.skillId = item.getSkill().getSkillId();
                this.skillName = item.getSkill().getSkillName();
            }
        }
        this.status = status;
        this.completedAt = completedAt;
        this.isMissingSkillForTargetRole = isMissingSkillForTargetRole;
        this.priority = priority;
    }

    public RoadmapItem getItem() {
        return item;
    }

    public void setItem(RoadmapItem item) {
        this.item = item;
        if (item != null) {
            this.roadmapId = item.getRoadmapId();
            this.title = item.getTitle();
            this.orderIndex = item.getOrderIndex();
            if (item.getSkill() != null) {
                this.skillId = item.getSkill().getSkillId();
                this.skillName = item.getSkill().getSkillName();
            }
        }
    }

    public Integer getRoadmapId() {
        return roadmapId != null ? roadmapId : (item != null ? item.getRoadmapId() : null);
    }

    public void setRoadmapId(Integer roadmapId) {
        this.roadmapId = roadmapId;
    }

    public String getTitle() {
        return title != null ? title : (item != null ? item.getTitle() : null);
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getOrderIndex() {
        return orderIndex != null ? orderIndex : (item != null ? item.getOrderIndex() : null);
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }

    public Integer getSkillId() {
        return skillId != null ? skillId : (item != null && item.getSkill() != null ? item.getSkill().getSkillId() : null);
    }

    public void setSkillId(Integer skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName != null ? skillName : (item != null && item.getSkill() != null ? item.getSkill().getSkillName() : null);
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public void setStatus(ProgressStatus status) {
        this.status = status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }

    public boolean isMissingSkillForTargetRole() {
        return isMissingSkillForTargetRole;
    }

    public void setMissingSkillForTargetRole(boolean missingSkillForTargetRole) {
        isMissingSkillForTargetRole = missingSkillForTargetRole;
    }

    public SkillPriority getPriority() {
        return priority;
    }

    public void setPriority(SkillPriority priority) {
        this.priority = priority;
    }
}
