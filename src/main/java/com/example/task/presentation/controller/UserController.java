package com.example.task.presentation.controller;

import com.example.task.application.dto.UserSignupResponseDto;
import com.example.task.application.service.UserService;
import com.example.task.presentation.request.UserSignupRequestDto;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/signup")
    public ResponseEntity<UserSignupResponseDto> signupUser(@Valid @RequestBody UserSignupRequestDto requestDto){
        UserSignupResponseDto responseDto = userService.signupUser(requestDto);
        return ResponseEntity.status(HttpStatus.CREATED).body(responseDto);
    }
}
