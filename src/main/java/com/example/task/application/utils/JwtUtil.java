package com.example.task.application.utils;

import com.example.task.domain.model.User;
import com.example.task.domain.repository.RedisRepository;
import com.example.task.domain.repository.UserRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.http.HttpServletRequest;
import java.util.Base64;
import java.util.Date;
import java.util.UUID;
import javax.crypto.SecretKey;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@Slf4j(topic = "JWT 관련 로그")
public class JwtUtil {

    public static final String AUTHORIZATION_HEADER = "Authorization";
    public static final String REFRESH_HEADER = "Refresh";
    public static final String BEARER_PREFIX = "Bearer ";

    private final SecretKey secretKey;
    private final String issuer;
    private final RedisRepository redisRepository;
    private final UserRepository userRepository;

    @Value("${jwt.access-expiration}")
    private Long accessExpiration;
    @Value("${jwt.refresh-expiration}")
    private Long refreshExpiration;

    public JwtUtil(
        @Value("${jwt.secret}") String secretKey, @Value("${jwt.issuer}")String issuer,
        RedisRepository redisRepository,
        UserRepository userRepository) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64URL.decode(secretKey));
        this.issuer = issuer;
        this.redisRepository = redisRepository;
        this.userRepository = userRepository;
    }

    public String createAccessToken(User user) {
        return generateToken(user, accessExpiration);
    }

    /**
     * refreshToken redis 저장후 반환
     * @param user
     * @return
     */
    public String createRefreshToken(User user) {
        String refreshToken = generateToken(user, refreshExpiration);

        redisRepository.save(refreshToken, "true");
        redisRepository.setExpire(refreshToken, refreshExpiration);

        return refreshToken;
    }

    private String generateToken(User user, Long expirationTime) {
        return Jwts.builder()
            .issuer(issuer)
            .id(user.getUserId().toString())
            .subject(user.getUsername())
            .claim("role", user.getAuthority())
            .claim("user_id", user.getUserId())
            .issuedAt(new Date())
            .expiration(new Date(System.currentTimeMillis() + expirationTime))
            .signWith(secretKey)  // HMAC SHA-256 알고리즘으로 서명
            .compact();
    }

    /**
     *
     * @param refreshToken
     * @return
     */
    public Long getUserIdFromToken(String refreshToken) {
        Jws<Claims> claims = parseToken(refreshToken);
        return claims.getPayload().get("user_id", Long.class);
    }

    public Jws<Claims> parseToken(final String token) {
        try {
            return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token);
        } catch (ExpiredJwtException e) {
            log.info("토큰이 만료되었습니다: {}", e.getMessage());
            return null; // 만료된 토큰은 null 반환 또는 다른 처리
        } catch (JwtException e) {
            log.error("유효하지 않은 토큰입니다: {}", e.getMessage());
            return null; // 유효하지 않은 토큰도 null 반환
        }
    }

    // header 에서 JWT 가져오기
    public String getJwtFromHeader(HttpServletRequest req, String token) {
        String bearerToken = req.getHeader(token);
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(BEARER_PREFIX)) {
            return bearerToken.substring(7);
        }
        return null;
    }

    // JWT 토큰 substring
    public String substringToken(String tokenValue) {
        if (StringUtils.hasText(tokenValue) && tokenValue.startsWith(BEARER_PREFIX)) {
            return tokenValue.substring(7);
        }
        throw new NullPointerException("Not Found Token");
    }

    public Claims verifyJwt(String token) {
        try{
            Jws<Claims> jws = parseToken(token);
            Claims claims = jws.getPayload();
            Long userId = claims.get("user_id", Long.class);

            if(userRepository.existsById(userId)){
                return claims;
            }else{
                throw new RuntimeException("유저 검증에 실패했습니다.");
            }
        } catch (ExpiredJwtException e) {
            throw new RuntimeException("토큰이 만료되었습니다: " + e.getMessage());
        } catch (Exception e) {
            throw new RuntimeException("유저 검증 중 문제가 발생했습니다: " + e.getMessage());
        }
    }
}
