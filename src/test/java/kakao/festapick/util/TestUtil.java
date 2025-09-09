package kakao.festapick.util;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
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
}
