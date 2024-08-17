package ru.practicum.explorewithme.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.comments.dto.CommentDto;
import ru.practicum.explorewithme.comments.dto.NewCommentDto;
import ru.practicum.explorewithme.comments.dto.UpdateCommentDto;
import ru.practicum.explorewithme.comments.mapper.CommentMapper;
import ru.practicum.explorewithme.comments.model.Comment;
import ru.practicum.explorewithme.comments.repository.CommentRepository;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public CommentDto addComment(NewCommentDto newCommentDto, long userId) {
        User author = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Event event = eventRepository.findById(newCommentDto.getEventId())
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        Comment comment = CommentMapper.toComment(newCommentDto, author, event);
        Comment savedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(savedComment);
    }

    @Override
    @Transactional
    public CommentDto updateCommentPrivate(Long userId, Long commentId, UpdateCommentDto updateCommentDto) {
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Комментарий не найден"));
        comment.setText(updateCommentDto.getText());
        Comment updatedComment = commentRepository.save(comment);
        return CommentMapper.toCommentDto(updatedComment);
    }

    @Override
    public CommentDto updateCommentAdmin(long commentId, UpdateCommentDto updateCommentDto) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Комментарий не найден"));
        comment.setText(updateCommentDto.getText());
        return CommentMapper.toCommentDto(commentRepository.save(comment));
    }

    @Override
    @Transactional(readOnly = true)
    public CommentDto getByIdPrivate(long userId, long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий не найден"));
        if (userId != comment.getUser().getId()) {
            throw new IllegalArgumentException("Вы пытаетесь получить чужой комментарий");
        }
        return CommentMapper.toCommentDto(comment);
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllByUserId(long userId) {
        List<Comment> comments = commentRepository.getAllByUserId(userId);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllByEventId(long eventId) {
        List<Comment> comments = commentRepository.getAllByEventId(eventId);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllCommentPrivate(long userId, int from, int size) {
        Sort sort = Sort.sort(Comment.class).by(Comment::getCreated).descending();
        Pageable pageable = PageRequest.of(from / size, size, sort);
        List<Comment> comments = commentRepository.findAllByUserId(userId, pageable);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CommentDto> getAllCommentAdmin(Long[] users, Long[] events, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        LocalDateTime startDate = (rangeStart != null) ? rangeStart : LocalDateTime.MIN;
        LocalDateTime endDate = (rangeEnd != null) ? rangeEnd : LocalDateTime.MAX;
        if (startDate.isAfter(endDate)) {
            throw new IllegalArgumentException("Дата начала не может быть позже даты окончания");
        }
        Sort sort = Sort.sort(Comment.class).by(Comment::getCreated).descending();
        Pageable pageable = PageRequest.of(from / size, size, sort);
        List<Comment> comments = commentRepository.findAllByUsersAndEvents(users, events, startDate, endDate, pageable);
        return comments.stream()
                .map(CommentMapper::toCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional
    public void deleteComment(long userId, long commentId) {
        log.info("Попытка удаления комментария с ID {} пользователем с ID {}", commentId, userId);
        Comment comment = commentRepository.findByIdAndUserId(commentId, userId)
                .orElseThrow(() -> new IllegalArgumentException("Комментарий не найден или у вас нет прав на его удаление"));
        commentRepository.deleteById(commentId);
    }

    @Override
    @Transactional
    public void deleteCommentAdmin(long commentId) {
        log.info("Администратор пытается удалить комментарий с ID {}", commentId);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new IllegalArgumentException("Комментарий не найден"));
        commentRepository.deleteById(commentId);
    }
}
