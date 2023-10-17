package ru.practicum.mapper.user;

import ru.practicum.model.user.User;
import ru.practicum.model.user.dtos.NewUserRequestDto;
import ru.practicum.model.user.dtos.UserDto;
import ru.practicum.model.user.dtos.UserShortDto;

public class UserMapper {
    public static User mapToUser(NewUserRequestDto newUserRequest) {
        User user = new User();
        user.setName(newUserRequest.getName());
        user.setEmail(newUserRequest.getEmail());

        return user;
    }

    public static UserDto mapToUserDto(User user) {
        UserDto userDto = new UserDto();
        userDto.setId(user.getId());
        userDto.setName(user.getName());
        userDto.setEmail(user.getEmail());

        return userDto;
    }

    public static UserShortDto mapToUserShortDto(User user) {
        UserShortDto userShortDto = new UserShortDto();
        userShortDto.setId(user.getId());
        userShortDto.setName(user.getName());

        return userShortDto;
    }
}
