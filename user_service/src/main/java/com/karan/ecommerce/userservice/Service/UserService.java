package com.karan.ecommerce.userservice.Service;

import com.karan.ecommerce.userservice.DTO.UserRequest;
import com.karan.ecommerce.userservice.DTO.UserResponse;

import java.util.List;

public interface UserService {

    UserResponse createUser(UserRequest request);

    UserResponse getUserById(Long id);

    List<UserResponse> getAllUsers();

    UserResponse updateUser(Long id, UserRequest request);

    void deleteUser(Long id);

    UserResponse createMyProfile(String keycloakUserId, UserRequest request);

    UserResponse getMyProfile(String keycloakUserId);
}
