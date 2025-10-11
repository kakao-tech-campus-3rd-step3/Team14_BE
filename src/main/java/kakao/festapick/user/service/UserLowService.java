package kakao.festapick.user.service;

import kakao.festapick.global.exception.ExceptionCode;
import kakao.festapick.global.exception.NotFoundEntityException;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.dto.UserSearchCond;
import kakao.festapick.user.repository.QUserRepository;
import kakao.festapick.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Service
@RequiredArgsConstructor
public class UserLowService {

    private final UserRepository userRepository;

    private final QUserRepository qUserRepository;

    public UserEntity findByIdentifier(String identifier) {
        return userRepository.findByIdentifier(identifier)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.USER_NOT_FOUND));
    }

    public UserEntity findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new NotFoundEntityException(ExceptionCode.USER_NOT_FOUND));
    }

    public void deleteById(Long id) {
        userRepository.deleteById(id);
    }

    public Page<UserEntity> findByIdentifierOrUserEmail(UserSearchCond userSearchCond, Pageable pageable) {
        return qUserRepository.findByIdentifierOrUserEmail(userSearchCond, pageable);
    }

    public UserEntity getReferenceById(Long id) {
        return userRepository.getReferenceById(id);
    }

}
