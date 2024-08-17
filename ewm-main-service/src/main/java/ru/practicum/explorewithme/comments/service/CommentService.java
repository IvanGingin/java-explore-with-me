package ru.practicum.explorewithme.comments.service;

import ru.practicum.explorewithme.comments.dto.CommentDto;
import ru.practicum.explorewithme.comments.dto.NewCommentDto;
import ru.practicum.explorewithme.comments.dto.UpdateCommentDto;

import java.time.LocalDateTime;
import java.util.List;

public interface CommentService {
    CommentDto addComment(NewCommentDto comment, long userId);

    CommentDto getByIdPrivate(long userId, long commentId);

    CommentDto updateCommentPrivate(Long userId, Long commentId, UpdateCommentDto updateCommentDto);

    CommentDto updateCommentAdmin(long commentId, UpdateCommentDto updateCommentDto);

    void deleteComment(long userId, long commentId);

    List<CommentDto> getAllCommentAdmin(Long[] users, Long[] events,
                                        LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size);

    List<CommentDto> getAllCommentPrivate(long userId, int from, int size);

    List<CommentDto> getAllByUserId(long userId);

    List<CommentDto> getAllByEventId(long eventId);

    void deleteCommentAdmin(long commentId);

}
