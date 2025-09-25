package kakao.festapick.ai.service;


import kakao.festapick.ai.dto.AiRecommendationRequest;
import kakao.festapick.festival.dto.FestivalListResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.util.List;

@Service
@RequiredArgsConstructor
public class AiRecommendationService {

    private final RestClient fastApiClient;


    public List<FestivalListResponse> getRecommendation(AiRecommendationRequest aiRecommendationRequest) {


        ResponseEntity<List<FestivalListResponse>> response = fastApiClient.post()
                .uri("/festivals/random")
                .contentType(MediaType.APPLICATION_JSON)
                .body(aiRecommendationRequest)
                .retrieve()
                .toEntity(new ParameterizedTypeReference<List<FestivalListResponse>>(){});

        return response.getBody();
    }
}
