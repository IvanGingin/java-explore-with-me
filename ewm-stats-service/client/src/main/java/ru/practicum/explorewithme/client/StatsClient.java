package ru.practicum.explorewithme.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.util.DefaultUriBuilderFactory;
import ru.practicum.explorewithme.dto.StatDto;

import java.util.List;

@Service
@Slf4j
public class StatsClient extends BaseClient {
    private static final String API_PREFIX = "/stats";

    @Autowired
    public StatsClient(@Value("${stats-service.url}") String serverUrl, RestTemplateBuilder builder) {
        super(
                builder
                        .uriTemplateHandler(new DefaultUriBuilderFactory(serverUrl + API_PREFIX))
                        .requestFactory(HttpComponentsClientHttpRequestFactory::new)
                        .build()
        );
    }

    public ResponseEntity<Object> postStat(StatDto statDto) {
        log.debug("Sending request to post stat: {}", statDto);
        ResponseEntity<Object> response = post("/hit", statDto);
        log.debug("Received response: {}", response);
        return response;
    }

    public ResponseEntity<Object> getStats(String start, String end, List<String> uris, boolean unique) {
        log.debug("Sending request to get stats from {} to {}, uris: {}, unique: {}", start, end, uris, unique);
        String url = String.format("?start=%s&end=%s&unique=%b", start, end, unique);
        if (uris != null && !uris.isEmpty()) {
            url += "&uris=" + String.join(",", uris);
        }
        ResponseEntity<Object> response = get(url);
        log.debug("Received response: {}", response);
        return response;
    }
}
