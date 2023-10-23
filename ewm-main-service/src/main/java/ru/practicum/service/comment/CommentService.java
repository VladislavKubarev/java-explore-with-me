package ru.practicum.service.comment;

import ru.practicum.model.comment.dtos.CommentFullDto;
import ru.practicum.model.comment.dtos.NewCommentDto;

import java.util.List;

public interface CommentService {
    CommentFullDto addComment(Long userId, Long eventId, NewCommentDto newCommentDto);

    void deleteCommentByAuthor(Long userId, Long eventId);

    List<CommentFullDto> getAllCommentsByAuthor(Long userId, Long eventId, Integer from, Integer size);

    CommentFullDto changeCommentByAuthor(Long userId, Long commentId, NewCommentDto newCommentDto);

    void deleteCommentByAdmin(Long commentId);

    CommentFullDto getCommentById(Long commentId);

    CommentFullDto changeCommentByAdmin(Long commentId, NewCommentDto newCommentDto);

    List<CommentFullDto> getAllCommentsInEvent(Long eventId, Integer from, Integer size);
}
