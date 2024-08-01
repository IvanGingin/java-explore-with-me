package ru.practicum.explorewithme.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.dto.StatDto;
import ru.practicum.explorewithme.dto.StatResponseDto;
import ru.practicum.explorewithme.mapper.StatMapper;
import ru.practicum.explorewithme.model.Stat;
import ru.practicum.explorewithme.repository.StatServiceRepository;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
@Slf4j
public class StatServiceImpl implements StatService {

    private final StatServiceRepository statRepository;

    public StatServiceImpl(StatServiceRepository statRepository) {
        this.statRepository = statRepository;
    }

    @Override
    public StatDto postStat(StatDto statDto) {
        log.debug("Received StatDto: {}", statDto);
        Stat stat = StatMapper.toStat(statDto);
        log.debug("Mapped Stat: {}", stat);
        Stat savedStat = statRepository.save(stat);
        log.debug("Saved Stat: {}", savedStat);
        return StatMapper.toStatDto(savedStat);
    }

    @Override
    @Transactional(readOnly = true)
    public List<StatResponseDto> getStat(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start.isAfter(end)) {
            throw new IllegalArgumentException("The start date cannot be later than the end date");
        }

        List<Object[]> results;
        if (uris == null || uris.isEmpty()) {
            if (unique) {
                results = statRepository.findUniqueStatsWithoutUris(start, end);
            } else {
                results = statRepository.findStatsWithoutUris(start, end);
            }
        } else {
            if (unique) {
                results = statRepository.findUniqueStats(start, end, uris);
            } else {
                results = statRepository.findStats(start, end, uris);
            }
        }

        return results.stream()
                .map(result -> new StatResponseDto((String) result[0], (String) result[1], (Long) result[2]))
                .sorted(Comparator.comparingLong(StatResponseDto::getHits).reversed())
                .collect(Collectors.toList());
    }
}

