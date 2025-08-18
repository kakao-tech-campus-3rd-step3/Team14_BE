package kakao.festapick.festival.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/view")
public class FestivalViewController {

    @GetMapping("/festivals")
    public String festivalPage() {
        return "festival/festivals";
    }

}
