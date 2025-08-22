package kakao.festapick.festival.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.domain.QFestival;
import kakao.festapick.festival.dto.FestivalSearchCondForAdmin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static kakao.festapick.festival.domain.QFestival.*;

@Transactional
@Repository
public class QFestivalRepository {

    private final JPAQueryFactory queryFactory;

    public QFestivalRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Page<Festival> findByStateAndTitleLike(FestivalSearchCondForAdmin cond, Pageable pageable) {

        List<Festival> content = queryFactory
                .selectFrom(festival)
                .where(stateEq(cond.state()), titleLike(cond.title()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(festival.count())
                .from(festival);

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    BooleanExpression stateEq(FestivalState state) {
        return state != null ? festival.state.eq(state) : null;
    }

    BooleanExpression titleLike(String title) {
        return title != null ? festival.title.like("%" + title + "%") : null;
    }
}
