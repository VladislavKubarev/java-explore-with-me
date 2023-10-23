package ru.practicum.controller.adminApi.comment;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.model.comment.dtos.CommentFullDto;
import ru.practicum.model.comment.dtos.NewCommentDto;
import ru.practicum.service.comment.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;

@RestController
@RequiredArgsConstructor
@RequestMapping("/admin/comments")
@Validated
public class AdminCommentController {
    private final CommentService commentService;

    @DeleteMapping("/{commentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentByAdmin(@Positive @PathVariable Long commentId) {
        commentService.deleteCommentByAdmin(commentId);
    }

    @GetMapping("/{commentId}")
    public CommentFullDto getCommentById(@Positive @PathVariable Long commentId) {
        return commentService.getCommentById(commentId);

    }

    @PatchMapping("/{commentId}")
    public CommentFullDto changeCommentByAdmin(@Positive @PathVariable Long commentId,
                                               @Valid @RequestBody NewCommentDto newCommentDto) {
        return commentService.changeCommentByAdmin(commentId, newCommentDto);
    }
}
