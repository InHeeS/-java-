package com.example.task.application.utils;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.task.domain.repository.RedisRepository;
import com.example.task.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private RedisRepository redisRepository;

    private JwtUtil jwtUtil;

    private String validToken;
    private String invalidToken = "invalidToken";
    private final Long userId = 1L;  // 실제 userId로 설정

    @BeforeEach
    void setUp(TestInfo testInfo) {

        // JwtUtil 인스턴스를 생성하면서 필요한 값들을 전달
        String secretKey = "dd8c1a53325ae3211ae6dd4f2ad2d5d80d0ced29305ba45d3df0c8a249e8ea50e984dac55d0513a400e4f7d7b17599ba49439039c22527d187d280d26722a5d1";
        jwtUtil = new JwtUtil(secretKey, "admin-user", redisRepository, userRepository);
        ReflectionTestUtils.setField(jwtUtil, "accessExpiration", 3600000000L); // 1시간
        ReflectionTestUtils.setField(jwtUtil, "refreshExpiration", 36000000000L); // 10시간

        // 실제 유효한 accessToken을 직접 설정, userId = 1L (실제 로그인 후 반환된 accessToken 및 userId 기반)
        validToken = "eyJhbGciOiJIUzUxMiJ9.eyJpc3MiOiJhZG1pbi11c2VyIiwianRpIjoiMSIsInN1YiI6ImV4YW1wbGVVc2VyIiwicm9sZSI6IlJPTEVfVVNFUiIsInVzZXJfaWQiOjEsImlhdCI6MTczMTIxOTg1OSwiZXhwIjoxNzM0ODE5ODU5fQ.74MH6209iUZK3azD9qssdDc_3_KqSYAAfAzx5H8M7QrZcpuNTykYwGyaSCeFw3xmsKdSlkbqL3IVjGzt45AVDQ";
        invalidToken = "invalidToken";

        if (!testInfo.getDisplayName().contains("유효하지 않은 토큰")) {
            // UserRepository에 userId로 유저가 존재하는지 확인
            when(userRepository.existsById(userId)).thenReturn(true);
        }
    }

    @Test
    @DisplayName("유효한 토큰일 때 verifyJwt는 Claims 객체를 반환한다")
    void verifyJwt_ShouldReturnClaims_WhenTokenIsValid() {
        Claims result = jwtUtil.verifyJwt(validToken);

        assertNotNull(result);
        assertEquals(userId, result.get("user_id", Long.class));
    }

    @Test
    @DisplayName("유효하지 않은 토큰일 때 verifyJwt는 RuntimeException을 던진다")
    void verifyJwt_ShouldThrowRuntimeException_WhenTokenIsInvalid() {

        RuntimeException exception = assertThrows(RuntimeException.class, () -> jwtUtil.verifyJwt(invalidToken));
        assertEquals("유효하지 않은 토큰입니다.", exception.getMessage());
    }

    @Test
    @DisplayName("존재하지 않는 유저의 토큰일 때 verifyJwt는 RuntimeException을 던진다")
    void verifyJwt_ShouldThrowRuntimeException_WhenUserDoesNotExist() {
        // 존재하지 않는 유저로 설정하여 예외 발생 시뮬레이션
        when(userRepository.existsById(userId)).thenReturn(false);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> jwtUtil.verifyJwt(validToken));
        assertEquals("유저 검증에 실패했습니다.", exception.getMessage());
    }
}