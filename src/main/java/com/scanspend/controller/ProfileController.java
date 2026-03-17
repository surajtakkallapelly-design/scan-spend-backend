package com.scanspend.controller;

import com.scanspend.dto.AuthResponse;
import com.scanspend.dto.ChangePasswordRequest;
import com.scanspend.dto.ProfileDto;
import com.scanspend.service.ProfileService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
public class ProfileController {

    private final ProfileService profileService;

    public ProfileController(ProfileService profileService) {
        this.profileService = profileService;
    }

    @GetMapping
    public ResponseEntity<ProfileDto> me(@AuthenticationPrincipal UserDetails user) {
        return ResponseEntity.ok(profileService.get(user.getUsername()));
    }

    @PutMapping
    public ResponseEntity<AuthResponse> update(@AuthenticationPrincipal UserDetails user,
                                               @Valid @RequestBody ProfileDto dto) {
        return ResponseEntity.ok(profileService.update(user.getUsername(), dto));
    }

    @PutMapping("/password")
    public ResponseEntity<Void> changePassword(@AuthenticationPrincipal UserDetails user,
                                               @Valid @RequestBody ChangePasswordRequest req) {
        profileService.changePassword(user.getUsername(), req);
        return ResponseEntity.noContent().build();
    }
}
