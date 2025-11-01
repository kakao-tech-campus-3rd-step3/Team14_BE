package kakao.festapick.festivalnotice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;
import kakao.festapick.festivalnotice.Repository.FestivalNoticeRepository;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FestivalNoticeLowServiceTest {

    @Mock
    private FestivalNoticeRepository festivalNoticeRepository;

    @InjectMocks
    private FestivalNoticeLowService festivalNoticeLowService;

    @Test
    @DisplayName("id와 작성자 id로 공지글을 찾지 못하는 경우 NotFound 예외가 발생")
    void findByIdAndAuthorId() {
        //given
        Long id = 1L;
        Long authorId = 2L;
        given(festivalNoticeRepository.findByIdAndAuthorId(any(), any())).willReturn(Optional.empty());

        //when
        NotFoundEntityException e = assertThrows(
                NotFoundEntityException.class,
                () -> festivalNoticeLowService.findByIdAndAuthorId(id, authorId)
        );

        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.FESTIVAL_NOTICE_NOT_FOUND);
        verify(festivalNoticeRepository).findByIdAndAuthorId(any(), any());
        verifyNoMoreInteractions(festivalNoticeRepository);
    }
}