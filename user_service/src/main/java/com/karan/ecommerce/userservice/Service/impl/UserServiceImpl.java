package com.karan.ecommerce.userservice.Service.impl;

import com.karan.ecommerce.userservice.DTO.UserRequest;
import com.karan.ecommerce.userservice.DTO.UserResponse;
import com.karan.ecommerce.userservice.Entity.UserEntity;
import com.karan.ecommerce.userservice.Entity.enums.UserStatus;
import com.karan.ecommerce.userservice.Exception.DuplicateUserException;
import com.karan.ecommerce.userservice.Exception.UserNotFoundException;
import com.karan.ecommerce.userservice.Repository.UserRepository;
import com.karan.ecommerce.userservice.Service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse createUser(UserRequest request) {
        validateEmailUniqueness(request.getEmail(), null);

        UserEntity user = new UserEntity();
        user.setKeycloakUserId(null);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.ACTIVE);

        return mapToResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getUserById(Long id) {
        return mapToResponse(getUserEntityById(id));
    }

    @Override
    public Page<UserResponse> getAllUsers(int page, int size) {
        return userRepository.findAll(PageRequest.of(page, size))
                .map(this::mapToResponse);
    }

    @Override
    public UserResponse updateUser(Long id, UserRequest request) {
        UserEntity user = getUserEntityById(id);
        applyUserUpdates(user, request);
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public void deactivateUser(Long id) {
        UserEntity user = getUserEntityById(id);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    @Override
    public UserResponse updateUserStatus(Long id, UserStatus status) {
        UserEntity user = getUserEntityById(id);
        user.setStatus(status);
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public UserResponse createMyProfile(String keycloakUserId, UserRequest request) {
        if (userRepository.existsByKeycloakUserId(keycloakUserId)) {
            throw new DuplicateUserException("Profile already exists for this Keycloak user");
        }

        validateEmailUniqueness(request.getEmail(), null);

        UserEntity user = new UserEntity();
        user.setKeycloakUserId(keycloakUserId);
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setStatus(UserStatus.ACTIVE);

        return mapToResponse(userRepository.save(user));
    }

    @Override
    public UserResponse getMyProfile(String keycloakUserId) {
        return mapToResponse(getUserEntityByKeycloakId(keycloakUserId));
    }

    @Override
    public UserResponse updateMyProfile(String keycloakUserId, UserRequest request) {
        UserEntity user = getUserEntityByKeycloakId(keycloakUserId);
        applyUserUpdates(user, request);
        return mapToResponse(userRepository.save(user));
    }

    @Override
    public void deactivateMyProfile(String keycloakUserId) {
        UserEntity user = getUserEntityByKeycloakId(keycloakUserId);
        user.setStatus(UserStatus.INACTIVE);
        userRepository.save(user);
    }

    private void applyUserUpdates(UserEntity user, UserRequest request) {
        validateEmailUniqueness(request.getEmail(), user.getId());
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
    }

    private void validateEmailUniqueness(String email, Long currentUserId) {
        UserEntity existingUser = userRepository.findByEmailIgnoreCase(email).orElse(null);
        if (existingUser == null) {
            return;
        }

        if (currentUserId != null && existingUser.getId().equals(currentUserId)) {
            return;
        }

        throw new DuplicateUserException("User with email " + email + " already exists");
    }

    private UserEntity getUserEntityById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }

    private UserEntity getUserEntityByKeycloakId(String keycloakUserId) {
        return userRepository.findByKeycloakUserId(keycloakUserId)
                .orElseThrow(() -> new UserNotFoundException("Profile not found for logged-in user"));
    }

    private UserResponse mapToResponse(UserEntity user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getStatus(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}
