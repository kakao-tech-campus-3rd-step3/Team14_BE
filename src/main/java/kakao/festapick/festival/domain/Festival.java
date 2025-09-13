package kakao.festapick.festival.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import kakao.festapick.festival.dto.FestivalCustomRequestDto;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.dto.FestivalUpdateRequestDto;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.global.exception.BadRequestException;
import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.review.domain.Review;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.wish.domain.Wish;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Entity
@Getter
@Slf4j
@Table(indexes = @Index(name = "idx_festival_area_state_startdate_id", columnList= "areaCode, state, startDate, id"))
public class Festival {

    private static String defaultImage;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true)
    private String contentId;

    @Column(nullable = false)
    private String title;

    private int areaCode;

    @Column(nullable = false)
    private String addr1;

    private String addr2;

    @Column(nullable = false)
    private String posterInfo;

    @Column(nullable = false)
    private LocalDate startDate;

    @Column(nullable = false)
    private LocalDate endDate;

    @Column(length = 5000, nullable = false)
    private String overView;

    @Column(length = 500)
    private String homePage;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FestivalState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private UserEntity manager;

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Wish>  wishes = new ArrayList<>();

    @OneToMany(mappedBy = "festival", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Review> reviews = new ArrayList<>();

    protected Festival() { }

    //TODO: contentId 규칙 만들기
    public Festival(FestivalCustomRequestDto festivalCustomRequestDto, UserEntity user) {
        checkStartAndEndDate(festivalCustomRequestDto.startDate(), festivalCustomRequestDto.endDate());
        this.contentId = null;
        this.title = festivalCustomRequestDto.title();
        this.areaCode = festivalCustomRequestDto.areaCode();
        this.addr1 = festivalCustomRequestDto.addr1();
        this.addr2 = festivalCustomRequestDto.addr2();
        this.posterInfo = festivalCustomRequestDto.posterInfo().presignedUrl(); //포스터 등록 필수
        this.startDate = festivalCustomRequestDto.startDate();
        this.endDate = festivalCustomRequestDto.endDate();
        this.overView = festivalCustomRequestDto.overView();
        this.homePage = festivalCustomRequestDto.homePage();
        this.state = FestivalState.PROCESSING;
        this.manager = user;
    }

    //tourAPI 호출
    public Festival(FestivalRequestDto festivalRequestDto, TourDetailResponse detailResponse) {
        checkStartAndEndDate(festivalRequestDto.startDate(), festivalRequestDto.endDate());
        this.contentId = festivalRequestDto.contentId();
        this.title = festivalRequestDto.title();
        this.areaCode = festivalRequestDto.areaCode();
        this.addr1 = festivalRequestDto.addr1();
        this.addr2 = festivalRequestDto.addr2();
        this.posterInfo = resolveImage(festivalRequestDto.posterInfo());
        this.startDate = festivalRequestDto.startDate();
        this.endDate = festivalRequestDto.endDate();
        this.overView = detailResponse.getOverview();
        this.homePage = parseHomepageInfo(detailResponse.getHomepage());
        this.state = FestivalState.APPROVED;
    }

    //축제 정보 수정
    public void updateFestival(FestivalUpdateRequestDto requestDto){
        checkStartAndEndDate(requestDto.startDate(), requestDto.endDate());
        this.title = requestDto.title();
        this.areaCode = requestDto.areaCode();
        this.addr1 = requestDto.addr1();
        this.addr2 = requestDto.addr2();
        this.startDate = requestDto.startDate();
        this.endDate = requestDto.endDate();
        this.posterInfo = requestDto.posterInfo().presignedUrl();
        this.overView = requestDto.overView();
        this.homePage = requestDto.homePage();
    }

    //admin만 축제 권한 변경
    public void updateState(FestivalState festivalState){
        this.state = festivalState;
    }

    public static void setDefaultImage(String url) {
        defaultImage = url;
    }

    private static String resolveImage(String url) {
        return (url == null || url.isBlank()) ? defaultImage : url;
    }

    private void checkStartAndEndDate(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) throw new BadRequestException(ExceptionCode.FESTIVAL_BAD_DATE);
    }

    private String parseHomepageInfo(String homePage){
        try{
            List<String> parsedResult = Arrays.asList(homePage.split("\""));
            return parsedResult.stream()
                    .filter(url -> url.startsWith("http") || url.startsWith("www."))
                    .findFirst()
                    .orElseThrow(() -> new IllegalArgumentException("홈페이지의 주소를 찾을 수 없습니다."));
        } catch (NullPointerException | IllegalArgumentException e) {
            log.error("홈페이지 정보를 찾을 수 없습니다.");
            log.error("homePage = {}", homePage);
        }
        return "no_homepage";
    }

}
