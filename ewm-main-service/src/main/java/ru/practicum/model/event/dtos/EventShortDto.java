package ru.practicum.model.event.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import ru.practicum.model.category.dtos.CategoryDto;
import ru.practicum.model.user.dtos.UserShortDto;

import java.time.LocalDateTime;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class EventShortDto {
    private Long id;
    private String annotation;
    private UserShortDto initiator;
    private CategoryDto category;
    private Long confirmedRequests;
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime eventDate;
    private Boolean paid;
    private String title;
    private Long views;
    private Long numberOfComments;
}
