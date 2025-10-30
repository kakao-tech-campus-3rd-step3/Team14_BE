package kakao.festapick.festival.domain;

import jakarta.persistence.*;
import kakao.festapick.domain.BaseTimeEntity;
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

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Table(indexes = @Index(name = "idx_festival_area_state_startdate_id", columnList= "areaCode, state, startDate, id"))
public class Festival extends BaseTimeEntity {

    private static String defaultImage;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FestivalType festivalType;

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

    @OneToMany(mappedBy = "festival")
    private List<Review> reviews = new ArrayList<>();

    @OneToMany(mappedBy = "festival")
    private List<Wish> wishes = new ArrayList<>();

    protected Festival() { }

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
        this.festivalType = FestivalType.FESTAPICK;
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
        this.homePage = detailResponse.getHomepage();
        this.state = FestivalState.APPROVED;
        this.festivalType = FestivalType.TOUR_API;
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

    public void updateManager(UserEntity user){
        this.manager = user;
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

}
