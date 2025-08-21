package kakao.festapick.wish.controller;

import java.util.List;
import kakao.festapick.wish.dto.WishRequestDto;
import kakao.festapick.wish.dto.WishResponseDto;
import kakao.festapick.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wishes")
@RequiredArgsConstructor
public class WishController {

    private final WishService wishService;

    @PostMapping
    public ResponseEntity<WishResponseDto> createWish(
            @RequestBody WishRequestDto requestDto,
            @AuthenticationPrincipal String identifier) {
        return new ResponseEntity<>(wishService.createWish(requestDto, identifier),
                HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<WishResponseDto>> getWishes(
            @AuthenticationPrincipal String identifier) {
        System.out.println(identifier);
        return new ResponseEntity<>(wishService.getWishes(identifier),
                HttpStatus.OK);
    }

    @DeleteMapping("/{wishId}")
    public ResponseEntity<Void> removeWish(
            @PathVariable Long wishId,
            @AuthenticationPrincipal String identifier) {
        wishService.removeWish(wishId, identifier);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
