package kakao.festapick.util;

import java.lang.reflect.Field;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.fileupload.dto.FileUploadRequest;
import kakao.festapick.user.domain.SocialType;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import org.springframework.stereotype.Component;

@Component
public class TestUtil {

    public UserEntity createTestUser() {
        return new UserEntity(1L, "KAKAO-1234567890", "asd@test.com", "testUser", UserRoleType.USER, SocialType.KAKAO);
    }

    public UserEntity createTestUser(String identifier){
        return new UserEntity(identifier, "example@gmail.com", "exampleName", UserRoleType.USER, SocialType.GOOGLE);
    }

    public UserEntity createTestManager(String identifier){
        return new UserEntity(identifier, "example@gmail.com", "exampleName", UserRoleType.FESTIVAL_MANAGER, SocialType.GOOGLE);
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
}
