package kakao.festapick.festival.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kakao.festapick.festival.dto.CustomFestivalRequestDto;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.user.domain.UserEntity;
import lombok.Getter;

@Entity
@Getter
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long Id;

    private String contentId;

    private String title;

    private String areaCode;

    private String addr1;

    private String addr2;

    private String imageUrl;

    private String startDate;

    private String endDate;

    @Column(length = 5000)
    private String overView;

    @Column(length = 5000)
    private String homePage;

    @Enumerated(EnumType.STRING)
    private FestivalState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private UserEntity manager;

    protected Festival() { }

    //TODO: contentId 규칙 만들기
    public Festival(CustomFestivalRequestDto customFestivalRequestDto, UserEntity user) {
        this.contentId = "tempcontentId";
        this.title = customFestivalRequestDto.title();
        this.areaCode = customFestivalRequestDto.areaCode();
        this.addr1 = customFestivalRequestDto.addr1();
        this.addr2 = customFestivalRequestDto.addr2();
        this.imageUrl = customFestivalRequestDto.imageUrl();
        this.startDate = customFestivalRequestDto.startDate();
        this.endDate = customFestivalRequestDto.endDate();
        this.overView = customFestivalRequestDto.overView();
        this.homePage = customFestivalRequestDto.homePage();
        this.state = FestivalState.PROCESSING;
        this.manager = user;
    }

    public Festival(FestivalRequestDto festivalRequestDto, String overView, String homePage) {
        this.contentId = festivalRequestDto.contentId();
        this.title = festivalRequestDto.title();
        this.areaCode = festivalRequestDto.areaCode();
        this.addr1 = festivalRequestDto.addr1();
        this.addr2 = festivalRequestDto.addr2();
        this.imageUrl = festivalRequestDto.imageUrl();
        this.startDate = festivalRequestDto.startDate();
        this.endDate = festivalRequestDto.endDate();
        this.overView = overView;
        this.homePage = homePage;
        this.state = FestivalState.APPROVED;
    }

    //자신이 등록한 축제에 대해서만
    public void updateFestival(FestivalRequestDto requestDto){
        this.title = requestDto.title();
        this.areaCode = requestDto.areaCode();
        this.addr1 = requestDto.addr1();
        this.addr2 = requestDto.addr2();
        this.startDate = requestDto.startDate();
        this.endDate = requestDto.endDate();
        this.overView = requestDto.overView();
        this.homePage = requestDto.homePage();
    }

    //admin만
    public void updateState(FestivalState festivalState){
        this.state = festivalState;
    }

}
