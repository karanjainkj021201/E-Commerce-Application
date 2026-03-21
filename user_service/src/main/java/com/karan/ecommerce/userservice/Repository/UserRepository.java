package com.karan.ecommerce.userservice.Repository;

import com.karan.ecommerce.userservice.Entity.UserEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepository extends JpaRepository<UserEntity, Long> {
}
