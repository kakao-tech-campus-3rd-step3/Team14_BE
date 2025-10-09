package kakao.festapick.permission.fmpermission.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.permission.fmpermission.repository.FMPermissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FMPermissionLowServiceTest {

    @Mock
    private FMPermissionRepository fmPermissionRepository;

    @InjectMocks
    private FMPermissionLowService fmPermissionLowService;

    @Test
    @DisplayName("id로 해당하는 FMPermission을 찾지 못한 경우 예외 발생")
    void findFMPermissionById(){
        //given
        Long id = 1L;
        given(fmPermissionRepository.findFMPermissionById(id)).willReturn(Optional.empty());

        //when
        NotFoundEntityException e = assertThrows(
                NotFoundEntityException.class,
                () -> fmPermissionLowService.findFMPermissionById(id)
        );

        //then
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.FM_PERMISSION_NOT_FOUND);
        verify(fmPermissionRepository).findFMPermissionById(any());
        verifyNoMoreInteractions(fmPermissionRepository);
    }

    @Test
    @DisplayName("userId로 해당하는 FMPermission을 찾지 못한 경우 예외 발생")
    void findFMPermissionByUserId(){
        //given
        Long userId = 1L;
        given(fmPermissionRepository.findFMPermissionById(userId)).willReturn(Optional.empty());

        //when
        NotFoundEntityException e = assertThrows(
                NotFoundEntityException.class,
                () -> fmPermissionLowService.findFMPermissionById(userId)
        );

        //then
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.FM_PERMISSION_NOT_FOUND);
        verify(fmPermissionRepository).findFMPermissionById(any());
        verifyNoMoreInteractions(fmPermissionRepository);
    }
}