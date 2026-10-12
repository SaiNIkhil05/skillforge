package com.skillforge.model;

import jakarta.persistence.*;

@Entity
@Table(name = "role_required_skills")
public class RoleRequiredSkill {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "role_id", nullable = false)
    private CareerRole careerRole;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id", nullable = false)
    private Skill skill;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false)
    private SkillPriority priority;

    @Column(name = "weight", nullable = false)
    private Integer weight;

    public RoleRequiredSkill() {
    }

    public RoleRequiredSkill(Integer id, CareerRole careerRole, Skill skill, SkillPriority priority, Integer weight) {
        this.id = id;
        this.careerRole = careerRole;
        this.skill = skill;
        this.priority = priority;
        this.weight = weight;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public CareerRole getCareerRole() {
        return careerRole;
    }

    public void setCareerRole(CareerRole careerRole) {
        this.careerRole = careerRole;
    }

    public Skill getSkill() {
        return skill;
    }

    public void setSkill(Skill skill) {
        this.skill = skill;
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
