package ru.practicum.explorewithme.comments.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.comments.dto.CommentDto;
import ru.practicum.explorewithme.comments.dto.NewCommentDto;
import ru.practicum.explorewithme.comments.model.Comment;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.user.model.User;

import java.time.LocalDateTime;

@UtilityClass
public class CommentMapper {

    public Comment toComment(NewCommentDto newCommentDto, User author, Event event) {
        return Comment.builder()
                .user(author)
                .event(event)
                .text(newCommentDto.getText())
                .created(LocalDateTime.now())
                .build();
    }

    public CommentDto toCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .userId(comment.getUser().getId())
                .eventId(comment.getEvent().getId())
                .created(comment.getCreated())
                .build();
    }
}
