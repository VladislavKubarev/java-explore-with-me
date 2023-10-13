package ru.practicum.service.request;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.mapper.request.ParticipationRequestMapper;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.enums.EventState;
import ru.practicum.model.request.ParticipationRequest;
import ru.practicum.model.request.dtos.EventRequestStatusUpdateRequest;
import ru.practicum.model.request.dtos.EventRequestStatusUpdateResult;
import ru.practicum.model.request.dtos.ParticipationRequestDto;
import ru.practicum.model.request.enums.RequestStatus;
import ru.practicum.model.user.User;
import ru.practicum.repository.event.EventRepository;
import ru.practicum.repository.request.ParticipationRequestRepository;
import ru.practicum.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ParticipationRequestImpl implements ParticipationRequestService {
    private final ParticipationRequestRepository participationRequestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public ParticipationRequestDto createRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));
        User requester = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %d not found.", userId)));

        ParticipationRequest participationRequest = new ParticipationRequest();
        participationRequest.setEvent(event);
        participationRequest.setRequester(requester);
        participationRequest.setCreated(LocalDateTime.now());

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException(String.format("Event with id %d has not yet been published", eventId));
        }
        if (event.getInitiator().getId().equals(userId)) {
            throw new DataConflictException("The event initiator cannot add a request to participate in his event");
        }
        if (participationRequestRepository.existsParticipationRequestByEventIdAndRequesterId(eventId, userId)) {
            throw new DataConflictException(String.format("A request from user with id %d to participate in an event with id %d already exists", userId, eventId));
        }
        if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
            participationRequest.setStatus(RequestStatus.CONFIRMED);
            event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            eventRepository.save(event);
        } else {
            participationRequest.setStatus(RequestStatus.PENDING);
        }
        if (participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED) >= event.getParticipantLimit()
                && event.getParticipantLimit() != 0) {
            throw new DataConflictException("The limit of requests for participation in the event has been exceeded");
        }

        return ParticipationRequestMapper.mapToParticipationRequestDto(participationRequestRepository.save(participationRequest));
    }

    @Override
    public List<ParticipationRequestDto> getRequestsInEventByUser(Long userId, Long eventId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %d not found.", userId)));
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));

        return participationRequestRepository.findAllByEventId(eventId).stream()
                .map(ParticipationRequestMapper::mapToParticipationRequestDto).collect(Collectors.toList());
    }

    @Override
    public EventRequestStatusUpdateResult changeStatusRequestInEventByUser(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %d not found.", userId)));
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));

        List<ParticipationRequest> participationRequests = participationRequestRepository.findAllByEventIdAndIdIn(eventId, eventRequestStatusUpdateRequest.getRequestIds());
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        for (ParticipationRequest request : participationRequests) {
            if (!request.getStatus().equals(RequestStatus.PENDING)) {
                throw new DataConflictException("Request must have status PENDING");
            }
            if (!event.getRequestModeration() || event.getParticipantLimit() == 0) {
                request.setStatus(RequestStatus.CONFIRMED);
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                eventRepository.save(event);
            }
            if (eventRequestStatusUpdateRequest.getStatus().equals(RequestStatus.CONFIRMED)) {
                if (participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED) >= event.getParticipantLimit()
                        && event.getParticipantLimit() != 0) {
                    throw new DataConflictException("The limit of requests for participation in the event has been exceeded");
                } else {
                    request.setStatus(RequestStatus.CONFIRMED);
                    event.setConfirmedRequests(event.getConfirmedRequests() + 1);
                    eventRepository.save(event);
                    participationRequestRepository.save(request);
                    confirmedRequests.add(ParticipationRequestMapper.mapToParticipationRequestDto(request));
                }
            } else {
                request.setStatus(RequestStatus.REJECTED);
                participationRequestRepository.save(request);
                rejectedRequests.add(ParticipationRequestMapper.mapToParticipationRequestDto(request));
            }
        }

        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }

    @Override
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %d not found.", userId)));

        ParticipationRequest participationRequest = participationRequestRepository.findById(requestId).orElseThrow(
                () -> new DataNotFoundException(String.format("Request with id %d not found.", requestId)));

        participationRequest.setStatus(RequestStatus.CANCELED);

        return ParticipationRequestMapper.mapToParticipationRequestDto(participationRequestRepository.save(participationRequest));
    }

    @Override
    public List<ParticipationRequestDto> getAllRequestsByUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %d not found.", userId)));

        return participationRequestRepository.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::mapToParticipationRequestDto).collect(Collectors.toList());
    }
}
