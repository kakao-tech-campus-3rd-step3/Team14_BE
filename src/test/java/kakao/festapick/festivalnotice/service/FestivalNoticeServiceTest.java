package kakao.festapick.festivalnotice.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import java.util.ArrayList;
import java.util.List;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.service.FestivalLowService;
import kakao.festapick.festivalnotice.domain.FestivalNotice;
import kakao.festapick.festivalnotice.dto.FestivalNoticeRequestDto;
import kakao.festapick.festivalnotice.dto.FestivalNoticeResponseDto;
import kakao.festapick.fileupload.domain.DomainType;
import kakao.festapick.fileupload.domain.FileEntity;
import kakao.festapick.fileupload.domain.FileType;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.fileupload.service.FileService;
import kakao.festapick.fileupload.service.FileUploadHelper;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.ForbiddenException;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.service.UserLowService;
import kakao.festapick.util.TestUtil;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class FestivalNoticeServiceTest {

    @Mock
    private UserLowService userLowService;

    @Mock
    private FestivalLowService festivalLowService;

    @Mock
    private FestivalNoticeLowService festivalNoticeLowService;


    @Mock
    private FileService fileService;

    @Mock
    private FileUploadHelper fileUploadHelper;

    @InjectMocks
    private FestivalNoticeService festivalNoticeService;

    private final TestUtil testUtil = new TestUtil();

    @Test
    @DisplayName("내가 관리하고 있는 축제에 대해서 공지를 작성할 수 있음")
    void addFestivalNotice() {

        //given
        UserEntity user = testUtil.createTestUserWithId();
        Festival festival = testUtil.createTourApiTestFestival();
        festival.updateManager(user);

        FestivalNoticeRequestDto requestDto = new FestivalNoticeRequestDto("제목", "공지 사항", uploadFiles());

        FestivalNotice festivalNotice = new FestivalNotice(requestDto, festival, user);

        given(festivalLowService.findFestivalById(any())).willReturn(festival);
        given(userLowService.getReferenceById(any())).willReturn(user);
        given(festivalNoticeLowService.save(any())).willReturn(festivalNotice);

        //when
        festivalNoticeService.addFestivalNotice(festival.getId(), user.getId(), requestDto);

        //then
        verify(festivalLowService).findFestivalById(any());
        verify(userLowService).getReferenceById(any());
        verify(festivalNoticeLowService).save(any());
        verify(fileUploadHelper).saveFiles(any(), any(), any(), any()); // 이미지가 존재하는 경우 이미지 업로드가 동작한다.
        verifyNoMoreInteractions(festivalLowService, userLowService, festivalNoticeLowService, fileUploadHelper);
    }

    @Test
    @DisplayName("내가 관리하고 있는 축제가 아닌 경우, 공지를 올릴 수 없음")
    void addFestivalNoticeFail() {

        //given
        Long userId = 99L;

        FestivalNoticeRequestDto requestDto = new FestivalNoticeRequestDto("제목", "공지 사항", uploadFiles());

        Festival festival = testUtil.createTourApiTestFestival();
        UserEntity user = testUtil.createTestUserWithId();
        festival.updateManager(user);

        given(festivalLowService.findFestivalById(any())).willReturn(festival);

        //when
        ForbiddenException e = assertThrows(
                ForbiddenException.class,
                () -> festivalNoticeService.addFestivalNotice(festival.getId(), userId, requestDto)
        );

        assertThat(e.getExceptionCode()).isEqualTo(ExceptionCode.FESTIVAL_NOTICE_ACCESS_FORBIDDEN);
        verify(festivalLowService).findFestivalById(any());
        verifyNoMoreInteractions(festivalLowService);
    }

    @Test
    void updateFestivalNotice() {

        //given
        FestivalNoticeRequestDto festivalNoticeRequestDto = createRequestDto("공지 사항1", "내용1");
        Festival festival = testUtil.createTourApiTestFestival();
        UserEntity user = testUtil.createTestManager("KAKAO-12345");
        FestivalNotice festivalNotice = new FestivalNotice(festivalNoticeRequestDto, festival, user);

        List<FileEntity> images = new ArrayList<>();
        images.add(new FileEntity("https://images", FileType.IMAGE, DomainType.FESTIVAL_NOTICE, festivalNotice.getId()));

        FestivalNoticeRequestDto updateRequest = createRequestDto("공지 사항2", "내용1");

        given(festivalNoticeLowService.findByIdAndAuthorId(any(), any())).willReturn(festivalNotice);
        given(fileService.findByDomainIdAndDomainType(any(), any())).willReturn(images);

        //when
        FestivalNoticeResponseDto responseDto = festivalNoticeService.updateFestivalNotice(festivalNotice.getId(), user.getId(), updateRequest);

        //then
        assertThat(responseDto.title()).isEqualTo("공지 사항2");
        verify(festivalNoticeLowService).findByIdAndAuthorId(any(), any());
        verify(fileUploadHelper).updateFiles(any(), any(), any(), any());
        verifyNoMoreInteractions(festivalNoticeLowService, fileUploadHelper);
    }

    private FestivalNoticeRequestDto createRequestDto(String title, String content){
        return new FestivalNoticeRequestDto(title, content, uploadFiles());
    }

    private List<FileUploadRequest> uploadFiles(){
        List<FileUploadRequest> fileUploadRequests = new ArrayList<>();
        fileUploadRequests.add(new FileUploadRequest(1L, "https://images1"));
        fileUploadRequests.add(new FileUploadRequest(1L, "https://images1"));
        return fileUploadRequests;
    }


}