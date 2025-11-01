package kakao.festapick.festival.repository;

import kakao.festapick.festival.domain.Festival;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

@Repository
@RequiredArgsConstructor
public class FestivalJdbcTemplateRepository {

    private final JdbcTemplate jdbcTemplate;

    public void upsertFestivalInfo(List<Festival> festivalList) {
        String upsertQuery =
                "insert into Festival(contentId, title, areaCode, addr1, addr2, posterInfo, startDate, endDate, overView, homePage, state, festivalType) "
                        + "values (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?) "
                        + "on duplicate key update "
                        + "title = ?, areaCode = ?, addr1 = ?, addr2 = ?, posterInfo = ?, startDate = ?, endDate = ?, overView = ?, homePage = ?, state = ?, festivalType = ?";

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
                ps.setString(12, festival.getFestivalType().toString());

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
                ps.setString(23, festival.getFestivalType().toString());
            }

            @Override
            public int getBatchSize() {
                return festivalList.size();
            }
        });
    }

    public void updatePosters(Map<String, String> posters) {
        String updateQuery = "update Festival "
                + "set posterInfo = ? "
                + "where contentId =?";

        List<Map.Entry<String, String>> posterList = posters.entrySet().stream().toList();

        jdbcTemplate.batchUpdate(updateQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                Map.Entry<String, String> posterInfo = posterList.get(i);
                ps.setString(1, posterInfo.getValue());
                ps.setString(2, posterInfo.getKey());
            }

            @Override
            public int getBatchSize() {
                return posters.size();
            }
        });

    }

}
