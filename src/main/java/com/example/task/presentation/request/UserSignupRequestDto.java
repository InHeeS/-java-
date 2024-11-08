package com.example.task.presentation.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserSignupRequestDto {

    @NotBlank(message = "Username은 필수 항목입니다.")
    private String username;

    @Size(min = 4, message = "Password는 최소 4자 이상이어야 합니다.")
    private String password;

    @NotBlank(message = "Nickname은 필수 항목입니다.")
    private String nickname;
}
