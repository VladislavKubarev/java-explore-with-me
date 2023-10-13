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
import ru.practicum.model.request.enums.RequestStatus;
import ru.practicum.model.user.User;
import ru.practicum.repository.category.CategoryRepository;
import ru.practicum.repository.event.EventRepository;
import ru.practicum.repository.location.LocationRepository;
import ru.practicum.repository.request.ParticipationRequestRepository;
import ru.practicum.repository.user.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EventServiceImpl implements EventService {
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final CategoryRepository categoryRepository;
    private final UserRepository userRepository;
    private final ParticipationRequestRepository participationRequestRepository;
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
        event.setConfirmedRequests(0L);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(EventState.PENDING);

        return EventMapper.mapToEventFullDto(eventRepository.save(event));
    }

    @Override
    public EventFullDto getEventByEventIdPrivate(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %d not found.", userId)));
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));

        getViewStatistics(event);

        return EventMapper.mapToEventFullDto(eventRepository.findByInitiatorIdAndId(userId, eventId));
    }

    @Override
    public List<EventShortDto> getAllEventsByUser(Long userId, Integer from, Integer size) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %d not found.", userId)));

        Pageable pageable = PageRequest.of(from / size, size);

        return eventRepository.findAllByInitiatorId(userId, pageable).stream()
                .peek(this::getViewStatistics)
                .map(EventMapper::mapToEventShortDto).collect(Collectors.toList());
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

        return eventRepository.findAll(filter, pageable).getContent().stream()
                .peek(this::getViewStatistics)
                .map(EventMapper::mapToEventFullDto).collect(Collectors.toList());
    }

    @Override
    public EventFullDto getEventByEventIdPublic(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));

        saveHit(request);
        getViewStatistics(event);

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataNotFoundException(String.format("Event with id %d was not published", eventId));
        } else {
            return EventMapper.mapToEventFullDto(event);
        }
    }

    @Override
    public List<EventShortDto> getAllEventsPublic(String text, List<Long> categories, Boolean paid,
                                                  LocalDateTime rangeStart, LocalDateTime rangeEnd,
                                                  Boolean onlyAvailable, String sort, Integer from,
                                                  Integer size, HttpServletRequest request) {

        if (rangeStart != null && rangeEnd != null && rangeStart.isAfter(rangeEnd)) {
            throw new DateTimeValidationException("The start of the event cannot be after the end of the event.");
        }

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

        if (sort != null && sort.equals("EVENT_DATE")) {
            events.stream().sorted(Comparator.comparing(Event::getEventDate));
        }
        if (sort != null && sort.equals("VIEWS")) {
            events.stream().sorted(Comparator.comparing(Event::getViews));
        }

        saveHit(request);

        if (onlyAvailable) {
            return events.stream()
                    .filter(e -> e.getParticipantLimit() > participationRequestRepository.countByEventIdAndStatus(e.getId(), RequestStatus.CONFIRMED))
                    .filter(e -> e.getState().equals(EventState.PUBLISHED))
                    .peek(this::getViewStatistics)
                    .map(EventMapper::mapToEventShortDto).collect(Collectors.toList());
        } else {
            return events.stream()
                    .filter(e -> e.getState().equals(EventState.PUBLISHED))
                    .peek(this::getViewStatistics)
                    .map(EventMapper::mapToEventShortDto).collect(Collectors.toList());
        }
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

    private void saveHit(HttpServletRequest httpServletRequest) {
        EndpointHitDto endpointHitDto = new EndpointHitDto();
        endpointHitDto.setApp("ewm-main-service");
        endpointHitDto.setUri(httpServletRequest.getRequestURI());
        endpointHitDto.setIp(httpServletRequest.getRemoteAddr());
        endpointHitDto.setTimestamp(LocalDateTime.now());

        statsClient.saveHit(endpointHitDto);
    }

    private void getViewStatistics(Event event) {
        List<ViewStatsDto> stats = statsClient.getStats(event.getCreatedOn(),
                LocalDateTime.now(), List.of("/events/" + event.getId()), true);

        if (stats.size() == 0) {
            event.setViews(0L);
        } else {
            event.setViews(stats.get(0).getHits());
        }
    }
}
