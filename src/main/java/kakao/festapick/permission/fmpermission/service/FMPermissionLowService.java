package kakao.festapick.permission.fmpermission.service;

import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.permission.fmpermission.domain.FMPermission;
import kakao.festapick.permission.fmpermission.repository.FMPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FMPermissionLowService {

    private final FMPermissionRepository fmPermissionRepository;

    public FMPermission saveFMPermission(FMPermission fmPermission){
        return fmPermissionRepository.save(fmPermission);
    }

    public boolean existsByUserId(Long userId){
        return fmPermissionRepository.existsFMPermissionByUserId(userId);
    }

    public FMPermission findFMPermissionById(Long id){
        return fmPermissionRepository.findFMPermissionById(id)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FM_PERMISSION_NOT_FOUND));
    }

    public FMPermission findFMPermissionByUserId(Long userId){
        return fmPermissionRepository.findByUserId(userId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FM_PERMISSION_NOT_FOUND));
    }

    public Page<FMPermission> findAll(Pageable pageable){
        return fmPermissionRepository.findAllFMPermissionsWithUser(pageable);
    }

    public void removeFMPermission(Long id){
        fmPermissionRepository.removeFMPermissionById(id);
    }

}
