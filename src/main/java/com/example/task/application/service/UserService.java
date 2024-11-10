package com.example.task.application.service;

import com.example.task.application.dto.UserLoginResponseDto;
import com.example.task.application.dto.UserSignupResponseDto;
import com.example.task.application.utils.JwtUtil;
import com.example.task.domain.model.User;
import com.example.task.domain.repository.RedisRepository;
import com.example.task.domain.repository.UserRepository;
import com.example.task.presentation.request.UserLoginRequestDto;
import com.example.task.presentation.request.UserSignupRequestDto;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final RedisRepository redisRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;  // refresh token 만료 시간

    @Transactional
    public UserSignupResponseDto signupUser(UserSignupRequestDto requestDto) {
        log.info("회원 가입 로직");

        String username = requestDto.getUsername();
        String encodedPassword = passwordEncoder.encode(requestDto.getPassword());
        String nickname = requestDto.getNickname();

        verifySignupException(username, nickname);

        User user = User.createUser(username, encodedPassword, nickname);

        userRepository.save(user);

        return UserSignupResponseDto.fromEntity(user);
    }

    public UserLoginResponseDto signUser(UserLoginRequestDto requestDto, HttpServletResponse response) {
        log.info("로그인 로직");

        String username = requestDto.getUsername();
        String password = requestDto.getPassword();

        User user = findUserByUsername(username);

        verifyPassword(password, user);

        String accessToken = jwtUtil.createAccessToken(user);
        String refreshToken = jwtUtil.createRefreshToken(user);

        // refresh token 을 Cookie 에 저장
        addCookieToResponse(response, "refreshToken", refreshToken, refreshExpiration);
        // access token 을 Header에 저장
        addHeaderToResponse(response, accessToken);

        return new UserLoginResponseDto(accessToken);
    }

    public void reissueAccessToken(String refreshToken, HttpServletResponse response) {
        log.info("AccessToken 재발급 로직");
        // Redis에서 refreshToken 유효성 확인

        String storedToken = redisRepository.getValue(refreshToken);
        if (storedToken == null || !storedToken.equals("true")) {
            throw new IllegalArgumentException("유효하지 않은 Refresh Token입니다.");
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);  // 토큰에서 userId 추출
        User user = userRepository.findById(userId).orElseThrow(() ->
            new IllegalArgumentException("해당 유저는 존재하지 않습니다"));  // userId로 유저 조회
        String newAccessToken = jwtUtil.createAccessToken(user);

        // 새로운 Access Token을 응답 헤더에 추가
        addHeaderToResponse(response, newAccessToken);
    }

    private void addHeaderToResponse(HttpServletResponse response, String accessToken) {
        response.setHeader("Authorization", "Bearer " + accessToken);  // Authorization 헤더에 Access Token 추가
    }

    private void addCookieToResponse(HttpServletResponse response, String identifier, String value,
        Long refreshExpiration) {
        Cookie cookie = new Cookie(identifier, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false); // HTTPS 사용 시만 적용
        cookie.setPath("/");
        cookie.setMaxAge((int) TimeUnit.MILLISECONDS.toSeconds(refreshExpiration)); // 만료시간 설정
        response.addCookie(cookie);
    }

    private void verifyPassword(String password, User user) {
        boolean matches = passwordEncoder.matches(password, user.getPassword());
        if (!matches) {
            log.warn("로그인 요청, 비밀번호 인증 실패 username: {}", user.getUsername());
            throw new BadCredentialsException("Invalid password for username: " + user.getUsername());
        }
    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username).orElseThrow(() ->
            new UsernameNotFoundException("해당 유저를 찾을 수 없습니다. :" + username));
    }

    private void verifySignupException(String username, String nickname) {
        verifyDuplicatedUsername(username);
        verifyDuplicatedNickname(nickname);
    }

    private void verifyDuplicatedUsername(String username) {
        boolean value = userRepository.existsByUsername(username);
        if (value){
            log.warn("중복된 아이디로 회원 가입 실패 username : {}", username);
            throw new IllegalArgumentException("중복된 아이디가 존재합니다.");
        }
    }

    private void verifyDuplicatedNickname(String nickname) {
        boolean value = userRepository.existsByNickname(nickname);
        if (value){
            log.warn("중복된 닉네임으로 회원 가입 실패 nickname : {}", nickname);
            throw new IllegalArgumentException("중복된 닉네임이 존재합니다.");
        }
    }
}
