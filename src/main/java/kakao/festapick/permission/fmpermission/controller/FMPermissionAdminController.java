package kakao.festapick.permission.fmpermission.controller;

import kakao.festapick.permission.PermissionState;
import kakao.festapick.permission.fmpermission.dto.FMPermissionAdminListResponseDto;
import kakao.festapick.permission.fmpermission.dto.FMPermissionResponseDto;
import kakao.festapick.permission.fmpermission.service.FMPermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@Validated
@RequiredArgsConstructor
@RequestMapping("/admin/fm-permissions")
public class FMPermissionAdminController {

    private final FMPermissionService fmPermissionService;

    @GetMapping
    public String getAllFMPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ){
        Page<FMPermissionAdminListResponseDto> response =  fmPermissionService.getAllFMPermission(PageRequest.of(page, size));
        model.addAttribute("pageData", response);
        return "admin/fm-permission-management";
    }

    @GetMapping("/{id}")
    public String getFMPermission(
            @PathVariable Long id,
            Model model
    ){
        FMPermissionResponseDto responseDto = fmPermissionService.getFMPermissionById(id);
        model.addAttribute("permission", responseDto);
        return "admin/fm-permission-detail";
    }

    @PostMapping("/{permissionId}/state")
    public String updateFMPermissionState(
            @PathVariable Long permissionId,
            @RequestParam PermissionState permissionState
    ){
        fmPermissionService.updateState(permissionId, permissionState);
        return "redirect:/admin/fm-permissions/" + permissionId;
    }

}