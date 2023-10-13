package ru.practicum.model.request.dtos;

import lombok.Data;
import ru.practicum.model.request.enums.RequestStatus;

@Data
public class NewParticipationRequestDto {

    private Long event;
    private Long requester;
    private RequestStatus status;
}
