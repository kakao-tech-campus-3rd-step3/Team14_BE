package kakao.festapick.festival.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import kakao.festapick.festival.domain.Festival;
import kakao.festapick.festival.dto.FestivalIdDto;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@Transactional
@RequiredArgsConstructor
public class FestivalJdbcTemplateRepository {

    private final JdbcTemplate jdbcTemplate;

    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    public void upsertFestivalInfo(List<Festival> festivalList) {
        String upsertQuery =
                "insert into festival(contentId, title, areaCode, addr1, addr2, posterInfo, startDate, endDate, overView, homePage, state)"
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"
                        + "on duplicate key update "
                        + "contentId = ?, title = ?, areaCode = ?, addr1 = ?, addr2 = ?, posterInfo = ?, startDate = ?, endDate = ?, overView = ?, homePage = ?, state = ?";

        jdbcTemplate.batchUpdate(upsertQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Festival festival = festivalList.get(i);

                ps.setString(1, festival.getContentId());
                ps.setString(2, festival.getTitle());
                ps.setInt(3, festival.getAreaCode());
                ps.setString(4, festival.getAddr1());
                ps.setString(5, festival.getAddr2());
                ps.setString(6, festival.getPosterInfo());
                ps.setObject(7, festival.getStartDate());
                ps.setObject(8, festival.getEndDate());
                ps.setString(9, festival.getOverView());
                ps.setString(10, festival.getHomePage());
                ps.setString(11, festival.getState().toString());

                ps.setString(12, festival.getContentId());
                ps.setString(13, festival.getTitle());
                ps.setInt(14, festival.getAreaCode());
                ps.setString(15, festival.getAddr1());
                ps.setString(16, festival.getAddr2());
                ps.setString(17, festival.getPosterInfo());
                ps.setObject(18, festival.getStartDate());
                ps.setObject(19, festival.getEndDate());
                ps.setString(20, festival.getOverView());
                ps.setString(21, festival.getHomePage());
                ps.setString(22, festival.getState().toString());
            }

            @Override
            public int getBatchSize() {
                return festivalList.size();
            }
        });
    }

    public void updatePosters(Map<String, String> posters) {
        String updateQuery = "update festival "
                + "set posterInfo = ?"
                + "where contentid =?";

        jdbcTemplate.batchUpdate(updateQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                String contentId = posters.keySet().stream().toList().get(i);
                String posterUrl = posters.values().stream().toList().get(i);

                ps.setString(1, posterUrl);
                ps.setString(2, contentId);
            }

            @Override
            public int getBatchSize() {
                return posters.size();
            }
        });

    }

    public List<FestivalIdDto> getFestivalIds(List<String> contentIds) {
        String selectQuery = "select f.contentid, f.id from festival f where contentid in (:contentIds)";
        SqlParameterSource params = new MapSqlParameterSource("contentIds", contentIds);
        return namedParameterJdbcTemplate.query(selectQuery,
                params,
                (rs, rowNum) -> new FestivalIdDto(rs.getString("contentId"), rs.getLong("id"))
        );
    }

}
