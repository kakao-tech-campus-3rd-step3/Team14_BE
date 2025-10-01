package kakao.festapick.user.controller;

import kakao.festapick.dto.ApiResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    // 로그인 여부 확인 api for 프론트 개발 편의성
    @GetMapping("/api/login/status")
    public ResponseEntity<ApiResponseDto<Map<String, Boolean>>> checkIsLogin(@AuthenticationPrincipal Long userId) {

        boolean isLogin = userId != null;

        return ResponseEntity.status(HttpStatus.OK).body(new ApiResponseDto<>(Map.of("isLogin",isLogin)));
    }
}
