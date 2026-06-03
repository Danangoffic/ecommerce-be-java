package com.ecommerce.controller;

import com.ecommerce.dto.request.UpdateProfileRequest;
import com.ecommerce.dto.response.ApiResponse;
import com.ecommerce.dto.response.UserProfileResponse;
import com.ecommerce.security.AuthenticatedUser;
import com.ecommerce.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PatchMapping("/profile")
    public ApiResponse<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal AuthenticatedUser user,
            @RequestBody UpdateProfileRequest request) {
        return ApiResponse.success("Profile updated", userService.updateProfile(user.getId(), request));
    }
}
