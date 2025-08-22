package kakao.festapick.festival.controller;

import java.net.URI;
import java.util.List;
import kakao.festapick.festival.dto.CustomFestivalRequestDto;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.dto.FestivalDetailResponse;
import kakao.festapick.festival.service.FestivalService;
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
@RequestMapping("/api/festivals")
public class FestivalUserController {

    private final FestivalService festivalService;

    public FestivalUserController(FestivalService festivalService) {
        this.festivalService = festivalService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ROLE_FESTIVAL_MANAGER')")
    public ResponseEntity<Long> addFestival(
            @AuthenticationPrincipal String identifier,
            @RequestBody CustomFestivalRequestDto requestDto
    ) {
        Long festivalId = festivalService.addCustomizedFestival(requestDto, identifier);
        return ResponseEntity.created(URI.create("/api/festivals/" + festivalId)).build();
    }

    //해당 지역에서 열리는 모든 축제
    @GetMapping("/approved/area/{areaCode}")
    public ResponseEntity<List<FestivalDetailResponse>> getFestivalByArea(@PathVariable String areaCode){
        List<FestivalDetailResponse> festivalResponseDtos = festivalService.findApprovedOneByArea(areaCode);
        return ResponseEntity.ok(festivalResponseDtos);
    }

    //해당 지역에서 현재 열리고 있는 축제
    @GetMapping("/approved/area/{areaCode}/current")
    public ResponseEntity<List<FestivalDetailResponse>> getCurrentFestivalByArea(@PathVariable String areaCode){
        List<FestivalDetailResponse> festivalResponseDtos = festivalService.findApprovedAreaAndDate(areaCode);
        return ResponseEntity.ok(festivalResponseDtos);
    }

    //모든 지역의 축제 조회(승인된 축제만)
    @GetMapping("/approved/all")
    public ResponseEntity<List<FestivalDetailResponse>> getApprovedFestivals(){
        List<FestivalDetailResponse> festivalResponseDtos = festivalService.findApproved();
        return ResponseEntity.ok(festivalResponseDtos);
    }

    //Keyword를 통한 축제 검색
    @GetMapping
    public ResponseEntity<List<FestivalDetailResponse>> getFestivalByKeyword(@RequestParam String keyword){
        List<FestivalDetailResponse> festivalResponseDtos = festivalService.findApprovedOneByKeyword(keyword);
        return ResponseEntity.ok(festivalResponseDtos);
    }

    //자신이 올린 축제에 대해서만 수정 가능
    @PatchMapping("/{festivalId}")
    @PreAuthorize("hasRole('ROLE_FESTIVAL_MANAGER')")
    public ResponseEntity<FestivalDetailResponse> updateFestivalInfo(
            @AuthenticationPrincipal String identifier,
            @PathVariable Long festivalId,
            @RequestBody FestivalRequestDto requestDto
    ){
        FestivalDetailResponse responseDto =  festivalService.updateFestival(identifier, festivalId, requestDto);
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

}
