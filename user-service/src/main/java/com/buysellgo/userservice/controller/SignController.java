package com.buysellgo.userservice.controller;

import com.buysellgo.userservice.common.configs.SocialLoginProperties;
import com.buysellgo.userservice.common.dto.CommonResDto;
import com.buysellgo.userservice.common.entity.Role;
import com.buysellgo.userservice.common.exception.CustomException;
import com.buysellgo.userservice.controller.dto.*;
import com.buysellgo.userservice.strategy.sign.common.SignContext;
import com.buysellgo.userservice.strategy.sign.dto.*;
import com.buysellgo.userservice.strategy.sign.common.SignResult;
import com.buysellgo.userservice.strategy.sign.common.SignStrategy;


import com.buysellgo.userservice.strategy.social.common.SocialLoginContext;
import com.buysellgo.userservice.strategy.social.common.SocialLoginResult;
import com.buysellgo.userservice.strategy.social.common.SocialLoginStrategy;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

import static com.buysellgo.userservice.common.util.CommonConstant.*;

@Slf4j
@RestController
@RequestMapping("/sign")
@RequiredArgsConstructor
public class SignController {
    private final SignContext signContext;
    private final SocialLoginContext socialLoginContext;
    private final SocialLoginProperties socialLoginProperties;

    @Operation(summary = "회원가입 요청(회원)")
    @PostMapping("/user")
    public ResponseEntity<CommonResDto> userSign(@Valid @RequestBody UserCreateReq req) {
        UserSignUpDto signUpDto = UserSignUpDto.from(req);
        SignStrategy<Map<String, Object>> strategy = signContext.getStrategy(Role.USER);
        SignResult<Map<String, Object>> result = strategy.signUp(signUpDto);

        if(!result.success()) {
            //에러처리
            throw new CustomException(result.message());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CommonResDto(HttpStatus.CREATED, "회원가입 성공(회원)", result.data()));
    }

    @Operation(summary = "회원가입 요청(판매자)")
    @PostMapping("/seller")
    public ResponseEntity<CommonResDto> sellerSign(@Valid @RequestBody SellerCreateReq req) {
        SellerSignUpDto signUpDto = SellerSignUpDto.from(req);
        SignStrategy<Map<String, Object>> strategy = signContext.getStrategy(Role.SELLER);
        SignResult<Map<String, Object>> result = strategy.signUp(signUpDto);

        if(!result.success()) {
           throw new CustomException(result.message());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CommonResDto(HttpStatus.CREATED, "회원가입 성공(판매자)", result.data()));
    }

    @Operation(summary = "회원가입 요청(관리자)")
    @PostMapping("/admin")
    public ResponseEntity<CommonResDto> adminSign(@Valid @RequestBody AdminCreateReq req) {
        AdminSignUpDto signUpDto = AdminSignUpDto.from(req);
        SignStrategy<Map<String, Object>> strategy = signContext.getStrategy(Role.ADMIN);
        SignResult<Map<String, Object>> result = strategy.signUp(signUpDto);

        if(!result.success()) {
            throw new CustomException(result.message());
        }

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(new CommonResDto(HttpStatus.CREATED, "회원가입 성공(관리자)", result.data()));
    }

    @Operation(summary = "회원탈퇴 요청(회원)")
    @DeleteMapping("/user")
    public ResponseEntity<CommonResDto> userDelete(
            @RequestHeader(value = "Authorization", required = true) String accessToken) {
        if (!accessToken.startsWith(BEARER_PREFIX.getValue())) {
            throw new IllegalArgumentException("잘못된 Authorization 헤더 형식입니다.");
        }

        String token = accessToken.replace(BEARER_PREFIX.getValue(), "");
        SignStrategy<Map<String, Object>> strategy = signContext.getStrategy(Role.USER);
        SignResult<Map<String, Object>> result = strategy.withdraw(token);

        if (!result.success()) {
            throw new CustomException(result.message());
        }
        return ResponseEntity.status(HttpStatus.OK)
            .body(new CommonResDto(HttpStatus.OK, "회원 탈퇴 완료(회원)", result.data()));
    }

