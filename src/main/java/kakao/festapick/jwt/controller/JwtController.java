package kakao.festapick.jwt.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import kakao.festapick.jwt.service.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class JwtController {

    private final JwtService jwtService;

    @PostMapping("/api/jwt/exchange")
    public ResponseEntity<Void> jwtExchange(HttpServletRequest request, HttpServletResponse response) {

        jwtService.exchangeToken(request, response);

        return  ResponseEntity.status(HttpStatus.OK).build();
    }
}
