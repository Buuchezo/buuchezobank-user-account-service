package com.buuchezo.useraccountservice.repository;

import com.buuchezo.useraccountservice.entity.Role;
import com.buuchezo.useraccountservice.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByUser(String username);

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Long countByEnabledTrue();

    List<User> findByEnabledTrue();

    @Query("SELECT COUNT(distinct u) FROM User u JOIN u.roles r WHERE r.name = :roleName")
    Long countByRoleName(@Param("roleName") String roleName);


    @Query("SELECT u FROM User u JOIN u.roles r WHERE r = :role")
    List<User> findByRolesContaining(@Param("role") Role role);

}
