package com.skillforge.repository;

import com.skillforge.model.CareerRole;
import com.skillforge.model.RoadmapItem;
import com.skillforge.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface RoadmapItemRepository extends JpaRepository<RoadmapItem, Integer> {
    List<RoadmapItem> findByCareerRoleOrderByOrderIndexAsc(CareerRole careerRole);
    List<RoadmapItem> findByCareerRoleRoleIdOrderByOrderIndexAsc(Integer roleId);
    List<RoadmapItem> findBySkill(Skill skill);
}
