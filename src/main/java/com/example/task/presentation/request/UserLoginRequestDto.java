package com.example.task.presentation.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;

@Getter
public class UserLoginRequestDto {

    @NotBlank
    private String username;

    @NotBlank
    private String password;
}
