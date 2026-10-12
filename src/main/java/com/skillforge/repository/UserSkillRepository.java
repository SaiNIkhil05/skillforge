package com.skillforge.repository;

import com.skillforge.model.Skill;
import com.skillforge.model.User;
import com.skillforge.model.UserSkill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserSkillRepository extends JpaRepository<UserSkill, Integer> {
    List<UserSkill> findByUser(User user);
    List<UserSkill> findByUserUserId(Integer userId);
    Optional<UserSkill> findByUserAndSkill(User user, Skill skill);
    void deleteByUserAndSkill(User user, Skill skill);
}
