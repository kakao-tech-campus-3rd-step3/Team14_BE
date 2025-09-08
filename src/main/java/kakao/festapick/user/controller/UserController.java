package kakao.festapick.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
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
    public ResponseEntity<Void> withDrawMember(@AuthenticationPrincipal String identifier,
                                               HttpServletResponse response) {

        userService.withDraw(identifier,response);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PatchMapping("/profileImage") // 프로필 이미지 변경
    public ResponseEntity<Void> changeProfileImage(@Valid @RequestBody FileUploadRequest fileUploadRequest,
                                                   @AuthenticationPrincipal String identifier) {

        userService.changeProfileImage(identifier, fileUploadRequest);

        return ResponseEntity.status(HttpStatus.OK).build();
    }

    @GetMapping("/my") // 본인 정보 조회
    public ResponseEntity<UserResponseDto> getMyInfo(@AuthenticationPrincipal String identifier) {

        UserResponseDto response = userService.findMyInfo(identifier);

        return ResponseEntity.status(HttpStatus.OK).body(response);
    }

}
