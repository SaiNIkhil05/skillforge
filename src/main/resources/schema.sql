-- =====================================================
-- SkillForge Database Schema (MySQL 8)
-- 8 Normalized Tables
-- =====================================================

CREATE DATABASE IF NOT EXISTS `skillforge2_db` 
  DEFAULT CHARACTER SET utf8mb4 
  COLLATE utf8mb4_unicode_ci;

USE `skillforge2_db`;

-- 1. career_roles
CREATE TABLE IF NOT EXISTS career_roles (
  role_id INT AUTO_INCREMENT PRIMARY KEY,
  role_name VARCHAR(100) NOT NULL,
  description TEXT
) ENGINE=InnoDB;

-- 2. skills (master catalog)
CREATE TABLE IF NOT EXISTS skills (
  skill_id INT AUTO_INCREMENT PRIMARY KEY,
  skill_name VARCHAR(100) NOT NULL UNIQUE,
  category ENUM('LANGUAGE', 'FRAMEWORK', 'DATABASE', 'TOOLS_AND_PRACTICES') NOT NULL
) ENGINE=InnoDB;

-- 3. users
CREATE TABLE IF NOT EXISTS users (
  user_id INT AUTO_INCREMENT PRIMARY KEY,
  full_name VARCHAR(100) NOT NULL,
  email VARCHAR(100) NOT NULL UNIQUE,
  password_hash VARCHAR(255) NOT NULL,
  role ENUM('STUDENT', 'ADMIN') NOT NULL,
  education VARCHAR(150),
  target_role_id INT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  email_verified BOOLEAN DEFAULT FALSE,
  verification_token VARCHAR(100) NULL,
  CONSTRAINT fk_users_career_role FOREIGN KEY (target_role_id) 
    REFERENCES career_roles(role_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 4. role_required_skills (benchmark profile for a role)
CREATE TABLE IF NOT EXISTS role_required_skills (
  id INT AUTO_INCREMENT PRIMARY KEY,
  role_id INT NOT NULL,
  skill_id INT NOT NULL,
  priority ENUM('CORE', 'GOOD_TO_HAVE') NOT NULL,
  weight INT NOT NULL,
  CONSTRAINT fk_rrs_career_role FOREIGN KEY (role_id) 
    REFERENCES career_roles(role_id) ON DELETE CASCADE,
  CONSTRAINT fk_rrs_skill FOREIGN KEY (skill_id) 
    REFERENCES skills(skill_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 5. user_skills (candidate declared skills)
CREATE TABLE IF NOT EXISTS user_skills (
  id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  skill_id INT NOT NULL,
  proficiency ENUM('BEGINNER', 'INTERMEDIATE', 'ADVANCED') NOT NULL,
  added_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
  UNIQUE KEY uq_user_skill (user_id, skill_id),
  CONSTRAINT fk_us_user FOREIGN KEY (user_id) 
    REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_us_skill FOREIGN KEY (skill_id) 
    REFERENCES skills(skill_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 6. user_projects (candidate projects)
CREATE TABLE IF NOT EXISTS user_projects (
  project_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  title VARCHAR(150) NOT NULL,
  tech_used VARCHAR(200) NOT NULL,
  project_link VARCHAR(255) NULL,
  CONSTRAINT fk_up_user FOREIGN KEY (user_id) 
    REFERENCES users(user_id) ON DELETE CASCADE
) ENGINE=InnoDB;

-- 7. roadmap_items (role milestones)
CREATE TABLE IF NOT EXISTS roadmap_items (
  roadmap_id INT AUTO_INCREMENT PRIMARY KEY,
  role_id INT NOT NULL,
  skill_id INT NULL,
  title VARCHAR(150) NOT NULL,
  order_index INT NOT NULL,
  CONSTRAINT fk_ri_career_role FOREIGN KEY (role_id) 
    REFERENCES career_roles(role_id) ON DELETE CASCADE,
  CONSTRAINT fk_ri_skill FOREIGN KEY (skill_id) 
    REFERENCES skills(skill_id) ON DELETE SET NULL
) ENGINE=InnoDB;

-- 8. user_progress (milestone progression)
CREATE TABLE IF NOT EXISTS user_progress (
  progress_id INT AUTO_INCREMENT PRIMARY KEY,
  user_id INT NOT NULL,
  roadmap_id INT NOT NULL,
  status ENUM('NOT_STARTED', 'IN_PROGRESS', 'COMPLETED') NOT NULL DEFAULT 'NOT_STARTED',
  completed_at TIMESTAMP NULL,
  UNIQUE KEY uq_user_roadmap (user_id, roadmap_id),
  CONSTRAINT fk_upr_user FOREIGN KEY (user_id) 
    REFERENCES users(user_id) ON DELETE CASCADE,
  CONSTRAINT fk_upr_roadmap FOREIGN KEY (roadmap_id) 
    REFERENCES roadmap_items(roadmap_id) ON DELETE CASCADE
) ENGINE=InnoDB;
