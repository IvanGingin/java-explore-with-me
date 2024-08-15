package ru.practicum.explorewithme.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.dto.StatDto;
import ru.practicum.explorewithme.model.Stat;

@UtilityClass
public class StatMapper {

    public Stat toStat(StatDto statDto) {
        if (statDto == null) {
            return null;
        }

        return Stat.builder()
                .ip(statDto.getIp())
                .uri(statDto.getUri())
                .timestamp(statDto.getTimestamp())
                .app(statDto.getApp())
                .build();
    }

    public StatDto toStatDto(Stat stat) {
        if (stat == null) {
            return null;
        }

        return StatDto.builder()
                .timestamp(stat.getTimestamp())
                .app(stat.getApp())
                .uri(stat.getUri())
                .ip(stat.getIp())
                .build();
    }
}
