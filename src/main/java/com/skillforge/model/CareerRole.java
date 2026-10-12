package com.skillforge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "career_roles")
public class CareerRole {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "role_id")
    private Integer roleId;

    @Column(name = "role_name", length = 100, nullable = false)
    private String roleName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @JsonIgnore
    @OneToMany(mappedBy = "careerRole", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<RoleRequiredSkill> requiredSkills = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "careerRole", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    @OrderBy("orderIndex ASC")
    private List<RoadmapItem> roadmapItems = new ArrayList<>();

    public CareerRole() {
    }

    public CareerRole(Integer roleId, String roleName, String description) {
        this.roleId = roleId;
        this.roleName = roleName;
        this.description = description;
    }

    public Integer getRoleId() {
        return roleId;
    }

    public void setRoleId(Integer roleId) {
        this.roleId = roleId;
    }

    public String getRoleName() {
        return roleName;
    }

    public void setRoleName(String roleName) {
        this.roleName = roleName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public List<RoleRequiredSkill> getRequiredSkills() {
        return requiredSkills;
    }

    public void setRequiredSkills(List<RoleRequiredSkill> requiredSkills) {
        this.requiredSkills = requiredSkills;
    }

    public List<RoadmapItem> getRoadmapItems() {
        return roadmapItems;
    }

    public void setRoadmapItems(List<RoadmapItem> roadmapItems) {
        this.roadmapItems = roadmapItems;
    }
}
