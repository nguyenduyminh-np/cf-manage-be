package com.duyminhdev.cf_manager.repository;

import com.duyminhdev.cf_manager.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleName(String roleName);

    Optional<Role> findByRoleCode(String roleCode);
}
