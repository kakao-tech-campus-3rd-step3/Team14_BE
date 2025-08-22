package kakao.festapick.user.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import kakao.festapick.user.dto.UserSearchCond;
import kakao.festapick.user.domain.UserEntity;
import kakao.festapick.user.domain.UserRoleType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static kakao.festapick.user.domain.QUserEntity.*;

@Transactional
@Repository
public class QUserRepository {

    private final JPAQueryFactory queryFactory;

    public QUserRepository(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public Page<UserEntity> findByIdentifierOrUserEmail(UserSearchCond userSearchCond, Pageable pageable) {

        List<UserEntity> content = queryFactory
                .selectFrom(userEntity)
                .where(emailLike(userSearchCond.email()), identifierLike(userSearchCond.identifier()), roleEq(userSearchCond.role()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory.select(userEntity.count())
                .where(emailLike(userSearchCond.email()), identifierLike(userSearchCond.identifier()), roleEq(userSearchCond.role()))
                .from(userEntity);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchOne);
    }

    private BooleanExpression emailLike(String email) {
        return email != null ? userEntity.email.like(email+"%") : null;
    }

    private BooleanExpression identifierLike(String identifier) {
        return identifier != null ? userEntity.identifier.like(identifier+"%") : null;
    }

    private BooleanExpression roleEq(UserRoleType roleType) {
        return roleType != null ? userEntity.roleType.eq(roleType) : null;
    }
}
