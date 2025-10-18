package kakao.festapick.permission.fmpermission.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.net.URI;
import kakao.festapick.global.dto.ApiResponseDto;
import kakao.festapick.permission.fmpermission.dto.FMPermissionRequestDto;
import kakao.festapick.permission.fmpermission.dto.FMPermissionResponseDto;
import kakao.festapick.permission.fmpermission.service.FMPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fm-permissions")
@Tag(name = "FestivalManager Permission API", description = "축제 관리자(role 변경)신청을 위한 API")
public class FMPermissionController {

    private final FMPermissionService fmPermissionService;

    @Operation(
            summary = "FESTIVAL_MANAGER로 승급 신청",
            security = @SecurityRequirement(name = "JWT")
    )
    @PostMapping
    public ResponseEntity<Void> applyFMPermission(
            @AuthenticationPrincipal Long userId,
            @Valid @RequestBody FMPermissionRequestDto fmPermissionRequestDto
    ) {
        Long savedId = fmPermissionService.createFMPermission(
                userId,
                fmPermissionRequestDto.documents(),
                fmPermissionRequestDto.department()
        );
        return ResponseEntity.created(URI.create("/api/fm-permissions/" + savedId)).build();
    }

    @Operation(
            summary = "내가 신청한 목록 조회하기",
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/my")
    public ResponseEntity<ApiResponseDto<FMPermissionResponseDto>> getMyFMPermission(
            @AuthenticationPrincipal Long userId
    ){
        FMPermissionResponseDto response = fmPermissionService.getFMPermissionByUserId(userId);
        ApiResponseDto<FMPermissionResponseDto> responseDto = new ApiResponseDto<>(response);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @Operation(
            summary = "신청 시, 제출한 서류 변경하기",
            security = @SecurityRequirement(name = "JWT")
    )
    @PatchMapping("/my")
    public ResponseEntity<ApiResponseDto<FMPermissionResponseDto>> updateDocument(
            @AuthenticationPrincipal Long userId,
            @RequestBody @Valid FMPermissionRequestDto requestDto
    ){
        FMPermissionResponseDto fmPermissionResponseDto = fmPermissionService.updateDocuments(userId, requestDto.documents());
        return ResponseEntity.ok(new ApiResponseDto<>(fmPermissionResponseDto));
    }

    @Operation(
            summary = "신청 내역 삭제하기",
            security = @SecurityRequirement(name = "JWT")
    )
    @DeleteMapping("/my")
    public ResponseEntity<Void> removeFMPermission(
            @AuthenticationPrincipal Long userId
    ){
        fmPermissionService.removeFMPermission(userId);
        return ResponseEntity.noContent().build();
    }

    @Operation(
            summary = "나의 축제 매니저 승급 신청 중복 확인을 위한 API",
            security = @SecurityRequirement(name = "JWT")
    )
    @GetMapping("/check")
    public ResponseEntity<ApiResponseDto<Boolean>> checkDuplicateFMPermission(
            @AuthenticationPrincipal Long userId
    ){
        Boolean checkDuplicate = fmPermissionService.checkFMPermission(userId);
        return ResponseEntity.ok(new ApiResponseDto<>(checkDuplicate));
    }


}
