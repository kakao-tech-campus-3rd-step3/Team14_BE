package kakao.festapick.festival.controller;


import jakarta.validation.Valid;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.dto.FestivalDetailResponseDto;
import kakao.festapick.festival.dto.FestivalListResponseForAdmin;
import kakao.festapick.festival.dto.FestivalSearchCondForAdmin;
import kakao.festapick.festival.dto.FestivalStateDto;
import kakao.festapick.festival.service.FestivalService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/festivals")
public class FestivalAdminController {

    private final FestivalService festivalService;

    public FestivalAdminController(FestivalService festivalService) {
        this.festivalService = festivalService;
    }

    @GetMapping("/{festivalId}")
    public String getFestivalInfo(@PathVariable Long festivalId, Model model){
        FestivalDetailResponseDto festivalInfo = festivalService.findOneById(festivalId);

        model.addAttribute("festival", festivalInfo);

        return  "admin/festival-detail";
    }

    //축제 조회
    @GetMapping
    public String getFestivals(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) FestivalState state,
            @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
            Pageable pageable,
            Model model
    ){
        Page<FestivalListResponseForAdmin> response = festivalService.findAllWithPage(new FestivalSearchCondForAdmin(title, state), pageable);

        model.addAttribute("pageData", response);
        model.addAttribute("title", title);
        model.addAttribute("state", state);

        return "admin/festival-management";
    }

    //축제 상태 변경
    @PostMapping("/{festivalId}/state")
    public String updateFestivalState(
            @PathVariable Long festivalId,
            @Valid @ModelAttribute FestivalStateDto state,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes
    ){
        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("errors", bindingResult.getAllErrors());
            return "redirect:/admin/festivals";
        }

        festivalService.updateState(festivalId, state);
        return "redirect:/admin/festivals";
    }

    @PostMapping("/{festivalId}")
    public String deleteFestival(@PathVariable Long festivalId){
        festivalService.deleteFestivalForAdmin(festivalId);
        return "redirect:/admin/festivals";
    }
}
