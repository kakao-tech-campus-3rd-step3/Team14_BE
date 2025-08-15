package kakao.festapick.user.controller;

import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.user.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final OAuth2UserService oAuth2UserService;

    @DeleteMapping
    public ResponseEntity<Void> withDrawMember(@AuthenticationPrincipal String identifier,
                                               HttpServletResponse response) {

        oAuth2UserService.withDraw(identifier,response);

        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

}
