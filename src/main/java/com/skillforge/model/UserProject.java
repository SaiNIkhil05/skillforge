package com.skillforge.model;

import jakarta.persistence.*;

@Entity
@Table(name = "user_projects")
public class UserProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "project_id")
    private Integer projectId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", length = 150, nullable = false)
    private String title;

    @Column(name = "tech_used", length = 200, nullable = false)
    private String techUsed;

    @Column(name = "project_link", length = 255)
    private String projectLink;

    public UserProject() {
    }

    public UserProject(Integer projectId, User user, String title, String techUsed, String projectLink) {
        this.projectId = projectId;
        this.user = user;
        this.title = title;
        this.techUsed = techUsed;
        this.projectLink = projectLink;
    }

    public Integer getProjectId() {
        return projectId;
    }

    public void setProjectId(Integer projectId) {
        this.projectId = projectId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getTechUsed() {
        return techUsed;
    }

    public void setTechUsed(String techUsed) {
        this.techUsed = techUsed;
    }

    public String getProjectLink() {
        return projectLink;
    }

    public void setProjectLink(String projectLink) {
        this.projectLink = projectLink;
    }
}
