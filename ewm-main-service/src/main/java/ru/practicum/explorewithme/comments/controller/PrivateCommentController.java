package ru.practicum.explorewithme.comments.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.comments.dto.CommentDto;
import ru.practicum.explorewithme.comments.dto.NewCommentDto;
import ru.practicum.explorewithme.comments.dto.UpdateCommentDto;
import ru.practicum.explorewithme.comments.service.CommentService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@Validated
@RequestMapping("/users/{userId}/comments")
public class PrivateCommentController {

    private final CommentService commentService;

    @PostMapping
    @RequestMapping
    public CommentDto addComment(@PathVariable @Positive long userId,
                                     @RequestBody @Valid NewCommentDto newCommentDto) {
        return commentService.addComment(newCommentDto, userId);
    }

    @PatchMapping("/{commentId}")
    public CommentDto updateCommentPrivate(@PathVariable long userId,
                             @PathVariable long commentId,
                             @Valid @RequestBody UpdateCommentDto updateCommentDto) {
        return commentService.updateCommentPrivate(userId, commentId, updateCommentDto);
    }

    @GetMapping
    public List<CommentDto> getAllPrivate(@PathVariable long userId,
                                   @PositiveOrZero @RequestParam(value = "from", defaultValue = "0") int from,
                                   @Positive @RequestParam(value = "size", defaultValue = "10") int size) {
        return commentService.getAllCommentPrivate(userId, from, size);
    }

    @GetMapping("/{commentId}")
    public CommentDto getByIdPrivate(@PathVariable long userId, @PathVariable long commentId) {
        return commentService.getByIdPrivate(userId, commentId);
    }

    @DeleteMapping("/{commentId}")
    public void deleteComment(@PathVariable long userId, @PathVariable long commentId) {
        commentService.deleteComment(userId, commentId);
    }
}
