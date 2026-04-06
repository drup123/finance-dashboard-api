package com.finance.dashboard.service;

import com.finance.dashboard.dto.CreateUserRequest;
import com.finance.dashboard.dto.LoginRequest;
import com.finance.dashboard.exception.BadRequestException;
import com.finance.dashboard.model.Role;
import com.finance.dashboard.model.User;
import com.finance.dashboard.repository.UserRepository;
import com.finance.dashboard.security.JwtUtils;
import com.finance.dashboard.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock AuthenticationManager authenticationManager;
    @Mock UserRepository userRepository;
    @Mock PasswordEncoder passwordEncoder;
    @Mock JwtUtils jwtUtils;

    @InjectMocks AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {

        // ❌ removed builder → ✅ manual object creation
        sampleUser = new User();
        sampleUser.setId(1L);
        sampleUser.setName("Test Admin");
        sampleUser.setEmail("admin@test.com");
        sampleUser.setPassword("encoded");
        sampleUser.setRole(Role.ADMIN);
        sampleUser.setActive(true);
    }

    @Test
    void login_successfulCredentials_returnsToken() {

        UserDetailsImpl userDetails = new UserDetailsImpl(sampleUser);

        Authentication auth = new UsernamePasswordAuthenticationToken(
                userDetails,
                null,
                userDetails.getAuthorities()
        );

        when(authenticationManager.authenticate(any())).thenReturn(auth);
        when(userRepository.findByEmail("admin@test.com"))
                .thenReturn(Optional.of(sampleUser));
        when(jwtUtils.generateToken(auth)).thenReturn("mock.jwt.token");

        LoginRequest req = new LoginRequest();
        req.setEmail("admin@test.com");
        req.setPassword("admin123");

        var response = authService.login(req);

        assertThat(response.getToken()).isEqualTo("mock.jwt.token");
        assertThat(response.getRole()).isEqualTo("ADMIN");
    }

    @Test
    void register_duplicateEmail_throwsBadRequest() {

        when(userRepository.existsByEmail("admin@test.com")).thenReturn(true);

        CreateUserRequest req = new CreateUserRequest();
        req.setName("Dup");
        req.setEmail("admin@test.com");
        req.setPassword("pass123");
        req.setRole(Role.VIEWER);

        assertThatThrownBy(() -> authService.register(req))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("already in use");
    }

    @Test
    void register_newEmail_savesUserAndReturnsToken() {

        when(userRepository.existsByEmail(anyString())).thenReturn(false);
        when(passwordEncoder.encode(anyString())).thenReturn("encoded");

        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(99L);
            return u;
        });

        when(jwtUtils.generateTokenFromEmail(anyString()))
                .thenReturn("new.jwt.token");

        CreateUserRequest req = new CreateUserRequest();
        req.setName("New User");
        req.setEmail("new@test.com");
        req.setPassword("pass123");
        req.setRole(Role.ANALYST);

        var response = authService.register(req);

        assertThat(response.getToken()).isEqualTo("new.jwt.token");
        assertThat(response.getRole()).isEqualTo("ANALYST");

        verify(userRepository).save(any(User.class));
    }
}