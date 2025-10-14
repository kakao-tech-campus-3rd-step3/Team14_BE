package kakao.festapick.festival.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FestivalLowServiceTest {

    private final TestUtil testUtil = new TestUtil();

    @Mock
    private FestivalRepository festivalRepository;

    @InjectMocks
    private FestivalLowService festivalLowService;

    @Test
    @DisplayName("존재하지 않는 축제를 검색한 경우")
    void findOneByIdFail() {

        //given
        Festival festival = createFestival();
        given(festivalRepository.findFestivalById(any())).willReturn(Optional.empty());

        //when - then
        NotFoundEntityException e = assertThrows(
                NotFoundEntityException.class, () -> festivalLowService.findFestivalById(festival.getId())
        );
        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.FESTIVAL_NOT_FOUND);

        verify(festivalRepository).findFestivalById(any());
        verifyNoMoreInteractions(festivalRepository);
    }

    private FestivalRequestDto createRequestDto() {
        return new FestivalRequestDto(
                "contentId","축제title", 32, "주소1", "상세주소",
                "imageUrl", testUtil.toLocalDate("20250824"), testUtil.toLocalDate("20250825"));
    }

    private Festival createFestival() {
        return new Festival(createRequestDto(), new TourDetailResponse());
    }


}