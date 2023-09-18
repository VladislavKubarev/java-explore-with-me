package ru.practicum.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.practicum.ViewStatsDto;
import ru.practicum.model.EndpointHit;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface StatsRepository extends JpaRepository<EndpointHit, Long> {
    @Query("select new ru.practicum.ViewStatsDto(e.app, e.uri, count(e.ip)) " +
            "from EndpointHit as e " +
            "where e.timestamp between ?1 and ?2 " +
            "group by e.app, e.uri " +
            "order by count(e.ip) desc")
    List<ViewStatsDto> getStatsWithoutUniqueIpAndWithoutUris(LocalDateTime start, LocalDateTime end);

    @Query("select new ru.practicum.ViewStatsDto(e.app, e.uri, count(e.ip)) " +
            "from EndpointHit as e " +
            "where e.timestamp between ?1 and ?2 " +
            "and e.uri in ?3 " +
            "group by e.app, e.uri " +
            "order by count(e.ip) desc")
    List<ViewStatsDto> getStatsWithoutUniqueIpAndWithUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.ViewStatsDto(e.app, e.uri, count(distinct e.ip)) " +
            "from EndpointHit as e " +
            "where e.timestamp between ?1 and ?2 " +
            "and e.uri in ?3 " +
            "group by e.app, e.uri " +
            "order by count(distinct e.ip) desc")
    List<ViewStatsDto> getStatsWithUniqueIpAndWithUris(LocalDateTime start, LocalDateTime end, List<String> uris);

    @Query("select new ru.practicum.ViewStatsDto(e.app, e.uri, count(distinct e.ip)) " +
            "from EndpointHit as e " +
            "where e.timestamp between ?1 and ?2 " +
            "group by e.app, e.uri " +
            "order by count(distinct e.ip) desc")
    List<ViewStatsDto> getStatsWithUniqueIpAndWithoutUris(LocalDateTime start, LocalDateTime end);
}
