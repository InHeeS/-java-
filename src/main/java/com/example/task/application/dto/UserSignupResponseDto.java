package com.example.task.application.dto;

import com.example.task.domain.model.User;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(access = AccessLevel.PRIVATE)
public class UserSignupResponseDto {

    private String username;
    private String nickname;
    private List<AuthorityDto> authorities;

    public static UserSignupResponseDto fromEntity(User user) {
        return UserSignupResponseDto.builder()
            .username(user.getUsername())
            .nickname(user.getNickname())
            .authorities(List.of(new AuthorityDto(user.getAuthority())))
            .build();
    }

    @Getter
    @AllArgsConstructor
    public static class AuthorityDto{
        private String authorityName;
    }
}
