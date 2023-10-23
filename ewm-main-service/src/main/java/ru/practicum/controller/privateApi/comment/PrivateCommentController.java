package ru.practicum.controller.privateApi.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.comment.dtos.CommentFullDto;
import ru.practicum.model.comment.dtos.NewCommentDto;
import ru.practicum.service.comment.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/users/{userId}/comments")
@Validated
public class PrivateCommentController {
    private final CommentService commentService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CommentFullDto addComment(@Positive @PathVariable Long userId,
                                     @Positive @RequestParam Long eventId,
                                     @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.addComment(userId, eventId, newCommentDto);
    }

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAuthor(@Positive @PathVariable Long userId, @Positive @PathVariable Long commentId) {
        commentService.deleteCommentByAuthor(userId, commentId);
    }

    @GetMapping
    public List<CommentFullDto> getAllCommentsByAuthor(@Positive @PathVariable Long userId,
                                                       @Positive @RequestParam(required = false) Long eventId,
                                                       @PositiveOrZero @RequestParam(defaultValue = "0") Integer from,
                                                       @Positive @RequestParam(defaultValue = "10") Integer size) {

        return commentService.getAllCommentsByAuthor(userId, eventId, from, size);
    }

    @PatchMapping("/{commentId}")
    public CommentFullDto changeCommentByAuthor(@Positive @PathVariable Long userId,
                                                @Positive @PathVariable Long commentId,
                                                @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.changeCommentByAuthor(userId, commentId, newCommentDto);
    }
}
