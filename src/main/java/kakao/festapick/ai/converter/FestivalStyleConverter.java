package kakao.festapick.ai.converter;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;
import kakao.festapick.ai.dto.FestivalStyle;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Converter
public class FestivalStyleConverter implements AttributeConverter<List<FestivalStyle>, String> {
    @Override
    public String convertToDatabaseColumn(List<FestivalStyle> festivalStyles) {
        return festivalStyles.stream()
                .map(Enum::name)
                .collect(Collectors.joining(","));
    }

    @Override
    public List<FestivalStyle> convertToEntityAttribute(String festivalStyles) {

        return Arrays.stream(festivalStyles.split(","))
                .map(FestivalStyle::valueOf)
                .toList();
    }
}
