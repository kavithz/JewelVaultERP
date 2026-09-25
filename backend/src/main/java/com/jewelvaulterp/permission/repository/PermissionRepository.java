package com.jewelvaulterp.permission.repository;

import com.jewelvaulterp.permission.entity.Permission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface PermissionRepository extends JpaRepository<Permission, UUID> {

    @Query(value = "SELECT p.* FROM permissions p INNER JOIN role_permissions rp ON rp.permission_id = p.id WHERE rp.role_id = :roleId ORDER BY p.name ASC", nativeQuery = true)
    List<Permission> findRolePermissions(@Param("roleId") UUID roleId);

    @Modifying
    @Query(value = "INSERT INTO role_permissions (role_id, permission_id) VALUES (:roleId, :permissionId)", nativeQuery = true)
    void assignPermissionToRole(@Param("roleId") UUID roleId, @Param("permissionId") UUID permissionId);

    @Modifying
    @Query(value = "DELETE FROM role_permissions WHERE role_id = :roleId AND permission_id = :permissionId", nativeQuery = true)
    void removePermissionFromRole(@Param("roleId") UUID roleId, @Param("permissionId") UUID permissionId);
}
