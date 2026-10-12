package com.skillforge.repository;

import com.skillforge.model.CareerRole;
import com.skillforge.model.RoleRequiredSkill;
import com.skillforge.model.Skill;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RoleRequiredSkillRepository extends JpaRepository<RoleRequiredSkill, Integer> {
    List<RoleRequiredSkill> findByCareerRole(CareerRole careerRole);
    List<RoleRequiredSkill> findByCareerRoleRoleId(Integer roleId);
    Optional<RoleRequiredSkill> findByCareerRoleAndSkill(CareerRole careerRole, Skill skill);
}
