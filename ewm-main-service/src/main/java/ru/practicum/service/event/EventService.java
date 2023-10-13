package ru.practicum.service.event;

import ru.practicum.model.event.dtos.EventFullDto;
import ru.practicum.model.event.dtos.EventShortDto;
import ru.practicum.model.event.dtos.NewEventDto;
import ru.practicum.model.event.dtos.UpdateEventUserRequest;
import ru.practicum.model.event.enums.EventState;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.List;

public interface EventService {
    EventFullDto addEvent(Long userId, NewEventDto newEventDto);

    EventFullDto getEventByEventIdPrivate(Long userId, Long eventId);

    List<EventShortDto> getAllEventsByUser(Long userId, Integer from, Integer size);

    EventFullDto changeEventByUser(Long userId, Long eventId, UpdateEventUserRequest newEventDto);

    EventFullDto changeEventByAdmin(Long eventId, UpdateEventUserRequest updateEventUserRequest);

    List<EventFullDto> getAllEventsByAdmin(List<Long> userId, List<EventState> states, List<Long> categories,
                                           LocalDateTime rangeStart, LocalDateTime rangeEnd, Integer from, Integer size);

    EventFullDto getEventByEventIdPublic(Long eventId, HttpServletRequest request);

    List<EventShortDto> getAllEventsPublic(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart,
                                           LocalDateTime rangeEnd, Boolean onlyAvailable, String sort,
                                           Integer from, Integer size, HttpServletRequest request);
}
