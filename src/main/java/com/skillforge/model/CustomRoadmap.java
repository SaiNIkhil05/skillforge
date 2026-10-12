package com.skillforge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "custom_ai_roadmaps", indexes = {
    @Index(name = "idx_custom_rm_user", columnList = "user_id")
})
public class CustomRoadmap {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "title", length = 150, nullable = false)
    private String title;

    @Column(name = "tech_stack", length = 255, nullable = false)
    private String techStack;

    @Column(name = "target_level", length = 100)
    private String targetLevel = "Junior to Mid-Level";

    @Column(name = "timeframe_weeks")
    private int timeframeWeeks = 12;

    @Column(name = "learning_focus", length = 100)
    private String learningFocus = "Project-Driven";

    @Column(name = "ai_model_used", length = 100)
    private String aiModelUsed = "Google Gemini 1.5 Flash";

    @Column(name = "total_milestones")
    private int totalMilestones = 0;

    @Column(name = "completed_milestones")
    private int completedMilestones = 0;

    @Lob
    @Column(name = "roadmap_json", columnDefinition = "LONGTEXT", nullable = false)
    private String roadmapJson;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    @PrePersist
    protected void onCreate() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.updatedAt == null) this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    public CustomRoadmap() {
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public String getTechStack() {
        return techStack;
    }

    public void setTechStack(String techStack) {
        this.techStack = techStack;
    }

    public String getTargetLevel() {
        return targetLevel;
    }

    public void setTargetLevel(String targetLevel) {
        this.targetLevel = targetLevel;
    }

    public int getTimeframeWeeks() {
        return timeframeWeeks;
    }

    public void setTimeframeWeeks(int timeframeWeeks) {
        this.timeframeWeeks = timeframeWeeks;
    }

    public String getLearningFocus() {
        return learningFocus;
    }

    public void setLearningFocus(String learningFocus) {
        this.learningFocus = learningFocus;
    }

    public String getAiModelUsed() {
        return aiModelUsed;
    }

    public void setAiModelUsed(String aiModelUsed) {
        this.aiModelUsed = aiModelUsed;
    }

    public int getTotalMilestones() {
        return totalMilestones;
    }

    public void setTotalMilestones(int totalMilestones) {
        this.totalMilestones = totalMilestones;
    }

    public int getCompletedMilestones() {
        return completedMilestones;
    }

    public void setCompletedMilestones(int completedMilestones) {
        this.completedMilestones = completedMilestones;
    }

    public String getRoadmapJson() {
        return roadmapJson;
    }

    public void setRoadmapJson(String roadmapJson) {
        this.roadmapJson = roadmapJson;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
