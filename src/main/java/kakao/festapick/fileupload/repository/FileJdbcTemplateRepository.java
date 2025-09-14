package kakao.festapick.fileupload.repository;

import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import kakao.festapick.fileupload.domain.FileEntity;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class FileJdbcTemplateRepository {

    private final JdbcTemplate jdbcTemplate;

    public void insertFestivalImages(List<FileEntity> fileEntities) {
        String upsertQuery =
                "insert into fileentity(url, fileType, domainType, domainId, createdDate)"
                        + "values (?, ?, ?, ?, ?)"
                        + "on duplicate key update "
                        + "createdDate =?";

        jdbcTemplate.batchUpdate(upsertQuery, new BatchPreparedStatementSetter() {
            @Override
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                FileEntity fileEntity = fileEntities.get(i);
                ps.setString(1, fileEntity.getUrl());
                ps.setString(2, fileEntity.getFileType().toString());
                ps.setString(3, fileEntity.getDomainType().toString());
                ps.setLong(4, fileEntity.getDomainId());
                ps.setObject(5, LocalDateTime.now());

                ps.setObject(6, LocalDateTime.now());
            }

            @Override
            public int getBatchSize() {
                return fileEntities.size();
            }
        });

    }

}
