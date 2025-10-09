package kakao.festapick.permission.fmpermission.controller;

import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import java.net.URI;
import kakao.festapick.dto.ApiResponseDto;
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
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/fm-permissions")
public class FMPermissionController {

    private final FMPermissionService fmPermissionService;

    @Operation(summary = "FESTIVAL_MANAGER로 승급 신청")
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

    @Operation(summary = "내가 신청한 목록 조회하기")
    @GetMapping("/my")
    private ResponseEntity<ApiResponseDto<FMPermissionResponseDto>> getMyFMPermission(
            @AuthenticationPrincipal Long userId
    ){
        FMPermissionResponseDto response = fmPermissionService.getFMPermissionByUserId(userId);
        ApiResponseDto<FMPermissionResponseDto> responseDto = new ApiResponseDto<>(response);
        return ResponseEntity.status(HttpStatus.OK).body(responseDto);
    }

    @Operation(summary = "신청 시, 제출한 서류 변경하기")
    @PatchMapping("/my/{id}")
    private ResponseEntity<ApiResponseDto<FMPermissionResponseDto>> updateDocument(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id,
            @RequestBody @Valid FMPermissionRequestDto requestDto
    ){
        FMPermissionResponseDto fmPermissionResponseDto = fmPermissionService.modifyDocuments(userId, id, requestDto.documents());
        return ResponseEntity.ok(new ApiResponseDto<>(fmPermissionResponseDto));
    }

    @Operation(summary = "신청 내역 삭제하기")
    @DeleteMapping("/my/{id}")
    private void cancelFMPermission(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long id
    ){
        fmPermissionService.removeMyFMPermission(userId, id);
    }

}
