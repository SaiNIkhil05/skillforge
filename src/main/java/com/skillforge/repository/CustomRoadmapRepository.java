package com.skillforge.repository;

import com.skillforge.model.CustomRoadmap;
import com.skillforge.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CustomRoadmapRepository extends JpaRepository<CustomRoadmap, Long> {

    List<CustomRoadmap> findByUserOrderByCreatedAtDesc(User user);

    Optional<CustomRoadmap> findByIdAndUser(Long id, User user);

    void deleteByIdAndUser(Long id, User user);
}
