package kakao.festapick.permission.festivalpermission.controller;

import jakarta.validation.Valid;
import java.net.URI;
import kakao.festapick.permission.festivalpermission.dto.FestivalPermissionRequestDto;
import kakao.festapick.permission.festivalpermission.service.FestivalPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/festival-permission")
@PreAuthorize("hasRole('ROLE_FESTIVAL_MANAGER')")
public class FestivalPermissionController {

    private final FestivalPermissionService festivalPermissionService;

    @PostMapping("/festival/{festivalId}")
    public ResponseEntity<Void> applyFestivalPermission(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long festivalId,
            @RequestBody @Valid FestivalPermissionRequestDto festivalPermissionRequestDto
    ){
        Long savedId = festivalPermissionService.createFestivalPermission(userId, festivalId, festivalPermissionRequestDto.documents());
        return ResponseEntity.created(URI.create("/api/festival-permission/" + savedId)).build();
    }

}
