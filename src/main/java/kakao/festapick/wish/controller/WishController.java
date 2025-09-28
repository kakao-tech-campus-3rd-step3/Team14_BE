package kakao.festapick.wish.controller;

import jakarta.validation.constraints.Max;
import kakao.festapick.dto.ApiResponseDto;
import kakao.festapick.wish.dto.WishResponseDto;
import kakao.festapick.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WishController {

    private final WishService wishService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/festivals/{festivalId}/wishes")
    public ResponseEntity<ApiResponseDto<WishResponseDto>> createWish(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long festivalId) {
        WishResponseDto response = wishService.createWish(festivalId, userId);
        ApiResponseDto<WishResponseDto> responseDto = new ApiResponseDto<>(response);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @GetMapping("/wishes/my")
    public ResponseEntity<Page<WishResponseDto>> getWishes(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0", required = false) int page,
            @Max(value = 1000)
            @RequestParam(defaultValue = "5", required = false) int size
            ) {
        return new ResponseEntity<>(wishService.getWishes(userId, PageRequest.of(page, size)),
                HttpStatus.OK);
    }

    @DeleteMapping("/wishes/{wishId}")
    public ResponseEntity<Void> removeWish(
            @PathVariable Long wishId,
            @AuthenticationPrincipal Long userId) {
        wishService.removeWish(wishId, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
