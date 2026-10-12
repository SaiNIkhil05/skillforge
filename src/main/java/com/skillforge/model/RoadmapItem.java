package com.skillforge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

@Entity
@Table(name = "roadmap_items")
public class RoadmapItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "roadmap_id")
    private Integer roadmapId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "role_id", nullable = false)
    private CareerRole careerRole;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "skill_id")
    private Skill skill;

    @Column(name = "title", length = 150, nullable = false)
    private String title;

    @Column(name = "order_index", nullable = false)
    private Integer orderIndex;

    public RoadmapItem() {
    }

    public RoadmapItem(Integer roadmapId, CareerRole careerRole, Skill skill, String title, Integer orderIndex) {
        this.roadmapId = roadmapId;
        this.careerRole = careerRole;
        this.skill = skill;
        this.title = title;
        this.orderIndex = orderIndex;
    }

    public Integer getRoadmapId() {
        return roadmapId;
    }

    public void setRoadmapId(Integer roadmapId) {
        this.roadmapId = roadmapId;
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

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(Integer orderIndex) {
        this.orderIndex = orderIndex;
    }
}
