package kakao.festapick.festivalnotice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import kakao.festapick.festivalnotice.dto.FestivalNoticeRequestDto;
import kakao.festapick.festivalnotice.dto.FestivalNoticeResponseDto;
import kakao.festapick.festivalnotice.service.FestivalNoticeService;
import kakao.festapick.global.dto.ApiResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/api/festivals")
@RequiredArgsConstructor
@Tag(name = "Festival Notice API", description = "축제 공지를 위한 API")
public class FestivalNoticeController {

    private final FestivalNoticeService festivalNoticeService;

    @Operation(
            summary = "축제 공지 작성하기",
            security = @SecurityRequirement(name = "JWT")
    )
    @PreAuthorize("hasRole('ROLE_FESTIVAL_MANAGER')")
    @PostMapping("/{festivalId}/notices")
    public ResponseEntity<Void> addNotice(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid FestivalNoticeRequestDto requestDto,
            @PathVariable Long festivalId
    ) {
        Long saveId = festivalNoticeService.addFestivalNotice(festivalId, userId, requestDto);
        return ResponseEntity.created(URI.create("/api/festival/notices/" + saveId)).build();
    }

    @Operation(
            summary = "축제를 기준으로 공지사항 가져오기",
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/{festivalId}/notices")
    public ResponseEntity<Page<FestivalNoticeResponseDto>> getFestivalNotices(
            @PathVariable Long festivalId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {
        Page<FestivalNoticeResponseDto> pagedRequestDto =
                festivalNoticeService.getFestivalNotices(festivalId, PageRequest.of(page, size));
        return ResponseEntity.ok(pagedRequestDto);
    }

    @Operation(
            summary = "공지 사항 수정 하기",
            security = @SecurityRequirement(name = "JWT")
    )
    @PreAuthorize("hasRole('ROLE_FESTIVAL_MANAGER')")
    @PutMapping("/notices/{id}")
    public ResponseEntity<ApiResponseDto<FestivalNoticeResponseDto>> updateFestivalNotice(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody FestivalNoticeRequestDto requestDto
    ) {
        FestivalNoticeResponseDto responseDto = festivalNoticeService.updateFestivalNotice(id, userId, requestDto);
        return ResponseEntity.ok(new ApiResponseDto<>(responseDto));
    }

    //삭제
    @Operation(
            summary = "공지 사항 삭제",
            security = @SecurityRequirement(name = "JWT")
    )
    @PreAuthorize("hasRole('ROLE_FESTIVAL_MANAGER')")
    @DeleteMapping("/notices/{id}")
    public ResponseEntity<Void> removeFestivalNotice(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ) {
        festivalNoticeService.deleteFestivalNotice(id, userId);
        return ResponseEntity.noContent().build();
    }

}
