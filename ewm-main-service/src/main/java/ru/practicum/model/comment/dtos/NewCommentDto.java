package ru.practicum.model.comment.dtos;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class NewCommentDto {
    @NotBlank
    @Length(min = 2, max = 1000)
    private String text;
}
