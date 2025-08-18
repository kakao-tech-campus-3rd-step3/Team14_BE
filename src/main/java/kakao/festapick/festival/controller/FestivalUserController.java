package kakao.festapick.festival.controller;

import java.util.List;
import kakao.festapick.festival.dto.FestivalResponseDto;
import kakao.festapick.festival.service.FestivalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;


@RestController
@RequestMapping("/api/festivals/approved")
public class FestivalUserController {

    private final FestivalService festivalService;

    public FestivalUserController(FestivalService festivalService) {
        this.festivalService = festivalService;
    }

    @GetMapping("/areacode/{areaCode}")
    public ResponseEntity<List<FestivalResponseDto>> getFestivalByArea(@PathVariable String areaCode){
        List<FestivalResponseDto> festivalResponseDtos = festivalService.findApprovedOneByArea(areaCode);
        return ResponseEntity.ok(festivalResponseDtos);
    }

    @GetMapping("/areacode/{areaCode}/current")
    public ResponseEntity<List<FestivalResponseDto>> getCurrentFestivalByArea(@PathVariable String areaCode){
        List<FestivalResponseDto> festivalResponseDtos = festivalService.findByAreaAndDate(areaCode);
        return ResponseEntity.ok(festivalResponseDtos);
    }

    @GetMapping("/all")
    public ResponseEntity<List<FestivalResponseDto>> getApprovedFestivals(){
        List<FestivalResponseDto> festivalResponseDtos = festivalService.findApproved();
        return ResponseEntity.ok(festivalResponseDtos);
    }

    @GetMapping
    public ResponseEntity<List<FestivalResponseDto>> getFestivalByKeyword(@RequestParam String keyword){
        List<FestivalResponseDto> festivalResponseDtos = festivalService.findApprovedOneByKeyword(keyword);
        return ResponseEntity.ok(festivalResponseDtos);
    }
}
