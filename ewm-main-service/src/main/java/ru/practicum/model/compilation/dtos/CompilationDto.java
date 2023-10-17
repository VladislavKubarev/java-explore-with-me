package ru.practicum.model.compilation.dtos;

import lombok.Data;
import ru.practicum.model.event.dtos.EventShortDto;

import java.util.List;

@Data
public class CompilationDto {
    private Long id;
    private List<EventShortDto> events;
    private Boolean pinned;
    private String title;
}
