package ru.practicum.service.user;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.user.User;
import ru.practicum.model.user.dtos.NewUserRequestDto;
import ru.practicum.model.user.dtos.UserDto;
import ru.practicum.repository.user.UserRepository;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;

    @Override
    public UserDto createUser(NewUserRequestDto newUserRequest) {
        User user = UserMapper.mapToUser(newUserRequest);

        try {
            return UserMapper.mapToUserDto(userRepository.save(user));
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException(String.format("User with email %s already exists.", newUserRequest.getEmail()));
        }
    }

    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);

        if (ids == null) {
            return userRepository.findAll(pageable).stream().map(UserMapper::mapToUserDto).collect(Collectors.toList());
        } else {
            return userRepository.findByIdIn(ids, pageable).stream().map(UserMapper::mapToUserDto).collect(Collectors.toList());
        }
    }

    @Override
    public void deleteUser(Long userId) {
        try {
            userRepository.deleteById(userId);
        } catch (EmptyResultDataAccessException e) {
            throw new DataNotFoundException(String.format("User with id %d not found.", userId));
        }
    }
}
