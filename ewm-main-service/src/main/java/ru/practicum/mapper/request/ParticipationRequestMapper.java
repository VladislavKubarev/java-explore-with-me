package ru.practicum.mapper.request;

import ru.practicum.model.request.ParticipationRequest;
import ru.practicum.model.request.dtos.ParticipationRequestDto;

public class ParticipationRequestMapper {
    public static ParticipationRequestDto mapToParticipationRequestDto(ParticipationRequest participationRequest) {
        ParticipationRequestDto participationRequestDto = new ParticipationRequestDto();
        participationRequestDto.setId(participationRequest.getId());
        participationRequestDto.setEvent(participationRequest.getEvent().getId());
        participationRequestDto.setRequester(participationRequest.getRequester().getId());
        participationRequestDto.setCreated(participationRequest.getCreated());
        participationRequestDto.setStatus(participationRequest.getStatus());

        return participationRequestDto;
    }
}
