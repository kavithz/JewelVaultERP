package com.jewelvaulterp.role.repository;

import com.jewelvaulterp.role.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface RoleRepository extends JpaRepository<Role, UUID> {
}
