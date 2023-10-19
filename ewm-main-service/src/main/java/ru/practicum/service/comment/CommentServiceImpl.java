package ru.practicum.service.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.exception.DateTimeValidationException;
import ru.practicum.mapper.comment.CommentMapper;
import ru.practicum.model.comment.Comment;
import ru.practicum.model.comment.dtos.CommentFullDto;
import ru.practicum.model.comment.dtos.NewCommentDto;
import ru.practicum.model.event.Event;
import ru.practicum.model.event.enums.EventState;
import ru.practicum.model.user.User;
import ru.practicum.repository.comment.CommentRepository;
import ru.practicum.repository.event.EventRepository;
import ru.practicum.repository.user.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {
    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final EventRepository eventRepository;

    @Override
    public CommentFullDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));
        User author = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %d not found.", userId)));

        if (!event.getState().equals(EventState.PUBLISHED)) {
            throw new DataConflictException("Oops, event not published yet.");
        }

        Comment comment = new Comment();
        comment.setText(newCommentDto.getText());
        comment.setAuthor(author);
        comment.setEvent(event);
        comment.setCreated(LocalDateTime.now());

        return CommentMapper.mapToCommentFullDto(commentRepository.save(comment));
    }

    @Override
    public void deleteCommentByAuthor(Long userId, Long commentId) {
        User author = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %d not found.", userId)));
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new DataNotFoundException(String.format("Comment with id %d not found.", commentId)));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new DataConflictException("Cannot to delete someone else's comment.");
        }

        commentRepository.delete(comment);
    }

    @Override
    public List<CommentFullDto> getAllCommentsByAuthor(Long userId, Long eventId, Integer from, Integer size) {
        User author = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %d not found.", userId)));

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created"));

        if (eventId == null) {
            return commentRepository.findAllByAuthorId(userId, pageable).stream()
                    .map(CommentMapper::mapToCommentFullDto).collect(Collectors.toList());
        } else {
            return commentRepository.findAllByAuthorIdAndEventId(userId, eventId, pageable).stream()
                    .map(CommentMapper::mapToCommentFullDto).collect(Collectors.toList());
        }
    }

    @Override
    public CommentFullDto changeCommentByAuthor(Long userId, Long commentId, NewCommentDto newCommentDto) {
        User author = userRepository.findById(userId).orElseThrow(
                () -> new DataNotFoundException(String.format("User with id %d not found.", userId)));
        Comment updatableComment = commentRepository.findById(commentId).orElseThrow(
                () -> new DataNotFoundException(String.format("Comment with id %d not found.", commentId)));

        if (updatableComment.getCreated().isBefore(LocalDateTime.now().minusHours(1))) {
            throw new DateTimeValidationException("Cannot edit a comment posted more than an hour ago.");
        }
        if (!updatableComment.getAuthor().getId().equals(userId)) {
            throw new DataConflictException("Cannot edit someone else's comment.");
        }
        updatableComment.setText(newCommentDto.getText());
        commentRepository.save(updatableComment);

        CommentFullDto commentFullDto = CommentMapper.mapToCommentFullDto(updatableComment);
        commentFullDto.setChanged(true);

        return commentFullDto;
    }

    @Override
    public void deleteCommentByAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new DataNotFoundException(String.format("Comment with id %d not found.", commentId)));

        commentRepository.deleteById(commentId);
    }

    @Override
    public CommentFullDto getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId).orElseThrow(
                () -> new DataNotFoundException(String.format("Comment with id %d not found.", commentId)));

        return CommentMapper.mapToCommentFullDto(comment);
    }

    @Override
    public CommentFullDto changeCommentByAdmin(Long commentId, NewCommentDto newCommentDto) {
        Comment updatableComment = commentRepository.findById(commentId).orElseThrow(
                () -> new DataNotFoundException(String.format("Comment with id %d not found.", commentId)));

        updatableComment.setText(newCommentDto.getText());
        commentRepository.save(updatableComment);

        CommentFullDto commentFullDto = CommentMapper.mapToCommentFullDto(updatableComment);
        commentFullDto.setChanged(true);

        return commentFullDto;
    }

    @Override
    public List<CommentFullDto> getAllCommentsInEvent(Long eventId, Integer from, Integer size) {
        Event event = eventRepository.findById(eventId).orElseThrow(
                () -> new DataNotFoundException(String.format("Event with id %d not found.", eventId)));

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created"));

        return commentRepository.findAllByEventId(eventId, pageable).stream()
                .map(CommentMapper::mapToCommentFullDto).collect(Collectors.toList());
    }
}