    @Operation(summary = "회원탈퇴 요청(판매자)")
    @DeleteMapping("/seller")
    public ResponseEntity<CommonResDto> sellerDelete(
            @RequestHeader(value = "Authorization", required = true) String accessToken){
        if(!accessToken.startsWith(BEARER_PREFIX.getValue())) {
            throw new IllegalArgumentException("잘못된 Authorization 헤더 형식입니다.");
        }

        String token = accessToken.replace(BEARER_PREFIX.getValue(), "");
        SignStrategy<Map<String, Object>> strategy = signContext.getStrategy(Role.SELLER);
        SignResult<Map<String, Object>> result = strategy.withdraw(token);

        if(!result.success()) {
            throw new CustomException(result.message());
        }
        return ResponseEntity.status(HttpStatus.OK)
                .body(new CommonResDto(HttpStatus.OK,"회원 탈퇴 완료(판매자)", result.data()));
    }

    @Operation(summary = "중복 검사")
    @GetMapping("/duplicate")
    public ResponseEntity<CommonResDto> checkDuplicate(@Valid @RequestBody CheckDuplicateReq req) {

        SignStrategy<Map<String, Object>> strategy = signContext.getStrategy(req.role());
        SignResult<Map<String, Object>> result = strategy.duplicate(DuplicateDto.from(req));

        if(!result.success()) {
            throw new CustomException(result.message());
        }

        return ResponseEntity.status(HttpStatus.OK)
            .body(new CommonResDto(HttpStatus.OK, "중복 검사 완료", result.data()));
    }

    @Operation(summary = "회원 활성화 및 판매자 승인(관리자)")
    @PutMapping("/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto> activate(@RequestBody ActivateReq req) {
        SignStrategy<Map<String, Object>> strategy = signContext.getStrategy(req.role());
        SignResult<Map<String, Object>> result = strategy.activate(ActivateDto.from(req));

        if(!result.success()) {
            throw new CustomException(result.message());
        }

        return ResponseEntity.status(HttpStatus.OK)
            .body(new CommonResDto(HttpStatus.OK, "회원 활성화 및 판매자 승인 완료", result.data()));
    }

    @Operation(summary = "회원 비활성화 및 판매자 비활성화(관리자)")
    @PutMapping("/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<CommonResDto> deactivate(@RequestBody ActivateReq req) {
        SignStrategy<Map<String, Object>> strategy = signContext.getStrategy(req.role());
        SignResult<Map<String, Object>> result = strategy.deactivate(ActivateDto.from(req));

        if(!result.success()) {
            throw new CustomException(result.message());
        }

        return ResponseEntity.status(HttpStatus.OK)
            .body(new CommonResDto(HttpStatus.OK, "회원 비활성화 및 판매자 비활성화 완료", result.data()));
    }

    @Operation(summary = "소셜 로그인(회원)")
    @GetMapping("/social")
    public ResponseEntity<CommonResDto> socialLogin(@RequestBody SocialLoginReq req) {
        String redirectUrl = socialLoginProperties.getRedirectUrl(req.provider());

        if(redirectUrl.equals(TYPE_NOT_SUPPORTED.getValue())) {
            throw new CustomException(TYPE_NOT_SUPPORTED.getValue());
        }

        return ResponseEntity.status(HttpStatus.OK)
            .body(new CommonResDto(HttpStatus.OK, "소셜 로그인 링크 전달", redirectUrl));
    }

    @Operation(summary = "소셜 로그인 콜백(회원)")
    @GetMapping("/{provider}")
    public ResponseEntity<CommonResDto> socialCallback(@PathVariable String provider, @RequestParam String code) {
        SocialLoginStrategy<Map<String,Object>> strategy = socialLoginContext.getStrategy(provider);
        SocialLoginResult<Map<String, Object>> result = strategy.execute(code);

        if(!result.success()) {
            throw new CustomException(result.message());
        }
        // 전달받은 회원정보를 토대로 회원존재여부 확인
        // 회원존재시 로그인 처리
        // 회원 존재하지 않을 시 회원가입 처리

        return ResponseEntity.status(HttpStatus.OK)
                .body(new CommonResDto(HttpStatus.OK, "소셜 로그인 콜백 완료", result.data()));
    }
}
