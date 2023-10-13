package ru.practicum.service.request;

import ru.practicum.model.request.dtos.EventRequestStatusUpdateRequest;
import ru.practicum.model.request.dtos.EventRequestStatusUpdateResult;
import ru.practicum.model.request.dtos.ParticipationRequestDto;

import java.util.List;

public interface ParticipationRequestService {
    ParticipationRequestDto createRequest(Long userId, Long eventId);

    List<ParticipationRequestDto> getRequestsInEventByUser(Long userId, Long eventId);

    EventRequestStatusUpdateResult changeStatusRequestInEventByUser(Long userId, Long eventId, EventRequestStatusUpdateRequest eventRequestStatusUpdateRequest);

    ParticipationRequestDto cancelRequest(Long userId, Long requestId);

    List<ParticipationRequestDto> getAllRequestsByUser(Long userId);
}
