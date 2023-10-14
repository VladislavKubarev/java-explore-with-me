package ru.practicum.controller.privateApi.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.event.dtos.EventFullDto;
import ru.practicum.model.event.dtos.EventShortDto;
import ru.practicum.model.event.dtos.NewEventDto;
import ru.practicum.model.event.dtos.UpdateEventUserRequest;
import ru.practicum.model.request.dtos.EventRequestStatusUpdateRequest;
import ru.practicum.model.request.dtos.EventRequestStatusUpdateResult;
import ru.practicum.model.request.dtos.ParticipationRequestDto;
import ru.practicum.service.event.EventService;
import ru.practicum.service.request.ParticipationRequestService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Slf4j
@Validated
@RequestMapping("/users/{userId}/events")
public class PrivateEventController {
    private final EventService eventService;
    private final ParticipationRequestService participationRequestService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public EventFullDto addEvent(@Positive @PathVariable Long userId, @Valid @RequestBody NewEventDto newEventDto) {
        return eventService.addEvent(userId, newEventDto);
    }

    @GetMapping("/{eventId}")
    public EventFullDto getEventByEventIdPrivate(@Positive @PathVariable Long userId,
                                                 @Positive @PathVariable Long eventId) {
        return eventService.getEventByEventIdPrivate(userId, eventId);
    }

    @GetMapping
    public List<EventShortDto> getAllEventsByUser(@Positive @PathVariable Long userId,
                                                  @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                  @Positive @RequestParam(defaultValue = "10") Integer size) {
        return eventService.getAllEventsByUser(userId, from, size);
    }

    @PatchMapping("/{eventId}")
    public EventFullDto changeEventByUser(@Positive @PathVariable Long userId, @Positive @PathVariable Long eventId,
                                          @Valid @RequestBody UpdateEventUserRequest newEventDto) {
        return eventService.changeEventByUser(userId, eventId, newEventDto);
    }

    @GetMapping("/{eventId}/requests")
    public List<ParticipationRequestDto> getRequestsInEventByUser(@Positive @PathVariable Long userId,
                                                                  @Positive @PathVariable Long eventId) {
        return participationRequestService.getRequestsInEventByUser(userId, eventId);
    }

    @PatchMapping("/{eventId}/requests")
    public EventRequestStatusUpdateResult changeStatusRequestInEventByUser(@Positive @PathVariable Long userId,
                                                                           @Positive @PathVariable Long eventId,
                                                                           @RequestBody EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        return participationRequestService.changeStatusRequestInEventByUser(userId, eventId, eventRequestStatusUpdateRequest);
    }
}
