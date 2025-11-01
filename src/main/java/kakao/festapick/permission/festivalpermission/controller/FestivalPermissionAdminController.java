package kakao.festapick.permission.festivalpermission.controller;

import java.util.Map;
import kakao.festapick.global.exception.AuthenticationException;
import kakao.festapick.global.exception.BadRequestException;
import kakao.festapick.permission.PermissionState;
import kakao.festapick.permission.festivalpermission.dto.FestivalPermissionAdminListDto;
import kakao.festapick.permission.festivalpermission.dto.FestivalPermissionDetailDto;
import kakao.festapick.permission.festivalpermission.service.FestivalPermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Slf4j
@Controller
@RequestMapping("/admin/festival-permissions")
@RequiredArgsConstructor
public class FestivalPermissionAdminController {

    private final FestivalPermissionService festivalPermissionService;

    @GetMapping
    public String getAllFMPermissions(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size,
            Model model
    ){
        Pageable pageable = PageRequest.of(page, size);
        Page<FestivalPermissionAdminListDto> permissionList = festivalPermissionService.getAllFestivalPermissions(pageable);
        model.addAttribute("pageData", permissionList);
        return "admin/festival-permission-management";
    }

    //상세 조회
    @GetMapping("/{id}")
    public String getFMPermission(
            @PathVariable Long id,
            Model model
    ){
        FestivalPermissionDetailDto responseDto = festivalPermissionService.getFestivalPermissionById(id);
        model.addAttribute("permission", responseDto);
        return "admin/festival-permission-detail";
    }

    // 관리
    @PostMapping("/{permissionId}/state")
    public String updateFMPermissionState(
            @PathVariable Long permissionId,
            @RequestParam PermissionState permissionState
    ){
        try{
            festivalPermissionService.updateFestivalPermissionState(permissionId, permissionState);
        }
        catch (BadRequestException e){
            log.info("[관리자 페이지 - Festival Manager] {}", e.getExceptionCode().getErrorMessage());
        }
        return "redirect:/admin/festival-permissions/" + permissionId;
    }

}
