package kakao.festapick.ai.domain;

import jakarta.persistence.*;
import kakao.festapick.ai.converter.FestivalStyleConverter;
import kakao.festapick.ai.dto.AiRecommendationRequest;
import kakao.festapick.ai.dto.FestivalStyle;
import kakao.festapick.domain.BaseTimeEntity;
import kakao.festapick.user.domain.UserEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Getter
public class RecommendationForm extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private int areaCode;

    @Convert(converter = FestivalStyleConverter.class)
    @Column(nullable = false)
    private List<FestivalStyle> styles;

    @Column(nullable = false)
    private boolean isNewPlace;

    @Column(nullable = false)
    private boolean isSolo;

    @Column(nullable = false)
    private boolean prefersEnjoyment;

    @Column(nullable = false)
    private boolean isSpontaneous;

    String additionalInfo;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private UserEntity user;


    public RecommendationForm(AiRecommendationRequest aiRecommendationRequest, UserEntity user) {
        this.areaCode = aiRecommendationRequest.areaCode();
        this.styles = aiRecommendationRequest.styles();
        this.isNewPlace = aiRecommendationRequest.isNewPlace();
        this.isSolo = aiRecommendationRequest.isSolo();
        this.prefersEnjoyment = aiRecommendationRequest.prefersEnjoyment();
        this.isSpontaneous = aiRecommendationRequest.isSpontaneous();
        this.additionalInfo = aiRecommendationRequest.additionalInfo();
        this.user = user;
    }
}
