package kakao.festapick.util;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import kakao.festapick.ai.domain.RecommendationHistory;
import kakao.festapick.chat.domain.ChatRoom;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.dto.FestivalCustomRequestDto;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.review.domain.Review;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import org.springframework.stereotype.Component;

@Component
public class TestUtil {

    public UserEntity createTestUser() {
        UserEntity userEntity = new UserEntity("KAKAO-1234567890", "asd@test.com", "testUser", UserRoleType.USER, SocialType.KAKAO);
        userEntity.changeProfileImage("profileImageUrl");
        return userEntity;
    }

    public UserEntity createTestUserWithId() {
        UserEntity userEntity = new UserEntity(1L,"KAKAO-1234567890", "asd@test.com", "testUser", UserRoleType.USER, SocialType.KAKAO);
        userEntity.changeProfileImage("profileImageUrl");
        return userEntity;
    }

    public UserEntity createTestUser(String identifier){
        UserEntity userEntity = new UserEntity(identifier, "example@gmail.com", "exampleName", UserRoleType.USER, SocialType.GOOGLE);
        userEntity.changeProfileImage("profileImageUrl");
        return userEntity;
    }

    public UserEntity createTestManager(String identifier){
        UserEntity userEntity = new UserEntity(identifier, "example@gmail.com", "exampleName", UserRoleType.FESTIVAL_MANAGER, SocialType.GOOGLE);
        userEntity.changeProfileImage("profileImageUrl");
        return userEntity;
    }

    public Festival createTestFestival(UserEntity userEntity) {
        FestivalCustomRequestDto festivalCustomRequestDto = new FestivalCustomRequestDto("부산대축제", 1,"주소1", null, new FileUploadRequest(9999L, "postImageUrl"), createFestivalImages(),
                toLocalDate("20250810"), toLocalDate("20250820"),"https://hompage.com", "thisisoverviewthisisoverviewthisisoverviewhisisoverviewthisisoverviewthisisoverview");
        return new Festival(festivalCustomRequestDto, userEntity);
    }

    public Festival createTourApiTestFestival() throws Exception {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto(null, "부산대축제", 1,"주소1", null, "https://postImageUrl", toLocalDate("20250810"), toLocalDate("20250820"));
        return new Festival(festivalRequestDto, createTourDetailResponse());
    }

    public Festival createTourApiTestFestival2() throws Exception {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto("contentId2", "카테캠축제", 1,"주소1", null, "https://postImageUrl2", toLocalDate("20250810"), toLocalDate("20250820"));
        return new Festival(festivalRequestDto, createTourDetailResponse() );
        }

    public Festival createTestFestivalByAreaCode(int areaCode) throws Exception {
        FestivalRequestDto festivalRequestDto = new FestivalRequestDto(null, "부산대축제", areaCode,"주소1", null, "https://postImageUrl", toLocalDate("20250810"), toLocalDate("20250820"));
        return new Festival(festivalRequestDto, createTourDetailResponse());
    }

    public ChatRoom createTestChatRoom(Festival festival) {
        return new ChatRoom("test room", festival);
    }


    public LocalDate toLocalDate(String date){
        return LocalDate.parse(date, DateTimeFormatter.BASIC_ISO_DATE);
    }

    public TourDetailResponse createTourDetailResponse() throws Exception {
        TourDetailResponse tourDetailResponse = new TourDetailResponse();

        Field overview = tourDetailResponse.getClass().getDeclaredField("overview");
        overview.setAccessible(true);
        overview.set(tourDetailResponse, "this is overview for PNU CSE Festival");

        Field homepage = tourDetailResponse.getClass().getDeclaredField("homepage");
        homepage.setAccessible(true);
        homepage.set(tourDetailResponse, "https://www.festapick.com");
        return tourDetailResponse;
    }

    public List<FileUploadRequest> createFestivalImages(){
        List<FileUploadRequest> images = new ArrayList<>();
        images.add(new FileUploadRequest(99L, "https://festapick.firstimage.com"));
        images.add(new FileUploadRequest(999L,"https://festapick.secondimage.com"));
        images.add(new FileUploadRequest(9999L,"https://festapick.thridimage.com"));
        return images;
    }

    public RecommendationHistory createRecommendationHistory(UserEntity user, Festival festival) {
        return new RecommendationHistory(festival, user);
    }

    public Review createReview(UserEntity user, Festival festival) {
        return new Review(user, festival, "test content", 5);
    }
}
