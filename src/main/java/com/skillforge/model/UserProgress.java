package com.skillforge.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_progress", uniqueConstraints = {
    @UniqueConstraint(columnNames = {"user_id", "roadmap_id"})
})
public class UserProgress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "progress_id")
    private Integer progressId;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "roadmap_id", nullable = false)
    private RoadmapItem roadmapItem;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private ProgressStatus status = ProgressStatus.NOT_STARTED;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    public UserProgress() {
    }

    public UserProgress(Integer progressId, User user, RoadmapItem roadmapItem, ProgressStatus status, LocalDateTime completedAt) {
        this.progressId = progressId;
        this.user = user;
        this.roadmapItem = roadmapItem;
        this.status = status;
        this.completedAt = completedAt;
    }

    public Integer getProgressId() {
        return progressId;
    }

    public void setProgressId(Integer progressId) {
        this.progressId = progressId;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public RoadmapItem getRoadmapItem() {
        return roadmapItem;
    }

    public void setRoadmapItem(RoadmapItem roadmapItem) {
        this.roadmapItem = roadmapItem;
    }

    public ProgressStatus getStatus() {
        return status;
    }

    public void setStatus(ProgressStatus status) {
        this.status = status;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }

    public void setCompletedAt(LocalDateTime completedAt) {
        this.completedAt = completedAt;
    }
}
