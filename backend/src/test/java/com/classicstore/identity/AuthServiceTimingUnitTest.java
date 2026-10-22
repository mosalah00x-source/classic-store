package com.classicstore.identity;

import com.classicstore.common.exception.BusinessException;
import com.classicstore.identity.dto.LoginRequest;
import com.classicstore.identity.repository.UserRepository;
import com.classicstore.identity.security.JwtService;
import com.classicstore.identity.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTimingUnitTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthService authService;

    @Test
    void loginWithUnknownUser_executesPasswordEncoderMatchesAgainstDummyHash() {
        when(userRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        LoginRequest request = new LoginRequest("unknown@example.com", "Password123!");

        assertThatThrownBy(() -> authService.login(request))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Invalid credentials");

        // Verify passwordEncoder.matches was called with raw password and dummy hash to prevent timing attack
        verify(passwordEncoder).matches(eq("Password123!"), anyString());
    }
}
