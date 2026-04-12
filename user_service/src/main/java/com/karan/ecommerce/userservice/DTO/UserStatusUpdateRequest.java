package com.karan.ecommerce.userservice.DTO;

import com.karan.ecommerce.userservice.Entity.enums.UserStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserStatusUpdateRequest {

    @NotNull(message = "Status is required")
    private UserStatus status;
}

