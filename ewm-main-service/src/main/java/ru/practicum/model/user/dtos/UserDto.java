package ru.practicum.model.user.dtos;

import lombok.Data;

@Data
public class UserDto {
    private long id;
    private String name;
    private String email;
}
