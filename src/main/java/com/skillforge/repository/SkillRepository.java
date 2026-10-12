package com.skillforge.repository;

import com.skillforge.model.Skill;
import com.skillforge.model.SkillCategory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface SkillRepository extends JpaRepository<Skill, Integer> {
    List<Skill> findByCategory(SkillCategory category);
    Optional<Skill> findBySkillNameIgnoreCase(String skillName);
    List<Skill> findAllByOrderBySkillNameAsc();
    List<Skill> findByCategoryOrderBySkillNameAsc(SkillCategory category);
    boolean existsBySkillNameIgnoreCase(String skillName);
}
