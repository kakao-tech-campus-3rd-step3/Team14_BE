package kakao.festapick.permission.festivalpermission.controller;

import jakarta.validation.Valid;
import java.net.URI;
import kakao.festapick.dto.ApiResponseDto;
import kakao.festapick.permission.festivalpermission.dto.FestivalPermissionDetailDto;
import kakao.festapick.permission.festivalpermission.dto.FestivalPermissionRequestDto;
import kakao.festapick.permission.festivalpermission.dto.FestivalPermissionResponseListDto;
import kakao.festapick.permission.festivalpermission.service.FestivalPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/festival-permissions")
@PreAuthorize("hasRole('ROLE_FESTIVAL_MANAGER')")
public class FestivalPermissionController {

    private final FestivalPermissionService festivalPermissionService;

    //신청하기
    @PostMapping("/festival/{festivalId}")
    public ResponseEntity<Void> applyFestivalPermission(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long festivalId,
            @RequestBody @Valid FestivalPermissionRequestDto festivalPermissionRequestDto
    ){
        Long savedId = festivalPermissionService.createFestivalPermission(userId, festivalId, festivalPermissionRequestDto.documents());
        return ResponseEntity.created(URI.create("/api/festival-permission/" + savedId)).build();
    }

    //신청 목록 가져오기
    @GetMapping("/my")
    public Page<FestivalPermissionResponseListDto> getFestivalPermissions(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        Page<FestivalPermissionResponseListDto> pagedContent = festivalPermissionService.getFestivalPermissionsByUserId(userId, pageable);
        return pagedContent;
    }

    //신청서 조회
    @GetMapping("/my/{id}")
    public ResponseEntity<ApiResponseDto<FestivalPermissionDetailDto>> getFestivalPermissions(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ){
        FestivalPermissionDetailDto detailDto = festivalPermissionService.getFestivalPermissionByUserId(userId, id);
        return ResponseEntity.ok(new ApiResponseDto<>(detailDto));
    }

    //신청 업데이트
    @PatchMapping("/my/{id}")
    public ResponseEntity<ApiResponseDto<FestivalPermissionDetailDto>> updateFestivalPermission(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody @Valid FestivalPermissionRequestDto requestDto
    ) {
        FestivalPermissionDetailDto detailDto = festivalPermissionService.updateFestivalPermission(userId, id, requestDto.documents());
        return ResponseEntity.ok(new ApiResponseDto<>(detailDto));
    }

    //신청서를 삭제
    @DeleteMapping("/my/{id}")
    public ResponseEntity<Void> removeFestivalPermission(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ){
        festivalPermissionService.removeFestivalPermission(userId, id);
        return ResponseEntity.noContent().build();
    }

}
