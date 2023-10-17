package ru.practicum.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.EndpointHitDto;
import ru.practicum.ViewStatsDto;
import ru.practicum.exception.DateTimeValidationException;
import ru.practicum.mapper.StatsMapper;
import ru.practicum.model.EndpointHit;
import ru.practicum.repository.StatsRepository;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class StatsServiceImpl implements StatsService {
    private final StatsRepository statsRepository;

    @Override
    public EndpointHitDto saveHit(EndpointHitDto endpointHitDto) {
        EndpointHit endpointHit = StatsMapper.toEndpointHit(endpointHitDto);

        return StatsMapper.toEndpointHitDto(statsRepository.save(endpointHit));
    }

    @Override
    public List<ViewStatsDto> getStats(LocalDateTime start, LocalDateTime end, List<String> uris, Boolean unique) {
        if (start.isAfter(end)) {
            throw new DateTimeValidationException("The start time for gathering stats cannot be after the end time");
        }

        if (uris != null) {
            if (unique) {
                return statsRepository.getStatsWithUniqueIpAndWithUris(start, end, uris);
            } else {
                return statsRepository.getStatsWithoutUniqueIpAndWithUris(start, end, uris);
            }
        } else {
            if (unique) {
                return statsRepository.getStatsWithUniqueIpAndWithoutUris(start, end);
            } else {
                return statsRepository.getStatsWithoutUniqueIpAndWithoutUris(start, end);
            }
        }
    }
}





