package com.skillforge.dto;

import com.skillforge.model.SkillProficiency;
import jakarta.validation.constraints.NotNull;

public class SkillAddRequest {

    @NotNull(message = "Skill ID is required")
    private Integer skillId;

    private SkillProficiency proficiency = SkillProficiency.BEGINNER;

    public SkillAddRequest() {
    }

    public SkillAddRequest(Integer skillId, SkillProficiency proficiency) {
        this.skillId = skillId;
        this.proficiency = proficiency;
    }

    public Integer getSkillId() {
        return skillId;
    }

    public void setSkillId(Integer skillId) {
        this.skillId = skillId;
    }

    public SkillProficiency getProficiency() {
        return proficiency;
    }

    public void setProficiency(SkillProficiency proficiency) {
        this.proficiency = proficiency;
    }
}
