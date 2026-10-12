package com.skillforge.service;

import com.skillforge.dto.AuthResponse;
import com.skillforge.dto.LoginRequest;
import com.skillforge.dto.RegisterRequest;
import com.skillforge.exception.InvalidDomainException;
import com.skillforge.exception.ResourceNotFoundException;
import com.skillforge.model.CareerRole;
import com.skillforge.model.User;
import com.skillforge.model.UserRole;
import com.skillforge.repository.CareerRoleRepository;
import com.skillforge.repository.UserRepository;
import com.skillforge.security.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthService {

    private static final Logger log = LoggerFactory.getLogger(AuthService.class);

    private final UserRepository userRepository;
    private final CareerRoleRepository careerRoleRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final String allowedDomain;

    public AuthService(
            UserRepository userRepository,
            CareerRoleRepository careerRoleRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            @Value("${app.college.allowed-domain:college.edu}") String allowedDomain) {
        this.userRepository = userRepository;
        this.careerRoleRepository = careerRoleRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.allowedDomain = allowedDomain;
    }

    @Transactional
    public User register(RegisterRequest request) {
        String email = request.getEmail().trim().toLowerCase();

        // 1. Validate allowed domain
        boolean validDomain = false;
        if (allowedDomain == null || allowedDomain.isBlank() || allowedDomain.equals("*") || allowedDomain.contains("*")) {
            validDomain = true;
        } else {
            String[] domains = allowedDomain.split(",");
            for (String d : domains) {
                String trimmed = d.trim().toLowerCase();
                if (trimmed.equals("*") || email.endsWith("@" + trimmed) || email.endsWith("." + trimmed)) {
                    validDomain = true;
                    break;
                }
            }
        }
        if (!validDomain) {
            throw new InvalidDomainException("Registration requires an approved email ending in @" + allowedDomain.replace(",", " or @"));
        }

        // 2. Validate passwords match if confirmation provided
        if (request.getConfirmPassword() != null && !request.getPassword().equals(request.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // 3. Check existing user
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with email " + email + " already exists");
        }

        User user = new User();
        user.setFullName(request.getFullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setRole(UserRole.STUDENT);
        user.setEducation(request.getEducation() != null ? request.getEducation().trim() : "B.Tech Computer Science");
        user.setEmailVerified(true);

        String token = UUID.randomUUID().toString();
        user.setVerificationToken(token);

        if (request.getTargetRoleId() != null) {
            careerRoleRepository.findById(request.getTargetRoleId()).ifPresent(user::setTargetRole);
        } else {
            careerRoleRepository.findAll().stream().findFirst().ifPresent(user::setTargetRole);
        }

        User saved = userRepository.save(user);

        // Produce verification link & log to console in dev mode
        String verificationUrl = "http://localhost:8080/verify?token=" + token;
        log.info("==================================================================");
        log.info("COLLEGE EMAIL VERIFICATION LINK FOR {}:", email);
        log.info(verificationUrl);
        log.info("==================================================================");

        return saved;
    }

    @Transactional
    public boolean verifyEmail(String token) {
        User user = userRepository.findByVerificationToken(token)
                .orElseThrow(() -> new ResourceNotFoundException("Invalid or expired verification token"));

        user.setEmailVerified(true);
        user.setVerificationToken(null);
        userRepository.save(user);
        return true;
    }

    public AuthResponse login(LoginRequest request) {
        String input = request.getEmail().trim().toLowerCase();
        final String resolvedEmail;
        if ("admin".equals(input)) {
            resolvedEmail = "admin@college.edu";
        } else if ("student".equals(input)) {
            resolvedEmail = "student@college.edu";
        } else {
            resolvedEmail = input;
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(resolvedEmail, request.getPassword())
        );

        User user = userRepository.findByEmail(resolvedEmail)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + resolvedEmail));

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getFullName(),
                user.getRole().name(),
                user.getEmailVerified(),
                "Login successful"
        );
    }
}
