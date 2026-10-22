package com.classicstore.identity.service;

import com.classicstore.common.exception.BusinessException;
import com.classicstore.identity.domain.UserEntity;
import com.classicstore.identity.domain.UserRole;
import com.classicstore.identity.dto.AuthResponse;
import com.classicstore.identity.dto.LoginRequest;
import com.classicstore.identity.dto.RefreshTokenRequest;
import com.classicstore.identity.dto.RefreshResponse;
import com.classicstore.identity.dto.RegisterRequest;
import com.classicstore.identity.dto.UserResponse;
import com.classicstore.identity.repository.UserRepository;
import com.classicstore.identity.security.JwtProperties;
import com.classicstore.identity.security.JwtService;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {

    // Pre-computed constant dummy BCrypt hash used on unknown-user login paths
    // to ensure equal execution time and prevent timing side-channel account enumeration.
    private static final String DUMMY_BCRYPT_HASH = "$2a$10$76yC/mCjXw1.P0hY5Y.G/eIqV4cQ4v7wN1H1m3c5S9e4r2t3u4v5w";

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        String email = request.getEmail() != null ? request.getEmail().trim() : null;
        if (email != null && email.isBlank()) {
            email = null;
        }

        String phone = request.getPhone() != null ? request.getPhone().trim() : null;
        if (phone != null && phone.isBlank()) {
            phone = null;
        }

        if (email == null && phone == null) {
            throw new BusinessException(
                    "EMAIL_OR_PHONE_REQUIRED",
                    "البريد الإلكتروني أو رقم الهاتف مطلوب",
                    "Email or phone is required",
                    HttpStatus.BAD_REQUEST
            );
        }

        if (phone != null) {
            validatePhone(phone);
        }

        if (email != null && userRepository.existsByEmailIgnoreCase(email)) {
            throw new BusinessException(
                    "EMAIL_ALREADY_REGISTERED",
                    "البريد الإلكتروني مسجل بالفعل",
                    "Email already registered",
                    HttpStatus.CONFLICT
            );
        }

        if (phone != null && userRepository.existsByPhone(phone)) {
            throw new BusinessException(
                    "PHONE_ALREADY_REGISTERED",
                    "رقم الهاتف مسجل بالفعل",
                    "Phone already registered",
                    HttpStatus.CONFLICT
            );
        }

        if (request.getPassword() == null || request.getPassword().length() < 8) {
            throw new BusinessException(
                    "INVALID_INPUT",
                    "كلمة المرور يجب أن لا تقل عن 8 أحرف",
                    "Password must be at least 8 characters",
                    HttpStatus.BAD_REQUEST
            );
        }

        UserEntity user = new UserEntity();
        user.setEmail(email);
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        // Role is set explicitly to CUSTOMER regardless of input
        user.setRole(UserRole.CUSTOMER);
        user.setActive(true);

        String firstName = request.getFirstName();
        String lastName = request.getLastName();

        if ((firstName == null || firstName.isBlank()) && request.getName() != null && !request.getName().isBlank()) {
            String[] parts = request.getName().trim().split("\\s+", 2);
            firstName = parts[0];
            if (parts.length > 1) {
                lastName = parts[1];
            }
        }

        user.setFirstName(firstName);
        user.setLastName(lastName);

        UserEntity saved = userRepository.save(user);
        return new UserResponse(saved);
    }

    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        // TODO: Rate limiting on login endpoint
        String identifier = request.getEmail();
        if (identifier == null || identifier.isBlank()) {
            identifier = request.getPhone();
        }

        String rawPassword = request.getPassword();
        if (identifier == null || identifier.isBlank() || rawPassword == null || rawPassword.isBlank()) {
            throw invalidCredentials();
        }

        identifier = identifier.trim();

        Optional<UserEntity> userOpt = userRepository.findByEmailIgnoreCase(identifier);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByPhone(identifier);
        }

        if (userOpt.isEmpty()) {
            // Run password check against dummy hash so unknown user path takes equal time
            passwordEncoder.matches(rawPassword, DUMMY_BCRYPT_HASH);
            throw invalidCredentials();
        }

        UserEntity user = userOpt.get();

        if (!user.isActive() || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            throw invalidCredentials();
        }

        String accessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name());
        String refreshToken = jwtService.generateRefreshToken(user.getId());

        return new AuthResponse(accessToken, refreshToken, "Bearer", JwtProperties.ACCESS_TOKEN_EXPIRATION_SECONDS);
    }

    @Transactional(readOnly = true)
    public RefreshResponse refresh(RefreshTokenRequest request) {
        if (request == null || request.getRefreshToken() == null || request.getRefreshToken().isBlank()) {
            throw invalidToken();
        }

        JwtService.TokenClaims claims = jwtService.validateAndParseToken(request.getRefreshToken());
        if (!claims.isValid(JwtService.TYPE_REFRESH)) {
            throw invalidToken();
        }

        UserEntity user = userRepository.findById(claims.userId())
                .orElseThrow(this::invalidToken);

        if (!user.isActive()) {
            throw invalidToken();
        }

        String newAccessToken = jwtService.generateAccessToken(user.getId(), user.getRole().name());
        return new RefreshResponse(newAccessToken, "Bearer", JwtProperties.ACCESS_TOKEN_EXPIRATION_SECONDS);
    }

    // TODO: NEW-Q-01 Egyptian phone number formatting and validation rules (+20 format)
    private void validatePhone(String phone) {
        if (!phone.matches("^\\+?[0-9]{8,24}$")) {
            throw new BusinessException(
                    "INVALID_INPUT",
                    "رقم الهاتف غير صالحة",
                    "Invalid phone number format",
                    HttpStatus.BAD_REQUEST
            );
        }
    }

    private BusinessException invalidCredentials() {
        return new BusinessException(
                "INVALID_CREDENTIALS",
                "بيانات الاعتماد غير صالحة",
                "Invalid credentials",
                HttpStatus.UNAUTHORIZED
        );
    }

    private BusinessException invalidToken() {
        return new BusinessException(
                "INVALID_TOKEN",
                "رمز التنشيط غير صالحة أو منتهية الصلاحية",
                "Invalid or expired refresh token",
                HttpStatus.UNAUTHORIZED
        );
    }
}
