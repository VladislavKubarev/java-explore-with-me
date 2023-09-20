package ru.practicum;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

public class StatsClient {
    private final RestTemplate restTemplate;
    private final String serverUrl;

    public StatsClient(RestTemplate restTemplate, @Value("${stat-server.url}") String serverUrl) {
        this.restTemplate = restTemplate;
        this.serverUrl = serverUrl;
    }

    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        HttpEntity<EndpointHitDto> httpEntity = new HttpEntity<>(endpointHitDto, defaultHeaders());
        ResponseEntity<EndpointHitDto> responseEntity = restTemplate.exchange(
                serverUrl + "/hit", HttpMethod.POST, httpEntity, EndpointHitDto.class);

        return responseEntity.getBody();
    }

    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        Map<String, Object> parameters = Map.of(
                "start", start,
                "end", end,
                "uris", String.join(",", uris),
                "unique", unique
        );

        ResponseEntity<List<ViewStatsDto>> responseEntity = restTemplate.exchange(
                serverUrl + "/stats?start={start}&end={end}&unique={unique}&uris={uris}", HttpMethod.GET,
                new HttpEntity<>(defaultHeaders()), new ParameterizedTypeReference<List<ViewStatsDto>>() {
                }, parameters
        );

        return responseEntity.getBody();

    }

    private HttpHeaders defaultHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(List.of(MediaType.APPLICATION_JSON));
        return headers;
    }
}
