package ru.practicum.service.user;

import ru.practicum.model.user.dtos.NewUserRequestDto;
import ru.practicum.model.user.dtos.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequestDto newUserRequest);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);
}
