package com.skillforge.dto;

import com.skillforge.model.SkillCategory;
import com.skillforge.model.SkillProficiency;

public class ParsedSkillDTO {

    private Integer skillId;
    private String skillName;
    private SkillCategory category;
    private SkillProficiency proficiency;
    private double confidence;
    private String snippet;
    private boolean alreadyInProfile;
    private boolean selected = true;

    public ParsedSkillDTO() {
    }

    public ParsedSkillDTO(Integer skillId, String skillName, SkillCategory category,
                          SkillProficiency proficiency, double confidence, String snippet,
                          boolean alreadyInProfile) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
        this.proficiency = proficiency;
        this.confidence = confidence;
        this.snippet = snippet;
        this.alreadyInProfile = alreadyInProfile;
        this.selected = !alreadyInProfile;
    }

    public Integer getSkillId() {
        return skillId;
    }

    public void setSkillId(Integer skillId) {
        this.skillId = skillId;
    }

    public String getSkillName() {
        return skillName;
    }

    public void setSkillName(String skillName) {
        this.skillName = skillName;
    }

    public SkillCategory getCategory() {
        return category;
    }

    public void setCategory(SkillCategory category) {
        this.category = category;
    }

    public SkillProficiency getProficiency() {
        return proficiency;
    }

    public void setProficiency(SkillProficiency proficiency) {
        this.proficiency = proficiency;
    }

    public double getConfidence() {
        return confidence;
    }

    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }

    public String getSnippet() {
        return snippet;
    }

    public void setSnippet(String snippet) {
        this.snippet = snippet;
    }

    public boolean isAlreadyInProfile() {
        return alreadyInProfile;
    }

    public void setAlreadyInProfile(boolean alreadyInProfile) {
        this.alreadyInProfile = alreadyInProfile;
    }

    public boolean isSelected() {
        return selected;
    }

    public void setSelected(boolean selected) {
        this.selected = selected;
    }
}
