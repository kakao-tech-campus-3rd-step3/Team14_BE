package kakao.festapick.permission.festivalpermission.service;

import java.util.List;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.permission.festivalpermission.domain.FestivalPermission;
import kakao.festapick.permission.festivalpermission.repository.FestivalPermissionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class FestivalPermissionLowService {

    private final FestivalPermissionRepository festivalPermissionRepository;

    public FestivalPermission save(FestivalPermission permission){
        return festivalPermissionRepository.save(permission);
    }

    public Page<FestivalPermission> findFestivalPermissionsByUserIdWithFestival(Long userId, Pageable pageable){
        return festivalPermissionRepository.findFestivalPermissionsByUserIdWithFestival(userId, pageable);
    }

    public List<FestivalPermission> findAllFestivalPermission(){
        return festivalPermissionRepository.findAllFestivalPermission();
    }

    public FestivalPermission findByIdAndUserId(Long id, Long userId){
        return festivalPermissionRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_PERMISSION_NOT_FOUND));
    }

    public FestivalPermission findByIdAndUserIdWithFestival(Long id, Long userId){
        return festivalPermissionRepository.findByIdAndUserIdWithFestival(id, userId)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_PERMISSION_NOT_FOUND));
    }

    public FestivalPermission findByIdWithFestival(Long id){
        return festivalPermissionRepository.findByIdWithFestival(id)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.FESTIVAL_PERMISSION_NOT_FOUND));
    }

    public boolean existsByUserIdAndFestivalId(Long userId, Long festivalId){
        return festivalPermissionRepository.existsByUserIdAndFestivalId(userId, festivalId);
    }

    public void removeById(Long id){
        festivalPermissionRepository.deleteById(id);
    }

    public List<FestivalPermission> findByUserId(Long userId){
        return festivalPermissionRepository.findByUserId(userId);
    }

    public void deleteByUserId(Long userId){
        festivalPermissionRepository.deleteByUserId(userId);
    }

    public List<FestivalPermission> findByFestivalId(Long festivalId){
        return festivalPermissionRepository.findByFestivalId(festivalId);
    }

    public void deleteByFestivalId(Long festivalId){
        festivalPermissionRepository.deleteByUserId(festivalId);
    }

}
