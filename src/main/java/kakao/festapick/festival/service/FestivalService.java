package kakao.festapick.festival.service;


import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.dto.CustomFestivalRequestDto;
import kakao.festapick.festival.dto.FestivalRequestDto;
import kakao.festapick.festival.dto.FestivalResponseDto;
import kakao.festapick.festival.dto.FestivalStateDto;
import kakao.festapick.festival.repository.FestivalRepository;
import kakao.festapick.festival.tourapi.TourDetailResponse;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class FestivalService {

    private final FestivalRepository festivalRepository;
    private final UserRepository userRepository;

    public FestivalService(FestivalRepository festivalRepository, UserRepository userRepository) {
        this.festivalRepository = festivalRepository;
        this.userRepository = userRepository;
    }

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
    public FestivalResponseDto findOneById(Long festivalId) {
        Festival festival = festivalRepository.findFestivalById(festivalId)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 축제입니다."));
        return convertToResponseDto(festival);
    }

    //Id를 통해 승인된 축제 조회
    public FestivalResponseDto findApprovedOneById(Long festivalId) {
        Festival festival = festivalRepository.findFestivalByIdAndState(festivalId, FestivalState.APPROVED)
                .orElseThrow(() -> new IllegalStateException("존재하지 않는 축제입니다."));
        return convertToResponseDto(festival);
    }

    //지역코드와 날짜(오늘)를 통해 승인된 축제를 조회
    public List<FestivalResponseDto> findApprovedAreaAndDate(String areaCode) {
        List<Festival> festivalList = festivalRepository.findFestivalByAreaCodeAndDate(areaCode,
                getDate(), FestivalState.APPROVED);
        return convertToResponseDtoList(festivalList);
    }

    //지역코드를 통해 승인된 축제 조회
    public List<FestivalResponseDto> findApprovedOneByArea(String areaCode) {
        List<Festival> festivalList = festivalRepository.findFestivalByAreaCodeAndState(areaCode,
                FestivalState.APPROVED);
        return convertToResponseDtoList(festivalList);
    }

    //축제 검색 기능
    public List<FestivalResponseDto> findApprovedOneByKeyword(String keyword) {
        List<Festival> festivalList = festivalRepository.findFestivalByTitleContainingAndState(
                keyword, FestivalState.APPROVED);
        return convertToResponseDtoList(festivalList);
    }

    //모든 승인된 축제 검색 기능
    public List<FestivalResponseDto> findApproved() {
        List<Festival> festivalList = festivalRepository.findAllByState(FestivalState.APPROVED);
        return convertToResponseDtoList(festivalList);
    }

    //모든 축제 검색 기능(관리자)
    public List<FestivalResponseDto> findAll() {
        List<Festival> festivalList = festivalRepository.findAll();
        return convertToResponseDtoList(festivalList);
    }

    //UPDATE
    //축제 정보를 업데이트(관리자)
    @Transactional
    public FestivalResponseDto updateFestival(String identifier, Long id, FestivalRequestDto requestDto) {
        Festival festival = getMyFestival(identifier, id);
        festival.updateFestival(requestDto);
        return convertToResponseDto(festival);
    }

    //축제 상태 변경(admin이 사용자가 등록한 축제를 허용, 관리자)
    @Transactional
    public FestivalResponseDto updateState(Long id, FestivalStateDto state) {
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

    private FestivalResponseDto convertToResponseDto(Festival festival) {
        return new FestivalResponseDto(festival);
    }

    private List<FestivalResponseDto> convertToResponseDtoList(List<Festival> festivalList) {
        return new ArrayList<>(
                festivalList.stream()
                        .map(festival -> new FestivalResponseDto(festival))
                        .toList()
        );
    }

    private Festival getMyFestival(String identifier, Long id){
        Festival festival = festivalRepository.findFestivalByIdWithManager(id)
                .orElseThrow(() -> new IllegalStateException("해당 축제를 찾을 수 없습니다"));
        UserEntity manager = festival.getManager();
        if (manager != null && identifier.equals(manager.getIdentifier())){
            return festival;
        }
        throw new IllegalStateException("내가 등록한 축제가 아닙니다.");
    }

}
