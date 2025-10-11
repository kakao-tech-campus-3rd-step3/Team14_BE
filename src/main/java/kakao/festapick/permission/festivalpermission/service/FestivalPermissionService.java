package kakao.festapick.permission.festivalpermission.service;

import java.util.List;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.permission.PermissionFileUploader;
import kakao.festapick.permission.festivalpermission.domain.FestivalPermission;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class FestivalPermissionService {

    private final FestivalPermissionLowService festivalPermissionLowService;
    private final FestivalLowService festivalLowService;
    private final UserLowService userLowService;

    private final PermissionFileUploader permissionFileUploader;

    public Long createFestivalPermission(Long userId, Long festivalId, List<FileUploadRequest> documents){
        UserEntity user = userLowService.findById(userId);
        Festival festival = festivalLowService.findFestivalById(festivalId);

        FestivalPermission festivalPermission = new FestivalPermission(user, festival);
        Long savedId = festivalPermissionLowService.save(festivalPermission).getId();

        permissionFileUploader.saveFiles(documents, savedId, DomainType.FESTIVAL_PERMISSION);
        return savedId;
    }

}
