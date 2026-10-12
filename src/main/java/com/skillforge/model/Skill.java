package com.skillforge.model;

import jakarta.persistence.*;

@Entity
@Table(name = "skills")
public class Skill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "skill_id")
    private Integer skillId;

    @Column(name = "skill_name", length = 100, nullable = false)
    private String skillName;

    @Enumerated(EnumType.STRING)
    @Column(name = "category", nullable = false)
    private SkillCategory category;

    public Skill() {
    }

    public Skill(Integer skillId, String skillName, SkillCategory category) {
        this.skillId = skillId;
        this.skillName = skillName;
        this.category = category;
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
}
