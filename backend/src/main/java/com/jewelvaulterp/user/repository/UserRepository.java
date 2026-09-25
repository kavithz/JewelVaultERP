package com.jewelvaulterp.user.repository;

import com.jewelvaulterp.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByUsername(String username);

    Optional<User> findByEmail(String email);

    List<User> findByCompanyId(UUID companyId);

    @Query(value = "SELECT ur.role_id FROM user_roles ur WHERE ur.user_id = :userId", nativeQuery = true)
    List<UUID> findUserRoleIds(@Param("userId") UUID userId);

    @Modifying
    @Query(value = "INSERT INTO user_roles (user_id, role_id) VALUES (:userId, :roleId)", nativeQuery = true)
    void assignRoleToUser(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    @Modifying
    @Query(value = "DELETE FROM user_roles WHERE user_id = :userId AND role_id = :roleId", nativeQuery = true)
    void removeRoleFromUser(@Param("userId") UUID userId, @Param("roleId") UUID roleId);

    long countByActiveTrue();

    long countByActiveFalse();
}