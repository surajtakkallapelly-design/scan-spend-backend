package com.scanspend.service;

import com.scanspend.dto.AuthResponse;
import com.scanspend.dto.ChangePasswordRequest;
import com.scanspend.dto.ProfileDto;
import com.scanspend.exception.ApiException;
import com.scanspend.model.User;
import com.scanspend.repository.UserRepository;
import com.scanspend.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class ProfileService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public ProfileService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    public ProfileDto get(String email) {
        User user = userRepository.findByEmail(email).orElseThrow();
        return new ProfileDto(user.getName(), user.getEmail(), user.getIncome());
    }

    public AuthResponse update(String email, ProfileDto dto) {
        User user = userRepository.findByEmail(email).orElseThrow();
        boolean emailChanged = !user.getEmail().equals(dto.email());

        if (emailChanged && userRepository.existsByEmail(dto.email())) {
            throw new ApiException("Email already in use", HttpStatus.BAD_REQUEST);
        }
        user.setName(dto.name());
        user.setEmail(dto.email());
        user.setIncome(dto.income());
        userRepository.save(user);

        String token = jwtService.generateToken(user.getEmail());
        return new AuthResponse(token, user.getEmail(), user.getName(), user.getIncome());
    }

    public void changePassword(String email, ChangePasswordRequest req) {
        User user = userRepository.findByEmail(email).orElseThrow();
        if (!passwordEncoder.matches(req.currentPassword(), user.getPassword())) {
            throw new ApiException("Current password is incorrect", HttpStatus.BAD_REQUEST);
        }
        user.setPassword(passwordEncoder.encode(req.newPassword()));
        userRepository.save(user);
    }
}
