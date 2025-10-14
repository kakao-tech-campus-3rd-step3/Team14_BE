package kakao.festapick.permission.festivalpermission.service;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.permission.festivalpermission.repository.FestivalPermissionRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FestivalPermissionLowServiceTest {

    @Mock
    private FestivalPermissionRepository festivalPermissionRepository;

    @InjectMocks
    private FestivalPermissionLowService festivalPermissionLowService ;

    @Test
    @DisplayName("userId와 id로 festival permission을 찾지 못한경우 예외 발생")
    void findByIdAndUserId(){
        //given
        Long festivalPermissionId = 1L;
        Long userId = 2L;
        given(festivalPermissionRepository.findByIdAndUserId(any(), any())).willReturn(Optional.empty());

        //when
        NotFoundEntityException e = assertThrows(
                NotFoundEntityException.class,
                () -> festivalPermissionLowService.findByIdAndUserId(festivalPermissionId, userId)
        );

        //then
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.FESTIVAL_PERMISSION_NOT_FOUND);
        verify(festivalPermissionRepository).findByIdAndUserId(any(), any());
        verifyNoMoreInteractions(festivalPermissionRepository);
    }

}