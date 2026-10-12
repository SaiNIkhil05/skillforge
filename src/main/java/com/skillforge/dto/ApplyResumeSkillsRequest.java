package com.skillforge.dto;

import com.skillforge.model.SkillProficiency;
import java.util.ArrayList;
import java.util.List;

public class ApplyResumeSkillsRequest {

    public static class SkillItem {
        private Integer skillId;
        private SkillProficiency proficiency = SkillProficiency.BEGINNER;

        public SkillItem() {
        }

        public SkillItem(Integer skillId, SkillProficiency proficiency) {
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

    private List<SkillItem> skills = new ArrayList<>();

    public ApplyResumeSkillsRequest() {
    }

    public List<SkillItem> getSkills() {
        return skills;
    }

    public void setSkills(List<SkillItem> skills) {
        this.skills = skills;
    }
}
