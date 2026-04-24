package com.mezo.pos.auth.infrastructure.adapter;

import com.mezo.pos.auth.domain.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface SpringDataUserRepository extends JpaRepository<User, UUID> {

    @Query("SELECT u FROM User u WHERE u.email.value = :email AND u.deleted = false")
    Optional<User> findByEmailValue(@Param("email") String email);

    @Query("SELECT CASE WHEN COUNT(u) > 0 THEN true ELSE false END FROM User u WHERE u.email.value = :email AND u.deleted = false")
    boolean existsByEmailValue(@Param("email") String email);
}
