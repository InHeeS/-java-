package com.example.task.application.service;

import com.example.task.application.dto.UserSignupResponseDto;
import com.example.task.domain.model.User;
import com.example.task.domain.repository.UserRepository;
import com.example.task.presentation.request.UserSignupRequestDto;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

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
