package com.example.task.application.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.task.application.dto.UserLoginResponseDto;
import com.example.task.application.utils.JwtUtil;
import com.example.task.domain.model.User;
import com.example.task.domain.repository.UserRepository;
import com.example.task.presentation.request.UserLoginRequestDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;


@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private UserService userService;

    private UserLoginRequestDto loginRequestDto;
    private final String accessToken = "accessToken";
    private final String refreshToken = "refreshToken";

    @BeforeEach
    void setUp(TestInfo testInfo) {
        loginRequestDto = new UserLoginRequestDto("testUser", "testPassword");
        User user = User.createUser("testUser", "encodedPassword", "testNick");

        // 테스트 환경에서 refreshExpiration을 직접 설정
        ReflectionTestUtils.setField(userService, "refreshExpiration", 36000000000L);

        when(userRepository.findByUsername("testUser")).thenReturn(Optional.of(user));

        if (!testInfo.getDisplayName().contains("비밀번호가 일치하지 않을 때")) {
            when(passwordEncoder.matches("testPassword", "encodedPassword")).thenReturn(true);
            when(jwtUtil.createAccessToken(user)).thenReturn(accessToken);
            when(jwtUtil.createRefreshToken(user)).thenReturn(refreshToken);
        }
    }

    @Test
    @DisplayName("Authorization 헤더와 쿠키에 토큰을 추가")
    void signUser_ShouldReturnUserLoginResponseDto_WhenCredentialsAreValid() {

        // Act
        UserLoginResponseDto responseDto = userService.signUser(loginRequestDto, response);

        // Assert
        assertNotNull(responseDto);
        assertEquals(accessToken, responseDto.getToken());

        verify(response).setHeader("Authorization", "Bearer " + accessToken);
        verify(response).addCookie(any(Cookie.class));
    }

    @Test
    @DisplayName("비밀번호가 일치하지 않을 때 BadCredentialsException을 던진다")
    void signUser_ShouldThrowBadCredentialsException_WhenPasswordIsInvalid() {
        // Arrange
        when(passwordEncoder.matches("testPassword", "encodedPassword")).thenReturn(false); // 비밀번호 불일치

        // Act & Assert
        BadCredentialsException exception = assertThrows(BadCredentialsException.class,
            () -> userService.signUser(loginRequestDto, response));

        assertEquals("Invalid password for username: testUser", exception.getMessage());
    }

}