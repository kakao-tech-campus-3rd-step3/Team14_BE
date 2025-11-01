package kakao.festapick.festival.repository;

import static kakao.festapick.festival.domain.QFestival.festival;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.util.List;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.domain.FestivalState;
import kakao.festapick.festival.domain.FestivalType;
import kakao.festapick.festival.dto.FestivalSearchCondForAdmin;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@Repository
public class QFestivalRepository {

    private final JPAQueryFactory queryFactory;

    public QFestivalRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public Page<Festival> findByStateAndTitleLikeAndType(FestivalSearchCondForAdmin cond, Pageable pageable) {

        List<Festival> content = queryFactory
                .selectFrom(festival)
                .where(stateEq(cond.state()), titleLike(cond.title()), typeEq(cond.festivalType()))
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(festival.count())
                .from(festival)
                .where(stateEq(cond.state()), titleLike(cond.title()), typeEq(cond.festivalType()));

        return PageableExecutionUtils.getPage(content, pageable, countQuery::fetchCount);
    }

    private BooleanExpression stateEq(FestivalState state) {
        return state != null ? festival.state.eq(state) : null;
    }

    private BooleanExpression titleLike(String title) {
        return title != null ? festival.title.like("%" + title + "%") : null;
    }

    private BooleanExpression typeEq(FestivalType type) {
        return type != null ? festival.festivalType.eq(type) : null;
    }

    public Page<Festival> findFestivalByAreaCodeAndDate(Integer areaCode, LocalDate now, Pageable pageable){

        List<Festival> content = queryFactory
                .select(festival)
                .from(festival)
                .where(areaCodeEq(areaCode), festival.state.eq(FestivalState.APPROVED), festival.endDate.goe(now))
                .offset(pageable.getOffset()) // 페이지 시작 번호
                .limit(pageable.getPageSize()) // 페이지 사이즈
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(festival.count())
                .from(festival)
                .where(areaCodeEq(areaCode), festival.state.eq(FestivalState.APPROVED), festival.endDate.goe(now));

        return PageableExecutionUtils.getPage(content, pageable, () -> countQuery.fetchOne());
    }

    private BooleanExpression areaCodeEq(Integer areaCode){
        return areaCode == null ? null : festival.areaCode.eq(areaCode); // 조건을 반환
    }
}
