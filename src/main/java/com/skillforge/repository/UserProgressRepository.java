package com.skillforge.repository;

import com.skillforge.model.RoadmapItem;
import com.skillforge.model.User;
import com.skillforge.model.UserProgress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProgressRepository extends JpaRepository<UserProgress, Integer> {
    List<UserProgress> findByUser(User user);
    Optional<UserProgress> findByUserAndRoadmapItem(User user, RoadmapItem roadmapItem);
    Optional<UserProgress> findByUserUserIdAndRoadmapItemRoadmapId(Integer userId, Integer roadmapId);
}
