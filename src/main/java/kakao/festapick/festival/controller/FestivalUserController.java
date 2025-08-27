package kakao.festapick.festival.controller;

import jakarta.validation.Valid;
import java.net.URI;
import java.util.List;
import kakao.festapick.festival.dto.FestivalCustomRequestDto;
import kakao.festapick.festival.dto.FestivalDetailResponseDto;
import kakao.festapick.festival.dto.FestivalListResponse;
import kakao.festapick.festival.dto.FestivalUpdateRequestDto;
import kakao.festapick.festival.service.FestivalService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
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
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/festivals")
@RequiredArgsConstructor
public class FestivalUserController {

    private final FestivalService festivalService;

    //축제 등록
    @PostMapping
    @PreAuthorize("hasRole('ROLE_FESTIVAL_MANAGER')")
    public ResponseEntity<Void> addFestival(
            @AuthenticationPrincipal String identifier,
            @RequestBody @Valid FestivalCustomRequestDto requestDto
    ) {
        Long festivalId = festivalService.addCustomizedFestival(requestDto, identifier);
        return ResponseEntity.created(URI.create("/api/festivals/" + festivalId)).build();
    }

    //해당 지역에서 현재 열리고 있는 축제
    @GetMapping("/area/{areaCode}/current")
    public ResponseEntity<Page<FestivalListResponse>> getCurrentFestivalByArea(
            @PathVariable int areaCode,
            @PageableDefault(size = 5) Pageable pageable

    ){
        Page<FestivalListResponse> festivalResponseDtos = festivalService.findApprovedAreaAndDate(areaCode, pageable);
        return ResponseEntity.ok(festivalResponseDtos);
    }

    //축제 상세 조회
    @GetMapping("/{festivalId}")
    public ResponseEntity<FestivalDetailResponseDto> getFestivalInfo(@PathVariable Long festivalId){
        FestivalDetailResponseDto festivalDetail = festivalService.findOneById(festivalId);
        return ResponseEntity.ok(festivalDetail);
    }

    //자신이 올린 축제에 대해서만 수정 가능
    @PatchMapping("/{festivalId}")
    @PreAuthorize("hasRole('ROLE_FESTIVAL_MANAGER')")
    public ResponseEntity<FestivalDetailResponseDto> updateFestivalInfo(
            @AuthenticationPrincipal String identifier,
            @PathVariable Long festivalId,
            @RequestBody @Valid FestivalUpdateRequestDto requestDto
    ){
        FestivalDetailResponseDto responseDto =  festivalService.updateFestival(identifier, festivalId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    //자신이 올린 축제에 대해서만 삭제 가능
    @DeleteMapping("/{festivalId}")
    @PreAuthorize("hasRole('ROLE_FESTIVAL_MANAGER')")
    public ResponseEntity<Void> removeFestival(
            @AuthenticationPrincipal String identifier,
            @PathVariable Long festivalId
    ){
        festivalService.removeOne(identifier, festivalId);
        return ResponseEntity.noContent().build();
    }

    //모든 지역의 축제 조회(승인된 축제만, for view Controller)
    @GetMapping("/all")
    public ResponseEntity<List<FestivalListResponse>> getApprovedFestivals(){
        List<FestivalListResponse> festivalResponseDtos = festivalService.findApproved();
        return ResponseEntity.ok(festivalResponseDtos);
    }

}
