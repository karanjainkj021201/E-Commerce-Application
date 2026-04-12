package com.karan.ecommerce.userservice.Repository;

import com.karan.ecommerce.userservice.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<UserEntity, Long> {
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByKeycloakUserId(String keycloakUserId);
    Optional<UserEntity> findByKeycloakUserId(String keycloakUserId);
    Optional<UserEntity> findByEmailIgnoreCase(String email);
}
