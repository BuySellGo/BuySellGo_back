package com.buysellgo.userservice.strategy.sign.impl;

import com.buysellgo.userservice.common.auth.JwtTokenProvider;
import com.buysellgo.userservice.common.auth.TokenUserInfo;
import com.buysellgo.userservice.common.entity.Role;
import com.buysellgo.userservice.domain.user.LoginType;
import com.buysellgo.userservice.domain.user.User;
import com.buysellgo.userservice.repository.UserRepository;
import com.buysellgo.userservice.strategy.sign.common.SignResult;
import com.buysellgo.userservice.strategy.sign.common.SignStrategy;
import com.buysellgo.userservice.strategy.sign.dto.*;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static com.buysellgo.userservice.common.util.CommonConstant.*;

@Component
@Transactional
@RequiredArgsConstructor
@Slf4j
public class UserSignStrategy implements SignStrategy<Map<String,Object>> {
    private final UserRepository userRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> userTemplate;

    @Override
    public SignResult<Map<String,Object>> signUp(SignUpDto dto) {
        Map<String, Object> data = new HashMap<>();
        try {
            if (!(dto instanceof UserSignUpDto userSignUpDto)) {
                return SignResult.fail(DTO_NOT_MATCHED.getValue(), data);
            }

            User user = User.of(userSignUpDto.email(),
                    passwordEncoder.encode(userSignUpDto.password()),
                    userSignUpDto.username(),
                    userSignUpDto.phone(),
                    LoginType.COMMON,
                    Role.USER,
                    userSignUpDto.emailCertified(),
                    userSignUpDto.agreePICU(),
                    userSignUpDto.agreeEmail(),
                    userSignUpDto.agreeTOS()
            );
            data.put(USER_VO.getValue(), user.toVo());

            if(userRepository.existsByEmail(user.getEmail()) ||
                    userRepository.existsByUsername(user.getUsername()) ||
                    userRepository.existsByPhone(user.getPhone())){
                return SignResult.fail(VALUE_DUPLICATED.getValue(),data);
            }
            userRepository.save(user);
            return SignResult.success(USER_CREATED.getValue(), data);
        } catch (RuntimeException e) {
            e.setStackTrace(e.getStackTrace());
            return SignResult.fail(SAVE_FAILURE.getValue(), data);
        }
    }

    @Override
    public SignResult<Map<String,Object>> withdraw(String token) {
        Map<String,Object> data = new HashMap<>();
        TokenUserInfo userInfo = jwtTokenProvider.validateAndGetTokenUserInfo(token);
        Optional<User> userOptional = userRepository.findByEmail(userInfo.getEmail());

        if (userOptional.isEmpty()) {
            return SignResult.fail(USER_NOT_FOUND.getValue(),data);
        }
        User user = userOptional.get();
        data.put(USER_VO.getValue(), user.toVo());

        try{
            userRepository.delete(user);
            userTemplate.delete(userInfo.getEmail());
        } catch (Exception e) {
           return SignResult.fail(e.getMessage(),data);
        }

        return SignResult.success(USER_DELETED.getValue(),data);
    }

    @Override
    public SignResult<Map<String, Object>> activate(ActivateDto dto) {
        return null;
    }

    @Override
    public SignResult<Map<String, Object>> duplicate(DuplicateDto dto) {
        return null;
    }

    @Override
    public SignResult<Map<String, Object>> socialSignUp() {
        return null;
    }

    @Override
    public boolean supports(Role role) {
        return Role.USER.equals(role);
    }
}
