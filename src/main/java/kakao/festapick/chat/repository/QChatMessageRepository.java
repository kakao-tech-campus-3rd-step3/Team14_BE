package kakao.festapick.chat.repository;

import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.List;
import kakao.festapick.chat.domain.ChatMessage;
import kakao.festapick.chat.dto.ChatMessageSliceDto;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import static kakao.festapick.chat.domain.QChatMessage.chatMessage;
import static kakao.festapick.user.domain.QUserEntity.userEntity;

@Transactional
@Repository
public class QChatMessageRepository {

    private final JPAQueryFactory queryFactory;

    public QChatMessageRepository(EntityManager em) {
        this.queryFactory = new JPAQueryFactory(em);
    }

    public ChatMessageSliceDto findByChatRoomIdWithUser(Long chatRoomId, Long cursorId,
            LocalDateTime cursorTime, int size) {
        List<ChatMessage> content = queryFactory
                .selectFrom(chatMessage)
                .join(chatMessage.user, userEntity).fetchJoin()
                .where(
                        chatMessage.chatRoom.id.eq(chatRoomId)
                                .and(
                                        cursorCond(cursorId, cursorTime)
                                )
                )
                .orderBy(chatMessage.createdDate.desc(), chatMessage.id.desc())
                .limit(size + 1)
                .fetch();

        boolean hasNext = false;

        if (content.size() > size) {
            content.remove(size);
            hasNext = true;
        }

        return new ChatMessageSliceDto(content, hasNext);
    }

    private BooleanExpression cursorCond(Long cursorId, LocalDateTime cursorTime) {
        if (cursorId == null || cursorTime == null) {
            return null;
        }

        return chatMessage.createdDate.lt(cursorTime)
                .or(
                        chatMessage.createdDate.eq(cursorTime)
                                .and(chatMessage.id.lt(cursorId))
                );
    }

}
