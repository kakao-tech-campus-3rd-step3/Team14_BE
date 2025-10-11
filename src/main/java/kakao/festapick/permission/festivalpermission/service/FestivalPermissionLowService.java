package kakao.festapick.permission.festivalpermission.service;

import kakao.festapick.permission.festivalpermission.domain.FestivalPermission;
import kakao.festapick.permission.festivalpermission.repository.FestivalPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalPermissionLowService {

    private final FestivalPermissionRepository festivalPermissionRepository;

    public FestivalPermission save(FestivalPermission permission){
        return festivalPermissionRepository.save(permission);
    }

}
