package ru.practicum.service.event;

import com.querydsl.core.BooleanBuilder;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.practicum.EndpointHitDto;
import ru.practicum.StatsClient;
import ru.practicum.ViewStatsDto;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.exception.DateTimeValidationException;
import ru.practicum.mapper.event.EventMapper;
import ru.practicum.mapper.location.LocationMapper;
import ru.practicum.model.category.Category;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.QEvent;
import ru.practicum.model.event.dtos.EventShortDto;
import ru.practicum.model.event.dtos.UpdateEventUserRequest;
import ru.practicum.model.event.enums.EventState;
import ru.practicum.model.event.dtos.EventFullDto;
import ru.practicum.model.event.dtos.NewEventDto;
import ru.practicum.model.location.Location;
import ru.practicum.model.request.ParticipationRequest;
import ru.practicum.model.request.enums.RequestStatus;
import ru.practicum.model.user.User;
import ru.practicum.repository.category.CategoryRepository;
import ru.practicum.repository.comment.CommentRepository;
import ru.practicum.repository.event.EventRepository;
import ru.practicum.repository.location.LocationRepository;
import ru.practicum.repository.request.ParticipationRequestRepository;
import ru.practicum.repository.user.UserRepository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final CommentRepository commentRepository;
    private final StatsClient statsClient = new StatsClient("http://stats-server:9090", new RestTemplate());


    @Override
    public EventFullDto addEvent(Long userId, NewEventDto newEventDto) {
        dateTimeValidationUser(newEventDto.getEventDate());
        Event event = EventMapper.mapToEvent(newEventDto);

        Location location = locationRepository.save(LocationMapper.mapToLocation(newEventDto.getLocation()));
        Category category = categoryRepository.findById(newEventDto.getCategory()).orElseThrow(
                () -> new DataNotFoundException(String.format("Category with id %d not found", newEventDto.getCategory())));
        User initiator = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %d not found.", userId)));

        if (newEventDto.getPaid() == null) {
            event.setPaid(false);
        }
        if (newEventDto.getParticipantLimit() == null) {
            event.setParticipantLimit(0L);
        }
        if (newEventDto.getRequestModeration() == null) {
            event.setRequestModeration(true);
        }

        event.setLocation(location);
        event.setInitiator(initiator);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        return EventMapper.mapToEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEventByEventIdPrivate(Long userId, Long eventId) {
        Event event = eventRepository.findByInitiatorIdAndId(userId, eventId).orElseThrow(
                () -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));

        EventFullDto eventFullDto = EventMapper.mapToEventFullDto(event);

        Long countConfirmedRequests = participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
        Map<Long, Long> views = getViewStatistics(List.of(event));

        eventFullDto.setConfirmedRequests(countConfirmedRequests);
        eventFullDto.setViews(views.getOrDefault(eventFullDto.getId(), 0L));

        return eventFullDto;
    }

    @Override
    public List<EventShortDto> getAllEventsByUser(Long userId, Integer from, Integer size) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %d not found.", userId)));

        Pageable pageable = PageRequest.of(from / size, size);

        List<Event> events = eventRepository.findAllByInitiatorId(userId, pageable);
        List<Long> eventsId = events.stream().map(Event::getId).collect(Collectors.toList());

        Map<Long, Long> countConfirmedRequests = getConfirmedRequests(eventsId);
        Map<Long, Long> views = getViewStatistics(events);

        List<EventShortDto> eventsShortDto = events.stream()
                .map(EventMapper::mapToEventShortDto).collect(Collectors.toList());

        for (EventShortDto event : eventsShortDto) {
            event.setConfirmedRequests(countConfirmedRequests.getOrDefault(event.getId(), 0L));
            event.setViews(views.getOrDefault(event.getId(), 0L));
            event.setNumberOfComments(commentRepository.countByEventId(event.getId()));
        }

        return eventsShortDto;
    }

    @Override
    public EventFullDto changeEventByUser(Long userId, Long eventId, UpdateEventUserRequest newEventDto) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %d not found.", userId)));
        Event updatableEvent = eventRepository.findById(eventId).orElseThrow(
                () -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));

        if (updatableEvent.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException("Only pending or canceled events can be changed");
        }
        if (newEventDto.getAnnotation() != null) {
            updatableEvent.setAnnotation(newEventDto.getAnnotation());
        }
        if (newEventDto.getCategory() != null) {
            Category category = categoryRepository.findById(newEventDto.getCategory()).orElseThrow(
                    () -> new DataNotFoundException(String.format("Category with id %d not found", newEventDto.getCategory())));
            updatableEvent.setCategory(category);
        }
        if (newEventDto.getDescription() != null) {
            updatableEvent.setDescription(newEventDto.getDescription());
        }
        if (newEventDto.getTitle() != null) {
            updatableEvent.setTitle(newEventDto.getTitle());
        }
        if (newEventDto.getEventDate() != null) {
            updatableEvent.setEventDate(newEventDto.getEventDate());
            dateTimeValidationUser(updatableEvent.getEventDate());
        }
        if (newEventDto.getLocation() != null) {
            Location location = locationRepository.save(LocationMapper.mapToLocation(newEventDto.getLocation()));
            updatableEvent.setLocation(location);
        }
        if (newEventDto.getPaid() != null) {
            updatableEvent.setPaid(newEventDto.getPaid());
        }
        if (newEventDto.getParticipantLimit() != null) {
            updatableEvent.setParticipantLimit(newEventDto.getParticipantLimit());
        }
        if (newEventDto.getRequestModeration() != null) {
            updatableEvent.setRequestModeration(newEventDto.getRequestModeration());
        }
        if (newEventDto.getStateAction() != null) {
            switch (newEventDto.getStateAction()) {
                case SEND_TO_REVIEW:
                    updatableEvent.setState(EventState.PENDING);
                    break;
                case CANCEL_REVIEW:
                    updatableEvent.setState(EventState.CANCELED);
                    break;
            }
        }

        return EventMapper.mapToEventFullDto(eventRepository.save(updatableEvent));
    }

    @Override
    public EventFullDto changeEventByAdmin(Long eventId, UpdateEventUserRequest newEventDto) {
        Event updatableEvent = eventRepository.findById(eventId).orElseThrow(
                () -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));

        if (updatableEvent.getState().equals(EventState.CANCELED)) {
            throw new DataConflictException("The event cannot be published because it has been cancelled.");
        }
        if (newEventDto.getAnnotation() != null) {
            updatableEvent.setAnnotation(newEventDto.getAnnotation());
        }
        if (newEventDto.getCategory() != null) {
            Category category = categoryRepository.findById(newEventDto.getCategory()).orElseThrow(
                    () -> new DataNotFoundException(String.format("Category with id %d not found", newEventDto.getCategory())));
            updatableEvent.setCategory(category);
        }
        if (newEventDto.getDescription() != null) {
            updatableEvent.setDescription(newEventDto.getDescription());
        }
        if (newEventDto.getTitle() != null) {
            updatableEvent.setTitle(newEventDto.getTitle());
        }
        if (newEventDto.getEventDate() != null) {
            dateTimeValidationAdmin(newEventDto.getEventDate());
            updatableEvent.setEventDate(newEventDto.getEventDate());
        }
        if (newEventDto.getLocation() != null) {
            Location location = locationRepository.save(LocationMapper.mapToLocation(newEventDto.getLocation()));
            updatableEvent.setLocation(location);
        }
        if (newEventDto.getPaid() != null) {
            updatableEvent.setPaid(newEventDto.getPaid());
        }
        if (newEventDto.getParticipantLimit() != null) {
            updatableEvent.setParticipantLimit(newEventDto.getParticipantLimit());
        }
        if (newEventDto.getRequestModeration() != null) {
            updatableEvent.setRequestModeration(newEventDto.getRequestModeration());
        }
        if (newEventDto.getStateAction() != null) {
            switch (newEventDto.getStateAction()) {
                case PUBLISH_EVENT:
                    if (updatableEvent.getState().equals(EventState.PUBLISHED)) {
                        throw new DataConflictException("The event cannot be published because it is already published");
                    }
                    updatableEvent.setState(EventState.PUBLISHED);
                    updatableEvent.setPublishedOn(LocalDateTime.now());
                    break;
                case REJECT_EVENT:
                    if (updatableEvent.getState().equals(EventState.PUBLISHED)) {
                        throw new DataConflictException("The event cannot be canceled because it has already been published");
                    }
                    updatableEvent.setState(EventState.CANCELED);
                    break;
            }
        }

        return EventMapper.mapToEventFullDto(eventRepository.save(updatableEvent));
    }

    @Override
    public List<EventFullDto> getAllEventsByAdmin(List<Long> users, List<EventState> states,
                                                  List<Long> categories, LocalDateTime rangeStart,
                                                  LocalDateTime rangeEnd, Integer from, Integer size) {

        Pageable pageable = PageRequest.of(from / size, size);

        BooleanBuilder filter = new BooleanBuilder();
        if (!ObjectUtils.isEmpty(users)) {
            filter.and(QEvent.event.initiator.id.in(users));
        }
        if (!ObjectUtils.isEmpty(states)) {
            filter.and(QEvent.event.state.in(states));
        }
        if (!ObjectUtils.isEmpty(categories)) {
            filter.and(QEvent.event.category.id.in(categories));
        }
        if (!ObjectUtils.isEmpty(rangeStart)) {
            filter.and(QEvent.event.eventDate.goe(rangeStart));
        }
        if (!ObjectUtils.isEmpty(rangeEnd)) {
            filter.and(QEvent.event.eventDate.loe(rangeEnd));
        }

        List<Event> events = eventRepository.findAll(filter, pageable).getContent();
        List<Long> eventsId = events.stream().map(Event::getId).collect(Collectors.toList());

        Map<Long, Long> countConfirmedRequests = getConfirmedRequests(eventsId);
        Map<Long, Long> views = getViewStatistics(events);

        List<EventFullDto> eventsFullDto = events.stream()
                .map(EventMapper::mapToEventFullDto).collect(Collectors.toList());

        for (EventFullDto event : eventsFullDto) {
            event.setConfirmedRequests(countConfirmedRequests.getOrDefault(event.getId(), 0L));
            event.setViews(views.getOrDefault(event.getId(), 0L));
        }

        return eventsFullDto;
    }

    @Override
    public EventFullDto getEventByEventIdPublic(Long eventId, String uri, String ip) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));

        saveHit(uri, ip);
        Map<Long, Long> views = getViewStatistics(List.of(event));
        Long countConfirmedRequests = participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);

        EventFullDto eventFullDto = EventMapper.mapToEventFullDto(event);
        eventFullDto.setConfirmedRequests(countConfirmedRequests);
        eventFullDto.setViews(views.getOrDefault(eventFullDto.getId(), 0L));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataNotFoundException(String.format("Event with id %d was not published", eventId));
        } else {
            return eventFullDto;
        }
    }

    @Override
    public List<EventShortDto> getAllEventsPublic(String text, List<Long> categories, Boolean paid,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                  Boolean onlyAvailable, String sort, Integer from,
                                                  Integer size, String uri, String ip) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new DateTimeValidationException("The start of the event cannot be after the end of the event.");
        }

        saveHit(uri, ip);

        Pageable pageable = PageRequest.of(from / size, size);

        BooleanBuilder filter = new BooleanBuilder();
        if (StringUtils.isNotBlank(text)) {
            filter.and(QEvent.event.annotation.containsIgnoreCase(text).or(QEvent.event.description.containsIgnoreCase(text)));
        }
        if (!ObjectUtils.isEmpty(categories)) {
            filter.and(QEvent.event.category.id.in(categories));
        }
        if (!ObjectUtils.isEmpty(paid)) {
            filter.and(QEvent.event.paid.eq(paid));
        }
        if (!ObjectUtils.isEmpty(rangeStart)) {
            filter.and(QEvent.event.eventDate.goe(rangeStart));
        }
        if (!ObjectUtils.isEmpty(rangeEnd)) {
            filter.and(QEvent.event.eventDate.loe(rangeEnd));
        }

        List<Event> events = eventRepository.findAll(filter, pageable).getContent();
        List<Long> eventsId = events.stream().map(Event::getId).collect(Collectors.toList());

        Map<Long, Long> countConfirmedRequests = getConfirmedRequests(eventsId);

        if (onlyAvailable) {
            events = events.stream()
                    .filter(e -> e.getParticipantLimit() > countConfirmedRequests.getOrDefault(e.getId(), 0L))
                    .collect(Collectors.toList());
        }

        Map<Long, Long> views = getViewStatistics(events);

        List<EventShortDto> eventShortDto = events.stream()
                .map(EventMapper::mapToEventShortDto).collect(Collectors.toList());

        for (EventShortDto event : eventShortDto) {
            event.setConfirmedRequests(countConfirmedRequests.getOrDefault(event.getId(), 0L));
            event.setViews(views.getOrDefault(event.getId(), 0L));
            event.setNumberOfComments(commentRepository.countByEventId(event.getId()));
        }

        if (sort != null && sort.equals("EVENT_DATE")) {
            eventShortDto.stream().sorted(Comparator.comparing(EventShortDto::getEventDate));
        }
        if (sort != null && sort.equals("VIEWS")) {
            eventShortDto.stream().sorted(Comparator.comparing(EventShortDto::getViews));
        }

        return eventShortDto;
    }

    private void dateTimeValidationUser(LocalDateTime eventDate) {
        if (Duration.between(eventDate, LocalDateTime.now()).toHours() > 2) {
            throw new DateTimeValidationException("The start date and time of the event cannot be earlier than two hours from the current moment.");
        }
    }

    private void dateTimeValidationAdmin(LocalDateTime eventDate) {
        if (eventDate.isBefore(LocalDateTime.now().plusHours(1))) {
            throw new DateTimeValidationException("The start date and time of the event must be no earlier than an hour from the date of publication.");
        }
    }

    private void saveHit(String uri, String ip) {
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp("ewm-main-service");
        endpointHitDto.setUri(uri);
        endpointHitDto.setIp(ip);
        endpointHitDto.setTimestamp(LocalDateTime.now());

        statsClient.saveHit(endpointHitDto);
    }

    private Map<Long, Long> getViewStatistics(List<Event> events) {
        List<String> uris = new ArrayList<>();
        for (Event ev : events) {
            String uri = "/events/" + ev.getId();
            uris.add(uri);
        }

        List<ViewStatsDto> stats = statsClient.getStats(LocalDateTime.of(1990, 1, 1, 0, 0),
                LocalDateTime.now(), uris, true);

        Map<Long, Long> views = new HashMap<>();
        for (ViewStatsDto viewStatsDto : stats) {
            String uri = viewStatsDto.getUri();
            String[] split = uri.split("/");
            String id = split[2];
            Long eventId = Long.parseLong(id);
            views.put(eventId, viewStatsDto.getHits());
        }

        return views;
    }

    private Map<Long, Long> getConfirmedRequests(List<Long> eventId) {
        List<ParticipationRequest> confirmedRequests = participationRequestRepository.findAllByEventIdInAndStatus(eventId, RequestStatus.CONFIRMED);

        return confirmedRequests.stream()
                .collect(Collectors.groupingBy(participationRequest -> participationRequest.getEvent().getId()))
                .entrySet()
                .stream()
                .collect(Collectors.toMap(Map.Entry::getKey, req -> Long.valueOf(req.getValue().size())));
    }
}
