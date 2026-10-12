package com.skillforge.dto;

import com.skillforge.model.SkillPriority;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class RequiredSkillAddRequest {

    @NotNull(message = "Skill ID is required")
    private Integer skillId;

    @NotNull(message = "Priority is required")
    private SkillPriority priority = SkillPriority.CORE;

    @NotNull(message = "Weight is required")
    @Min(value = 1, message = "Weight must be at least 1")
    private Integer weight = 10;

    public RequiredSkillAddRequest() {
    }

    public RequiredSkillAddRequest(Integer skillId, SkillPriority priority, Integer weight) {
        this.skillId = skillId;
        this.priority = priority;
        this.weight = weight;
    }

    public Integer getSkillId() {
        return skillId;
    }

    public void setSkillId(Integer skillId) {
        this.skillId = skillId;
    }

    public SkillPriority getPriority() {
        return priority;
    }

    public void setPriority(SkillPriority priority) {
        this.priority = priority;
    }

    public Integer getWeight() {
        return weight;
    }

    public void setWeight(Integer weight) {
        this.weight = weight;
    }
}
