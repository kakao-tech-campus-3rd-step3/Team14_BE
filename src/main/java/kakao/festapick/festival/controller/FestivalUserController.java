package kakao.festapick.festival.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import kakao.festapick.festival.dto.FestivalCustomListResponse;
import kakao.festapick.global.dto.ApiResponseDto;
import kakao.festapick.festival.dto.FestivalCustomRequestDto;
import kakao.festapick.festival.dto.FestivalDetailResponseDto;
import kakao.festapick.festival.dto.FestivalListResponse;
import kakao.festapick.festival.dto.FestivalUpdateRequestDto;
import kakao.festapick.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@Validated
@RestController
@RequestMapping("/api/festivals")
@RequiredArgsConstructor
@Tag(name = "Festival API", description = "축제 도메인 API")
public class FestivalUserController {

    private final FestivalService festivalService;

    @Operation(
            summary = "축제 등록 기능",
            security = @SecurityRequirement(name = "JWT"))
    @PostMapping
    @PreAuthorize("hasRole('ROLE_FESTIVAL_MANAGER')")
    public ResponseEntity<Void> addFestival(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid FestivalCustomRequestDto requestDto
    ) {
        Long festivalId = festivalService.addCustomizedFestival(requestDto, userId);
        return ResponseEntity.created(URI.create("/api/festivals/" + festivalId)).build();
    }

    @Operation(summary = "해당 지역에서 현재 참여할 수 있는 축제 조회")
    @GetMapping("/area/{areaCode}")
    public ResponseEntity<Page<FestivalListResponse>> getCurrentFestivalByArea(
            @PathVariable int areaCode,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ){
        Page<FestivalListResponse> festivalResponseDtos = festivalService.findApprovedAreaAndDate(areaCode, PageRequest.of(page, size));
        return ResponseEntity.ok(festivalResponseDtos);
    }

    @Operation(summary = "축제 상세 조회")
    @GetMapping("/{festivalId}")
    public ResponseEntity<ApiResponseDto<FestivalDetailResponseDto>> getFestivalInfo(@PathVariable Long festivalId, @AuthenticationPrincipal Long userId) {
        FestivalDetailResponseDto festivalDetail = festivalService.findOneById(festivalId, userId);
        ApiResponseDto<FestivalDetailResponseDto> responseDto = new ApiResponseDto<>(festivalDetail);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(summary = "축제명으로 축제를 검색하는 기능")
    @GetMapping
    public ResponseEntity<Page<FestivalListResponse>> searchFestival(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            @RequestParam @NotBlank(message = "검색어를 필수로 입력해야 합니다.") String keyword
    ){
        Page<FestivalListResponse> festivalListResponses = festivalService.findFestivalByTitle(keyword, PageRequest.of(page, size));
        return ResponseEntity.ok(festivalListResponses);
    }

    @Operation(
            summary = "내가 관리하는 축제 조회",
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/my")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<FestivalListResponse>> getMyFestivals(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ){
        Page<FestivalListResponse> myFestivals = festivalService.findMyFestivals(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(myFestivals);
    }

    @Operation(
            summary = "내가 관리자인 축제 수정",
            security = @SecurityRequirement(name = "JWT")
    )
    @PatchMapping("/{festivalId}")
    @PreAuthorize("hasRole('ROLE_FESTIVAL_MANAGER')")
    public ResponseEntity<ApiResponseDto<FestivalDetailResponseDto>> updateFestivalInfo(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long festivalId,
            @RequestBody @Valid FestivalUpdateRequestDto requestDto
    ){
        FestivalDetailResponseDto festivalDetail = festivalService.updateFestival(userId, festivalId, requestDto);
        ApiResponseDto<FestivalDetailResponseDto> responseDto = new ApiResponseDto<>(festivalDetail);
        return ResponseEntity.ok(responseDto);
    }

    @Operation(
            summary = "내가 관리자인 축제 삭제",
            security = @SecurityRequirement(name = "JWT")
    )
    @DeleteMapping("/{festivalId}")
    @PreAuthorize("hasRole('ROLE_FESTIVAL_MANAGER')")
    public ResponseEntity<Void> removeFestival(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long festivalId
    ){
        festivalService.deleteFestivalForManager(userId, festivalId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "내가 등록한 축제 조회",
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/my/custom")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Page<FestivalCustomListResponse>> getMyCustomFestivals(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ){
        Page<FestivalCustomListResponse> myCustomFestivals = festivalService.findMyCustomFestivals(userId, PageRequest.of(page, size));
        return ResponseEntity.ok(myCustomFestivals);
    }

    //모든 지역의 축제 조회(승인된 축제만, for view Controller)
    @GetMapping("/all")
    public ResponseEntity<List<FestivalListResponse>> getApprovedFestivals(){
        List<FestivalListResponse> festivalResponseDtos = festivalService.findApproved();
        return ResponseEntity.ok(festivalResponseDtos);
    }

}
