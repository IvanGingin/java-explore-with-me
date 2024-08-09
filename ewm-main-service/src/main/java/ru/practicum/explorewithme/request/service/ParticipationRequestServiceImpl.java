package ru.practicum.explorewithme.request.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.event.model.State;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.ForbiddenException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.request.mapper.ParticipationRequestMapper;
import ru.practicum.explorewithme.request.model.ParticipationRequest;
import ru.practicum.explorewithme.request.model.RequestStatus;
import ru.practicum.explorewithme.request.repository.ParticipationRequestRepository;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ParticipationRequestServiceImpl implements ParticipationRequestService {

    private final ParticipationRequestRepository participationRequestRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    @Transactional
    public ParticipationRequestDto addRequest(Long userId, Long eventId) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id=" + eventId + " не найдено"));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id=" + userId + " не найден"));

        if (participationRequestRepository.existsByRequesterIdAndEventId(userId, eventId)) {
            throw new ConflictException("Запрос уже существует.");
        }
        if (userId.equals(event.getInitiator().getId())) {
            throw new ConflictException("Инициатор не может отправить запрос на своё собственное событие.");
        }
        if (!event.getState().equals(State.PUBLISHED)) {
            throw new ConflictException("Участие возможно только в опубликованном событии.");
        }
        if (event.getParticipantLimit() != 0 &&
                event.getParticipantLimit() <= participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED)) {
            throw new ConflictException("Достигнут лимит участников.");
        }
        ParticipationRequest request = new ParticipationRequest();
        request.setCreated(LocalDateTime.now());
        request.setEvent(event);
        request.setRequester(user);
        if (event.getRequestModeration() && event.getParticipantLimit() != 0) {
            request.setStatus(RequestStatus.PENDING);
        } else {
            request.setStatus(RequestStatus.CONFIRMED);
        }
        ParticipationRequest savedRequest = participationRequestRepository.save(request);
        return ParticipationRequestMapper.toParticipationRequestDto(savedRequest);
    }

    @Override
    @Transactional
    public ParticipationRequestDto cancelRequest(Long userId, Long requestId) {
        log.debug("Получен запрос на отмену участия пользователя с id={} для запроса с id={}", userId, requestId);
        ParticipationRequest request = participationRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("Запрос с id={} не найден", requestId);
                    return new NotFoundException("Запрос с id=" + requestId + " не найден");
                });
        if (!request.getRequester().getId().equals(userId)) {
            log.error("Пользователь с id={} не имеет прав на отмену запроса с id={}", userId, requestId);
            throw new ForbiddenException("Пользователь может отменить только свои собственные запросы");
        }
        if (request.getStatus() == RequestStatus.CONFIRMED) {
            log.error("Подтвержденные запросы не могут быть отменены");
            throw new ConflictException("Подтвержденные запросы не могут быть отменены");
        }
        request.setStatus(RequestStatus.CANCELED);
        ParticipationRequest canceledRequest = participationRequestRepository.save(request);
        log.debug("Запрос с id={} успешно отменен пользователем с id={}", requestId, userId);
        return ParticipationRequestMapper.toParticipationRequestDto(canceledRequest);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getRequestsByUserId(Long userId) {
        return participationRequestRepository.findAllByRequesterId(userId).stream()
                .map(ParticipationRequestMapper::toParticipationRequestDto)
                .collect(Collectors.toList());
    }
}
