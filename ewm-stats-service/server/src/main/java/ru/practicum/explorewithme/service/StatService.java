package ru.practicum.explorewithme.service;


import ru.practicum.explorewithme.dto.StatDto;
import ru.practicum.explorewithme.dto.StatResponseDto;

import java.time.LocalDateTime;
import java.util.List;

public interface StatService {

    StatDto postStat(StatDto statDto);

    List<StatResponseDto> getStat(LocalDateTime rangeStart, LocalDateTime end, List<String> uris, boolean unique);
}