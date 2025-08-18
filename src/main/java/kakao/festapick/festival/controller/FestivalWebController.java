package kakao.festapick.festival.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class FestivalWebController {

    @GetMapping("/festivals")
    public String festivalPage() {
        return "festival/festivals"; // templates/festivals.html
    }

}
