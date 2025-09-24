package kakao.festapick.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import kakao.festapick.dto.ApiResponseDto;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.user.dto.UserResponseDto;
import kakao.festapick.user.service.OAuth2UserService;
import kakao.festapick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final OAuth2UserService oAuth2UserService;
    private final UserService userService;

    @DeleteMapping
    public ResponseEntity<Void> withDrawMember(@AuthenticationPrincipal Long userId,
                                               HttpServletResponse response) {

        userService.withDraw(userId,response);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/profileImage") // 프로필 이미지 변경
    public ResponseEntity<Void> changeProfileImage(@Valid @RequestBody FileUploadRequest fileUploadRequest,
                                                   @AuthenticationPrincipal Long userId) {

        userService.changeProfileImage(userId, fileUploadRequest);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/my") // 본인 정보 조회
    public ResponseEntity<ApiResponseDto<UserResponseDto>> getMyInfo(@AuthenticationPrincipal Long userId) {

        UserResponseDto response = userService.findMyInfo(userId);
        ApiResponseDto<UserResponseDto> responseDto = new ApiResponseDto<>(response);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

}
