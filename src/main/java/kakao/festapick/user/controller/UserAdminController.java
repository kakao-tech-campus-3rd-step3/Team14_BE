package kakao.festapick.user.controller;

import kakao.festapick.user.domain.UserRoleType;
import kakao.festapick.user.dto.UserResponseDtoForAdmin;
import kakao.festapick.user.dto.UserSearchCond;
import kakao.festapick.user.service.OAuth2UserService;
import kakao.festapick.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequiredArgsConstructor
public class UserAdminController {

    private final OAuth2UserService oAuth2UserService;
    private final UserService userService;

    @GetMapping("/login")
    public String loginPage() {
        return "admin/login";
    }

    @GetMapping("/admin")
    public String adminPage() {
        return "admin/admin-home";
    }

    @GetMapping("/admin/users")
    public String userManagementPage(@RequestParam(required = false) String identifier,
                                     @RequestParam(required = false) String email,
                                     @RequestParam(required = false) UserRoleType role,
                                     @PageableDefault(page = 0, size = 10, sort = "id", direction = Sort.Direction.ASC)
                                     Pageable pageable,
                                     Model model) {


        Page<UserResponseDtoForAdmin> response = userService.findByIdentifierOrUserEmail(new UserSearchCond(identifier,email, role), pageable);

        model.addAttribute("pageData", response);
        model.addAttribute("identifier", identifier);
        model.addAttribute("email", email);
        model.addAttribute("role", role);

        return "admin/users-management";
    }

    @PostMapping("/admin/users/{id}")
    public String deleteUser(@PathVariable Long id) {

        userService.deleteUser(id);

        return "redirect:/admin/users";
    }

    @PostMapping("/admin/users/{id}/role")
    public String changeRole(@PathVariable Long id, @RequestParam UserRoleType role) {

        System.out.println(id);
        System.out.println(role);
        userService.changeUserRole(id, role);

        return "redirect:/admin/users";
    }
}
