package com.example.task.application.security;


import com.example.task.application.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j(topic = "JWT 검증 및 인가")
@RequiredArgsConstructor
public class JwtAuthorizationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final UserDetailsServiceImpl userDetailsService;

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
        FilterChain filterChain) throws ServletException, IOException {
        if (req.getRequestURI().startsWith("/signup") ||
            req.getRequestURI().startsWith("/sign") ||
            req.getRequestURI().startsWith("/access-token/reissue") ||
            req.getRequestURI().equals("/")
        ) {
            log.info("Pass Authorization : " + req.getRequestURI());
            filterChain.doFilter(req, res);
            return;
        }

        String accessTokenValue = jwtUtil.getJwtFromHeader(req, JwtUtil.AUTHORIZATION_HEADER);
        log.info("accessTokenValue = {}", accessTokenValue);

        if (StringUtils.hasText(accessTokenValue)) {
            // JWT 토큰 substring
            accessTokenValue = jwtUtil.substringToken(accessTokenValue);

            // access 토큰이 유효하면 그대로 반환 -> validateToken
            // 만료되어 refresh 토큰을 통해 반환되면 새로운 토큰 발급 -> validateToken

            Claims info = jwtUtil.verifyJwt(accessTokenValue);

            try {
                setAuthentication(info.getSubject());
            } catch (Exception e) {
                log.error(e.toString());
                return;
            }
        }

        filterChain.doFilter(req, res);
    }

    // 인증 처리
    public void setAuthentication(String username) {
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        Authentication authentication = createAuthentication(username);
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
    }


    // 인증 객체 생성
    private Authentication createAuthentication(String username){
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
    }
}
