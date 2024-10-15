package com.springBoot.security.services;

import com.springBoot.security.auth.AuthenticationRequest;
import com.springBoot.security.auth.AuthenticationResponse;
import com.springBoot.security.auth.RegisterRequest;
import com.springBoot.security.config.JWTService;
import com.springBoot.security.enums.Role;
import com.springBoot.security.exception.EmailAlreadyRegisteredException;
import com.springBoot.security.repository.UserRepository;
import com.springBoot.security.user.User;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Map;
@Service
@AllArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JWTService jwtService;
    private final AuthenticationManager authenticationManager;

    public AuthenticationResponse register(RegisterRequest request) {
        if (request.getFirstName() == null || request.getFirstName().isEmpty()) {
            return AuthenticationResponse.builder()
                    .token(null)
                    .message("First name is required")
                    .build();
        }
        if (request.getLastName() == null || request.getLastName().isEmpty()) {
            return AuthenticationResponse.builder()
                    .token(null)
                    .message("Last name is required")
                    .build();
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            return AuthenticationResponse.builder()
                    .token(null)
                    .message("Email already registered")
                    .build();
        }

        var user = User.builder()
                .firstName(request.getFirstName())
                .lastName(request.getLastName())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.User)
                .build();

        var savedUser = userRepository.save(user);
        Map<String, Object> claims = Map.of("userId", savedUser.getId());
        var jwtToken = jwtService.generateToken(claims, savedUser);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }


    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        var user = userRepository.findByEmail(request.getEmail()).orElseThrow();
        Map<String, Object> claims = Map.of("userId", user.getId());
        var jwtToken = jwtService.generateToken(claims, user);
        return AuthenticationResponse.builder().token(jwtToken).build();
    }
}
