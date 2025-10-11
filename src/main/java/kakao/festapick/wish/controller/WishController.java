package kakao.festapick.wish.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.constraints.Max;
import kakao.festapick.global.dto.ApiResponseDto;
import kakao.festapick.wish.dto.WishResponseDto;
import kakao.festapick.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WishController {

    private final WishService wishService;

    @Operation(
            summary = "축제 좋아요 기능(좋아요 생성)",
            security = @SecurityRequirement(name = "JWT")
    )
    @PreAuthorize("isAuthenticated()")
    @PostMapping("/festivals/{festivalId}/wishes")
    public ResponseEntity<ApiResponseDto<WishResponseDto>> createWish(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long festivalId) {
        WishResponseDto response = wishService.createWish(festivalId, userId);
        ApiResponseDto<WishResponseDto> responseDto = new ApiResponseDto<>(response);
        return new ResponseEntity<>(responseDto, HttpStatus.CREATED);
    }

    @Operation(
            summary = "내가 누른 좋아요 목록 가져오기",
            security = @SecurityRequirement(name = "JWT")
    )
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

    @Operation(
            summary = "id 기반 좋아요 삭제",
            security = @SecurityRequirement(name = "JWT")
    )
    @DeleteMapping("/wishes/{wishId}")
    public ResponseEntity<Void> removeWish(
            @PathVariable Long wishId,
            @AuthenticationPrincipal Long userId) {
        wishService.removeWishWithWishId(wishId, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

    @Operation(
            summary = "축제 id기반 좋아요 삭제",
            security = @SecurityRequirement(name = "JWT")
    )
    @DeleteMapping("/festivals/{festivalId}/wishes/my")
    public ResponseEntity<Void> removeMyWishes(@PathVariable Long festivalId, @AuthenticationPrincipal Long userId) {

        wishService.removeWishWithFestivalId(festivalId, userId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
