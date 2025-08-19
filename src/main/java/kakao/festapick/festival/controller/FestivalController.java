package kakao.festapick.festival.controller;

import java.util.List;
import kakao.festapick.festival.dto.CustomFestivalRequestDto;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.dto.FestivalResponseDto;
import kakao.festapick.festival.dto.FestivalStateDto;
import kakao.festapick.festival.service.FestivalService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/api/festivals")
public class FestivalController {

    private final FestivalService festivalService;

    public FestivalController(FestivalService festivalService) {
        this.festivalService = festivalService;
    }

    @PostMapping
    public ResponseEntity<Long> addFestival(@RequestBody CustomFestivalRequestDto requestDto){
        Long festivalId = festivalService.addCustomizedFestival(requestDto);
        return new ResponseEntity<>(festivalId, HttpStatus.CREATED);
    }

    @GetMapping("/{festivalId}")
    public ResponseEntity<FestivalResponseDto> getFestivalInfo(@PathVariable Long festivalId){
        FestivalResponseDto festivalInfo = festivalService.findOneById(festivalId);
        return ResponseEntity.ok(festivalInfo);
    }

    @GetMapping("/all")
    public ResponseEntity<List<FestivalResponseDto>> getFestivals(){
        List<FestivalResponseDto> festivalResponseDtos = festivalService.findAll();
        return ResponseEntity.ok(festivalResponseDtos);
    }

    //official한 사람만
    @PatchMapping("/{festivalId}")
    public ResponseEntity<FestivalResponseDto> updateFestivalInfo(
            @PathVariable Long festivalId,
            @RequestBody FestivalRequestDto requestDto
    ){
        FestivalResponseDto responseDto =  festivalService.updateFestival(festivalId, requestDto);
        return ResponseEntity.ok(responseDto);
    }

    //admin만
    @PatchMapping("/state/{festivalId}")
    public ResponseEntity<FestivalResponseDto> updateFestivalState(
            @PathVariable Long festivalId,
            @RequestBody FestivalStateDto state
    ){
        System.out.println("here = " + state);
        FestivalResponseDto responseDto =  festivalService.updateState(festivalId, state);
        return ResponseEntity.ok(responseDto);
    }

    //자기가 올린 축제만 또는 admin만/
    @DeleteMapping("/{festivalId}")
    public ResponseEntity<Void> removeFestival(@PathVariable Long festivalId){
        festivalService.removeOne(festivalId);
        return ResponseEntity.noContent().build();
    }

}
