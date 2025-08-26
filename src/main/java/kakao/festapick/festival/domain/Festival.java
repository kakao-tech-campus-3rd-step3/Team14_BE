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
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import kakao.festapick.festival.dto.FestivalCustomRequestDto;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.dto.FestivalUpdateRequestDto;
import kakao.festapick.user.domain.UserEntity;
import lombok.Getter;

@Entity
@Getter
public class Festival {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String contentId;

    @NotBlank
    private String title;

    private int areaCode;

    @NotBlank
    private String addr1;

    private String addr2;

    private String imageUrl;

    @NotNull
    private LocalDate startDate;

    @NotNull
    private LocalDate endDate;

    @Column(length = 5000)
    private String overView;

    @Column(length = 500)
    private String homePage;

    @NotNull
    @Enumerated(EnumType.STRING)
    private FestivalState state;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "manager_id")
    private UserEntity manager;

    protected Festival() { }

    //TODO: contentId 규칙 만들기
    public Festival(FestivalCustomRequestDto festivalCustomRequestDto, UserEntity user) {
        this.contentId = "tempcontentId";
        this.title = festivalCustomRequestDto.title();
        this.areaCode = festivalCustomRequestDto.areaCode();
        this.addr1 = festivalCustomRequestDto.addr1();
        this.addr2 = festivalCustomRequestDto.addr2();
        this.imageUrl = festivalCustomRequestDto.imageUrl();
        this.startDate = festivalCustomRequestDto.startDate();
        this.endDate = festivalCustomRequestDto.endDate();
        this.overView = festivalCustomRequestDto.overView();
        this.homePage = festivalCustomRequestDto.homePage();
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

    //축제 정보 수정
    public void updateFestival(FestivalUpdateRequestDto requestDto){
        this.title = requestDto.title();
        this.areaCode = requestDto.areaCode();
        this.addr1 = requestDto.addr1();
        this.addr2 = requestDto.addr2();
        this.startDate = requestDto.startDate();
        this.endDate = requestDto.endDate();
        this.overView = requestDto.overView();
        this.homePage = requestDto.homePage();
    }

    //admin만 축제 권한 변경
    public void updateState(FestivalState festivalState){
        this.state = festivalState;
    }

}
