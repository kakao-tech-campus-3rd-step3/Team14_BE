package kakao.festapick.festival.service;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.dto.*;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.festival.repository.QFestivalRepository;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;
    private final QFestivalRepository  qFestivalRepository;


    //CREATE
    //TODO: create - customized Festival (How to upload an image)
    @Transactional
    public Long addCustomizedFestival(CustomFestivalRequestDto requestDto, String identifier) {
        UserEntity user =  userRepository.findByIdentifier(identifier)
                .orElseThrow(()->new NotFoundEntityException("존재하지 않는 회원입니다."));
        Festival festival = new Festival(requestDto, user);
        Festival savedFestival = festivalRepository.save(festival);
        return savedFestival.getId();
    }

    //create - TourAPI
    @Transactional
    public Long addFestival(FestivalRequestDto requestDto, TourDetailResponse detailResponse) {
        Festival festival = new Festival(
                requestDto,
                detailResponse.getOverview(),
                getHomePage(detailResponse.getHomepage())
        );
        Festival savedFestival = festivalRepository.save(festival);
        return savedFestival.getId();
    }

    //READ
    //contentId를 통한 축제 조회(to get Overview)
    public boolean checkExistenceByContentId(String contentId) {
        Optional<Festival> festival = festivalRepository.findFestivalByContentIdAndState(contentId,
                FestivalState.APPROVED);
        return festival.isEmpty();
    }

    //Id를 통한 축제 조회
    public FestivalDetailResponse findOneById(Long festivalId) {
        Festival festival = festivalRepository.findFestivalById(festivalId)
                .orElseThrow(() -> new NotFoundEntityException("존재하지 않는 축제입니다."));
        return convertToResponseDto(festival);
    }

    //Id를 통해 승인된 축제 조회
    public FestivalDetailResponse findApprovedOneById(Long festivalId) {
        Festival festival = festivalRepository.findFestivalByIdAndState(festivalId, FestivalState.APPROVED)
                .orElseThrow(() -> new NotFoundEntityException("존재하지 않는 축제입니다."));
        return convertToResponseDto(festival);
    }

    //지역코드와 날짜(오늘)를 통해 승인된 축제를 조회
    public List<FestivalDetailResponse> findApprovedAreaAndDate(String areaCode) {
        List<Festival> festivalList = festivalRepository.findFestivalByAreaCodeAndDate(areaCode,
                getDate(), FestivalState.APPROVED);
        return convertToResponseDtoList(festivalList);
    }

    //지역코드를 통해 승인된 축제 조회
    public List<FestivalDetailResponse> findApprovedOneByArea(String areaCode) {
        List<Festival> festivalList = festivalRepository.findFestivalByAreaCodeAndState(areaCode,
                FestivalState.APPROVED);
        return convertToResponseDtoList(festivalList);
    }

    //축제 검색 기능
    public List<FestivalDetailResponse> findApprovedOneByKeyword(String keyword) {
        List<Festival> festivalList = festivalRepository.findFestivalByTitleContainingAndState(
                keyword, FestivalState.APPROVED);
        return convertToResponseDtoList(festivalList);
    }

    //모든 승인된 축제 검색 기능
    public List<FestivalDetailResponse> findApproved() {
        List<Festival> festivalList = festivalRepository.findAllByState(FestivalState.APPROVED);
        return convertToResponseDtoList(festivalList);
    }

    //모든 축제 검색 기능(관리자)
    public Page<FestivalListResponseForAdmin> findAllWithPage(FestivalSearchCondForAdmin cond, Pageable pageable) {
        return qFestivalRepository.findByStateAndTitleLike(cond, pageable)
                .map(FestivalListResponseForAdmin::new);
    }

    //UPDATE
    //축제 정보를 업데이트(관리자)
    @Transactional
    public FestivalDetailResponse updateFestival(String identifier, Long id, FestivalRequestDto requestDto) {
        Festival festival = getMyFestival(identifier, id);
        festival.updateFestival(requestDto);
        return convertToResponseDto(festival);
    }

    //축제 상태 변경(admin이 사용자가 등록한 축제를 허용, 관리자)
    @Transactional
    public FestivalDetailResponse updateState(Long id, FestivalStateDto state) {
        Festival festival = festivalRepository.findFestivalById(id).orElseThrow(
                () -> new IllegalStateException("해당 축제를 찾을 수 없습니다")
        );
        festival.updateState(FestivalState.valueOf(state.state()));
        return convertToResponseDto(festival);
    }

    //DELETE
    @Transactional
    public void removeOne(String identifier, Long id) {
        getMyFestival(identifier, id);
        festivalRepository.removeFestivalById(id);
    }

    @Transactional
    public void deleteFestivalForAdmin(Long festivalId) {
        festivalRepository.deleteById(festivalId);
    }

    //현재 날짜 구하기
    private String getDate() {
        LocalDate now = LocalDate.now();
        DateTimeFormatter date = DateTimeFormatter.ofPattern("yyyyMMdd");
        return now.format(date);
    }

    private String getHomePage(String homePage){
        if(!homePage.isBlank()){
            String [] parsedResult = homePage.split("<|>");
            String pageURL = parsedResult[2];
            if(pageURL.startsWith("https://")){
                return pageURL;
            }
        }
        return "festivalHomePage";
    }

    private FestivalDetailResponse convertToResponseDto(Festival festival) {
        return new FestivalDetailResponse(festival);
    }

    private List<FestivalDetailResponse> convertToResponseDtoList(List<Festival> festivalList) {
        return new ArrayList<>(
                festivalList.stream()
                        .map(festival -> new FestivalDetailResponse(festival))
                        .toList()
        );
    }

    private Festival getMyFestival(String identifier, Long id){
        Festival festival = festivalRepository.findFestivalByIdWithManager(id)
                .orElseThrow(() -> new NotFoundEntityException("해당 축제를 찾을 수 없습니다"));
        UserEntity manager = festival.getManager();
        if (manager != null && identifier.equals(manager.getIdentifier())){
            return festival;
        }
        throw new IllegalStateException("내가 등록한 축제가 아닙니다.");
    }
}
