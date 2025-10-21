package kakao.festapick.festivalnotice.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import kakao.festapick.domain.BaseTimeEntity;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festivalnotice.dto.FestivalNoticeRequestDto;
import kakao.festapick.user.domain.UserEntity;
import lombok.Getter;

@Getter
@Entity
public class FestivalNotice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String content;

    @ManyToOne
    @JoinColumn(name = "festival_id")
    private Festival festival;

    @ManyToOne
    @JoinColumn(name = "user_id")
    private UserEntity author;

    public void updateTitle(String title){
        this.title = title;
    }

    public void updateContent(String content){
        this.content = content;
    }

    public FestivalNotice(FestivalNoticeRequestDto requestDto, Festival festival, UserEntity user){
        this.content = requestDto.content();
        this.title = requestDto.title();
        this.festival = festival;
        this.author = user;
    }

    protected FestivalNotice(){}

}
