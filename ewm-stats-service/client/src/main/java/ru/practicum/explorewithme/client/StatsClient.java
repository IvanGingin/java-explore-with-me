package ru.practicum.explorewithme.client;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.explorewithme.dto.StatDto;
import ru.practicum.explorewithme.dto.StatResponseDto;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class StatsClient {

    @Value("${stats-service.url}")
    private String url;

    private final RestTemplate restTemplate;
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public void saveStats(StatDto statDto) {
        try {
            restTemplate.postForEntity(url + "/hit", statDto, Void.class);
        } catch (Exception e) {
            log.error("Не удалось отправить статистику: {}", e.getMessage());
        }
    }

    public List<StatResponseDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, boolean unique) {
        if (start == null || end == null) {
            throw new IllegalArgumentException("Необходимо указать начальную и конечную дату");
        }

        String urisString = uris != null ? String.join(",", uris) : "";
        Map<String, Object> parameters = Map.of(
                "start", encodeDateTime(start),
                "end", encodeDateTime(end),
                "uris", urisString,
                "unique", unique
        );

        try {
            StatResponseDto[] response = restTemplate.getForObject(
                    url + "/stats?start={start}&end={end}&uris={uris}&unique={unique}",
                    StatResponseDto[].class,
                    parameters
            );
            return Objects.isNull(response) ? List.of() : List.of(response);
        } catch (Exception e) {
            log.error("Не удалось получить статистику: {}", e.getMessage());
            return List.of();
        }
    }

    private String encodeDateTime(LocalDateTime time) {
        String timeString = time.format(formatter);
        return URLEncoder.encode(timeString, StandardCharsets.UTF_8);
    }
}
