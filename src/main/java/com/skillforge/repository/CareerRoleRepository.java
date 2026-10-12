package com.skillforge.repository;

import com.skillforge.model.CareerRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CareerRoleRepository extends JpaRepository<CareerRole, Integer> {
    Optional<CareerRole> findByRoleName(String roleName);
}
