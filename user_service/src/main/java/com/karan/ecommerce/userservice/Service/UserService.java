package com.karan.ecommerce.userservice.Service;

import com.karan.ecommerce.userservice.DTO.UserRequest;
import com.karan.ecommerce.userservice.DTO.UserResponse;
import com.karan.ecommerce.userservice.Entity.enums.UserStatus;
import org.springframework.data.domain.Page;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse getUserById(Long id);

    Page<UserResponse> getAllUsers(int page, int size);

    UserResponse updateUser(Long id, UserRequest request);

    void deactivateUser(Long id);

    UserResponse updateUserStatus(Long id, UserStatus status);

    UserResponse createMyProfile(String keycloakUserId, UserRequest request);

    UserResponse getMyProfile(String keycloakUserId);

    UserResponse updateMyProfile(String keycloakUserId, UserRequest request);

    void deactivateMyProfile(String keycloakUserId);
}
