package com.skillforge.repository;

import com.skillforge.model.User;
import com.skillforge.model.UserProject;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserProjectRepository extends JpaRepository<UserProject, Integer> {
    List<UserProject> findByUserOrderByProjectIdDesc(User user);
    Optional<UserProject> findByProjectIdAndUser(Integer projectId, User user);
}
