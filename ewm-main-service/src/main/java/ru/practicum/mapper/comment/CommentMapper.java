package ru.practicum.mapper.comment;

import ru.practicum.mapper.event.EventMapper;
import ru.practicum.mapper.user.UserMapper;
import ru.practicum.model.comment.Comment;
import ru.practicum.model.comment.dtos.CommentFullDto;

public class CommentMapper {
    public static CommentFullDto mapToCommentFullDto(Comment comment) {
        CommentFullDto commentFullDto = new CommentFullDto();
        commentFullDto.setId(comment.getId());
        commentFullDto.setText(comment.getText());
        commentFullDto.setAuthor(UserMapper.mapToUserShortDto(comment.getAuthor()));
        commentFullDto.setEvent(EventMapper.mapToEventShortDto(comment.getEvent()));
        commentFullDto.setCreatedOn(comment.getCreated());

        return commentFullDto;
    }
}
