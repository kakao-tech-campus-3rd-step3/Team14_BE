package kakao.festapick.admin.controller;

import kakao.festapick.admin.dto.UserSearchCond;
import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.dto.UserResponseDto;
import kakao.festapick.user.service.OAuth2UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequiredArgsConstructor
public class AdminViewController {

    private final OAuth2UserService oAuth2UserService;

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "/admin/admin";
    }

    @GetMapping("/admin/users")
    public String userManagementPage(@RequestParam(required = false) String identifier,
                                     @RequestParam(required = false) String email,
                                     @RequestParam(required = false) UserRoleType role,
                                     @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
                                     Pageable pageable,
                                     Model model) {


        Page<UserResponseDto> response = oAuth2UserService.findByIdentifierOrUserEmail(new UserSearchCond(identifier,email, role), pageable);

        model.addAttribute("pageData", response);
        model.addAttribute("identifier", identifier);
        model.addAttribute("email", email);
        model.addAttribute("role", role);

        return "admin/users-management";
    }

    @PostMapping("/admin/users/{id}")
    public String deleteUser(@PathVariable Long id) {

        oAuth2UserService.deleteUser(id);

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam UserRoleType role) {

        System.out.println(id);
        System.out.println(role);
        oAuth2UserService.changeUserRole(id, role);

        return "redirect:/admin/users";
    }
}
