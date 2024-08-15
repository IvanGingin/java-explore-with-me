package ru.practicum.explorewithme.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.comments.dto.CommentDto;
import ru.practicum.explorewithme.comments.dto.UpdateCommentDto;
import ru.practicum.explorewithme.comments.service.CommentService;

import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/admin/comments")
@RequiredArgsConstructor
@Slf4j
public class AdminCommentController {
    private final CommentService commentService;

   @GetMapping
    public List<CommentDto> getAllCommentAdmin(
            @RequestParam(required = false) Long[] users,
            @RequestParam(required = false) Long[] events,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeStart,
            @RequestParam(required = false) @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime rangeEnd,
            @PositiveOrZero @RequestParam(defaultValue = "0") int from,
            @Positive @RequestParam(defaultValue = "10") int size) {
        log.info("Called getAll");
        return commentService.getAllCommentAdmin(users, events, rangeStart, rangeEnd, from, size);
    }

    @GetMapping("/users/{userId}")
    public List<CommentDto> getAllCommentByUser(@PathVariable long userId) {
        return commentService.getAllByUserId(userId);
    }

    @GetMapping("/events/{eventId}")
    public List<CommentDto> getAllCommentByEventId(@PathVariable long eventId) {
        return commentService.getAllByEventId(eventId);
    }

    @PutMapping("/{commentId}")
    public CommentDto updateCommentAdmin(@RequestBody UpdateCommentDto updateCommentDto,
                             @PathVariable long commentId) {
        return commentService.updateCommentAdmin(commentId, updateCommentDto);
    }
    @DeleteMapping("/{commentId}")
    public void deleteCommentAdmin(@PathVariable long commentId) {
        commentService.deleteCommentAdmin(commentId);
    }
}
