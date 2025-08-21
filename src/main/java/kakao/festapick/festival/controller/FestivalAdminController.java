package kakao.festapick.festival.controller;

import java.util.List;
import kakao.festapick.festival.dto.FestivalResponseDto;
import kakao.festapick.festival.dto.FestivalStateDto;
import kakao.festapick.festival.service.FestivalService;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/admin/festivals")
public class FestivalAdminController {

    private final FestivalService festivalService;

    public FestivalAdminController(FestivalService festivalService) {
        this.festivalService = festivalService;
    }

    //state와 상관 없이 모든 축제를 조회
    @GetMapping("/{festivalId}")
    public ResponseEntity<FestivalResponseDto> getFestivalInfo(@PathVariable Long festivalId){
        FestivalResponseDto festivalInfo = festivalService.findOneById(festivalId);
        return ResponseEntity.ok(festivalInfo);
    }

    //state와 상관 없이 모든 축제를 조회
    @GetMapping("/all")
    public ResponseEntity<List<FestivalResponseDto>> getFestivals(){
        List<FestivalResponseDto> festivalResponseDtos = festivalService.findAll();
        return ResponseEntity.ok(festivalResponseDtos);
    }

    //축제 승인
    @PatchMapping("/state/{festivalId}")
    public ResponseEntity<FestivalResponseDto> updateFestivalState(
            @PathVariable Long festivalId,
            @RequestBody FestivalStateDto state
    ){
        FestivalResponseDto responseDto =  festivalService.updateState(festivalId, state);
        return ResponseEntity.ok(responseDto);
    }

}
