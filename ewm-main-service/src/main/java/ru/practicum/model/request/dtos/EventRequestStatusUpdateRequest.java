package ru.practicum.model.request.dtos;

import lombok.Data;
import ru.practicum.model.request.enums.RequestStatus;

import java.util.List;

@Data
public class EventRequestStatusUpdateRequest {
    private List<Long> requestIds;
    private RequestStatus status;
}
