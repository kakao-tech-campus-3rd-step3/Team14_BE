package kakao.festapick.wish.controller;

import kakao.festapick.wish.dto.WishResponseDto;
import kakao.festapick.wish.service.WishService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class WishController {

    private final WishService wishService;

    @PreAuthorize("isAuthenticated()")
    @PostMapping("/festivals/{festivalId}/wishes")
    public ResponseEntity<WishResponseDto> createWish(
            @AuthenticationPrincipal String identifier,
            @PathVariable Long festivalId) {
        return new ResponseEntity<>(wishService.createWish(festivalId, identifier),
                HttpStatus.CREATED);
    }

    @GetMapping("/wishes")
    public ResponseEntity<Page<WishResponseDto>> getWishes(
            @AuthenticationPrincipal String identifier,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable
            ) {
        return new ResponseEntity<>(wishService.getWishes(identifier, pageable),
                HttpStatus.OK);
    }

    @DeleteMapping("/wishes/{wishId}")
    public ResponseEntity<Void> removeWish(
            @PathVariable Long wishId,
            @AuthenticationPrincipal String identifier) {
        wishService.removeWish(wishId, identifier);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }

}
