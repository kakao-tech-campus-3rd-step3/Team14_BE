package kakao.festapick.permission.festivalpermission.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import kakao.festapick.global.dto.ApiResponseDto;
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
@Tag(name = "Festival Permission API", description = "축제 관리 신청을 위한 API")
public class FestivalPermissionController {

    private final FestivalPermissionService festivalPermissionService;

    @Operation(
            summary = "기존 축제에 관리자 신청하기",
            security = @SecurityRequirement(name = "JWT")
    )
    @PostMapping("/festival/{festivalId}")
    public ResponseEntity<Void> applyFestivalPermission(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long festivalId,
            @RequestBody @Valid FestivalPermissionRequestDto festivalPermissionRequestDto
    ){
        Long savedId = festivalPermissionService.createFestivalPermission(userId, festivalId, festivalPermissionRequestDto.documents());
        return ResponseEntity.created(URI.create("/api/festival-permission/" + savedId)).build();
    }

    @Operation(
            summary = "나의 축제 관리 신청 목록 가져오기",
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/my")
    public ResponseEntity<Page<FestivalPermissionResponseListDto>> getMyFestivalPermissions(
            @AuthenticationPrincipal Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ){
        Pageable pageable = PageRequest.of(page, size);
        Page<FestivalPermissionResponseListDto> pagedContent = festivalPermissionService.getMyFestivalPermissionsByUserId(userId, pageable);
        return ResponseEntity.ok(pagedContent);
    }

    @Operation(
            summary = "Festival Permission 단건 조회",
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponseDto<FestivalPermissionDetailDto>> getFestivalPermission(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ){
        FestivalPermissionDetailDto detailDto = festivalPermissionService.getFestivalPermissionByUserId(userId, id);
        return ResponseEntity.ok(new ApiResponseDto<>(detailDto));
    }

    @Operation(
            summary = "신청서 업데이트 - (증빙 서류 업데이트)",
            security = @SecurityRequirement(name = "JWT")
    )
    @PatchMapping("/{id}")
    public ResponseEntity<ApiResponseDto<FestivalPermissionDetailDto>> updateFestivalPermission(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody @Valid FestivalPermissionRequestDto requestDto
    ) {
        FestivalPermissionDetailDto detailDto = festivalPermissionService.updateFestivalPermission(userId, id, requestDto.documents());
        return ResponseEntity.ok(new ApiResponseDto<>(detailDto));
    }

    @Operation(
            summary = "신청서 삭제 (ACCEPTED된 경우 삭제 시, 해당 축제 관리자 권한 박탈)",
            security = @SecurityRequirement(name = "JWT")
    )
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> removeFestivalPermission(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ){
        festivalPermissionService.removeFestivalPermission(userId, id);
        return ResponseEntity.noContent().build();
    }

}
