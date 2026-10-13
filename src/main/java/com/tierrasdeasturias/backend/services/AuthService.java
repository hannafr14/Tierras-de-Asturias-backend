package com.tierrasdeasturias.backend.services;

import com.tierrasdeasturias.backend.dtos.AuthResponseDto;
import com.tierrasdeasturias.backend.dtos.LoginRequestDto;
import com.tierrasdeasturias.backend.dtos.RegisterRequestDto;
import com.tierrasdeasturias.backend.entities.User;
import com.tierrasdeasturias.backend.repositories.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.tierrasdeasturias.backend.exceptions.DuplicateEmailException;
import com.tierrasdeasturias.backend.exceptions.InvalidCredentialsException;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public AuthResponseDto register(RegisterRequestDto request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateEmailException("Email is already registered");
        }

        User user = new User();
        user.setFirstName(request.getFirstName().trim());
        user.setLastName(request.getLastName().trim());
        user.setEmail(request.getEmail().trim().toLowerCase());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        User savedUser = userRepository.save(user);

        return mapToResponseDto(savedUser);
    }

    public AuthResponseDto login(LoginRequestDto request) {
        User user = userRepository.findByEmail(request.getEmail().trim().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        return mapToResponseDto(user);
    }

    private AuthResponseDto mapToResponseDto(User user) {
        return new AuthResponseDto(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail()
        );
    }
}