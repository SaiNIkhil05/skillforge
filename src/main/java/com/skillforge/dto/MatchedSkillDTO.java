package com.skillforge.dto;

import com.skillforge.model.RoleRequiredSkill;
import com.skillforge.model.SkillProficiency;

public class MatchedSkillDTO {
    private RoleRequiredSkill requirement;
    private SkillProficiency proficiency;
    private double earnedPoints;

    public MatchedSkillDTO() {
    }

    public MatchedSkillDTO(RoleRequiredSkill requirement, SkillProficiency proficiency, double earnedPoints) {
        this.requirement = requirement;
        this.proficiency = proficiency;
        this.earnedPoints = earnedPoints;
    }

    public RoleRequiredSkill getRequirement() {
        return requirement;
    }

    public void setRequirement(RoleRequiredSkill requirement) {
        this.requirement = requirement;
    }

    public SkillProficiency getProficiency() {
        return proficiency;
    }

    public void setProficiency(SkillProficiency proficiency) {
        this.proficiency = proficiency;
    }

    public double getEarnedPoints() {
        return earnedPoints;
    }

    public void setEarnedPoints(double earnedPoints) {
        this.earnedPoints = earnedPoints;
    }
}
