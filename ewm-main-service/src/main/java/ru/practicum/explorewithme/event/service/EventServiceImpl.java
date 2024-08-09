package ru.practicum.explorewithme.event.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.category.repository.CategoryRepository;
import ru.practicum.explorewithme.event.dto.*;
import ru.practicum.explorewithme.event.mapper.EventMapper;
import ru.practicum.explorewithme.event.mapper.LocationMapper;
import ru.practicum.explorewithme.event.model.*;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.event.repository.LocationRepository;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.ForbiddenException;
import ru.practicum.explorewithme.exception.NotFoundException;
import ru.practicum.explorewithme.client.StatsClient;
import ru.practicum.explorewithme.dto.StatDto;
import ru.practicum.explorewithme.dto.StatResponseDto;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateRequest;
import ru.practicum.explorewithme.request.dto.EventRequestStatusUpdateResult;
import ru.practicum.explorewithme.request.dto.ParticipationRequestDto;
import ru.practicum.explorewithme.request.mapper.ParticipationRequestMapper;
import ru.practicum.explorewithme.request.model.ParticipationRequest;
import ru.practicum.explorewithme.request.model.RequestStatus;
import ru.practicum.explorewithme.request.repository.ParticipationRequestRepository;
import ru.practicum.explorewithme.user.model.User;
import ru.practicum.explorewithme.user.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static ru.practicum.explorewithme.event.model.State.PENDING;

@Service
@RequiredArgsConstructor
@Slf4j
public class EventServiceImpl implements EventService {

    private final UserRepository userRepository;
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;
    private final LocationRepository locationRepository;
    private final ParticipationRequestRepository participationRequestRepository;
    private final StatsClient statsClient;
    private final HashMap<Long, HashSet<String>> eventViews = new HashMap<>();

    @Override
    @Transactional
    public EventFullDto addEvent(Long userId, NewEventDto eventNewDto) {
        validateEventDate(eventNewDto.getEventDate());
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь не найден"));
        Category category = categoryRepository.findById(eventNewDto.getCategory())
                .orElseThrow(() -> new NotFoundException("Категория не найдена"));
        Event event = EventMapper.toModel(eventNewDto);
        event.setInitiator(user);
        event.setCategory(category);
        event.setCreatedOn(LocalDateTime.now());
        event.setState(PENDING);
        event.setLocation(locationRepository.save(event.getLocation()));
        Event savedEvent = eventRepository.save(event);
        log.info("Событие успешно создано: {}", savedEvent);
        return EventMapper.toEventFullDto(savedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> searchPublicEvents(String text, List<Long> categories, Boolean paid, LocalDateTime rangeStart, LocalDateTime rangeEnd, Boolean onlyAvailable, String sort, int from, int size) {
        validateDateRange(rangeStart, rangeEnd);
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.searchPublicEvents(text, categories, paid, rangeStart, rangeEnd, pageable)
                .stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventFullDto getPublishedEventById(Long eventId, HttpServletRequest request) {
        Event event = eventRepository.findByIdAndState(eventId, State.PUBLISHED)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        String ip = request.getRemoteAddr();
        boolean isUniqueView = eventViews
                .computeIfAbsent(eventId, k -> new HashSet<>())
                .add(ip);
        if (isUniqueView) {
            if (event.getViews() == null) {
                event.setViews(0L);
            }
            event.setViews(event.getViews() + 1);
            eventRepository.save(event);
        }
        try {
            StatDto statDto = new StatDto(null, "ewm-main-service", "/events/" + eventId, ip, LocalDateTime.now());
            statsClient.saveStats(statDto);
        } catch (Exception e) {
            log.error("Failed to send stats: {}", e.getMessage());
        }
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEventAdmin(Long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (updateEventAdminRequest.getStateAction() != null) {
            updateEventStateAdmin(event, StateAdmin.valueOf(updateEventAdminRequest.getStateAction()));
        }
        updateEventAdminFields(event, updateEventAdminRequest);
        Event updatedEvent = eventRepository.save(event);
        log.info("Событие с id: {} успешно обновлено администратором", eventId);
        return EventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventFullDto> searchAdminEvents(List<Long> users, List<State> states, List<Long> categories, LocalDateTime rangeStart, LocalDateTime rangeEnd, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.searchEvents(users, states, categories, rangeStart, rangeEnd, pageable)
                .stream().map(EventMapper::toEventFullDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<EventShortDto> getEventsByUserId(Long userId, int from, int size) {
        Pageable pageable = PageRequest.of(from / size, size);
        return eventRepository.findByInitiatorId(userId, pageable)
                .stream().map(EventMapper::toEventShortDto).collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public EventFullDto getEventByUserIdAndEventId(Long userId, Long eventId) {
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        return EventMapper.toEventFullDto(event);
    }

    @Override
    @Transactional
    public EventFullDto updateEvent(Long userId, Long eventId, UpdateEventUserRequest updateEventUserRequest) {
        validateEventDate(updateEventUserRequest.getEventDate());
        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        if (event.getState() == State.PUBLISHED) {
            throw new ConflictException("Только ожидающие или отмененные события можно изменить");
        }
        updateEventUserFields(event, updateEventUserRequest);
        if (updateEventUserRequest.getParticipantLimit() != null && updateEventUserRequest.getParticipantLimit() < 0) {
            throw new IllegalArgumentException("Лимит участников не может быть отрицательным");
        }
        if (updateEventUserRequest.getStateAction() != null) {
            updateEventStatePrivate(event, StatePrivate.valueOf(updateEventUserRequest.getStateAction()));
        }
        Event updatedEvent = eventRepository.save(event);
        log.info("Событие с id: {} успешно обновлено для пользователя id: {}", eventId, userId);
        return EventMapper.toEventFullDto(updatedEvent);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ParticipationRequestDto> getEventRequests(Long userId, Long eventId) {
        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));
        return participationRequestRepository.findAllByEventId(eventId)
                .stream().map(ParticipationRequestMapper::toParticipationRequestDto).collect(Collectors.toList());
    }

    @Override
    @Transactional
    public EventRequestStatusUpdateResult updateStatusRequestsForEvent(Long userId, Long eventId, EventRequestStatusUpdateRequest requestDto) {
        log.info("Получен запрос на обновление статусов запросов для события с id: {} пользователем с id: {}", eventId, userId);

        Event event = eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие не найдено"));

        if (event.getConfirmedRequests() == null) {
            event.setConfirmedRequests(0L);
        }

        if (!event.getInitiator().getId().equals(userId)) {
            throw new ForbiddenException("Только инициатор может обновить статусы запросов");
        }

        List<ParticipationRequest> requests = participationRequestRepository.findAllById(requestDto.getRequestIds());
        List<ParticipationRequestDto> confirmedRequests = new ArrayList<>();
        List<ParticipationRequestDto> rejectedRequests = new ArrayList<>();

        log.info("Всего найдено запросов на участие: {}", requests.size());

        if (requestDto.getStatus() == RequestStatus.CONFIRMED) {
            long confirmedCount = participationRequestRepository.countByEventIdAndStatus(eventId, RequestStatus.CONFIRMED);
            if (event.getParticipantLimit() > 0 && confirmedCount + requestDto.getRequestIds().size() > event.getParticipantLimit()) {
                throw new ConflictException("Лимит участников достигнут.");
            }
        }

        for (ParticipationRequest request : requests) {
            if (request.getStatus() == RequestStatus.CONFIRMED && requestDto.getStatus() == RequestStatus.REJECTED) {
                throw new ConflictException("Подтвержденные запросы не могут быть отменены");
            }
            request.setStatus(requestDto.getStatus());
            if (requestDto.getStatus() == RequestStatus.CONFIRMED) {
                confirmedRequests.add(ParticipationRequestMapper.toParticipationRequestDto(request));
                event.setConfirmedRequests(event.getConfirmedRequests() + 1);
            } else if (requestDto.getStatus() == RequestStatus.REJECTED) {
                rejectedRequests.add(ParticipationRequestMapper.toParticipationRequestDto(request));
            }
        }

        participationRequestRepository.saveAll(requests);
        eventRepository.save(event);

        log.info("Запросы для события id={} обновлены пользователем id={}", eventId, userId);
        return new EventRequestStatusUpdateResult(confirmedRequests, rejectedRequests);
    }


    private void validateEventDate(LocalDateTime eventDate) {
        if (eventDate != null && eventDate.isBefore(LocalDateTime.now().plusHours(2))) {
            throw new ConflictException("Дата события должна быть минимум через " + 2 + " часа");
        }
    }

    private void validateDateRange(LocalDateTime rangeStart, LocalDateTime rangeEnd) {
        if (rangeStart != null && rangeEnd != null && rangeEnd.isBefore(rangeStart)) {
            throw new IllegalArgumentException("rangeEnd должен быть позже rangeStart");
        }
    }

    private void updateEventAdminFields(Event event, UpdateEventAdminRequest request) {
        if (request.getAnnotation() != null && !request.getAnnotation().isBlank()) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена")));
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(locationRepository.save(LocationMapper.toModel(request.getLocation())));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            event.setTitle(request.getTitle());
        }
    }

    private void updateEventUserFields(Event event, UpdateEventUserRequest request) {
        if (request.getAnnotation() != null && !request.getAnnotation().isBlank()) {
            event.setAnnotation(request.getAnnotation());
        }
        if (request.getCategory() != null) {
            event.setCategory(categoryRepository.findById(request.getCategory())
                    .orElseThrow(() -> new NotFoundException("Категория не найдена")));
        }
        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            event.setDescription(request.getDescription());
        }
        if (request.getLocation() != null) {
            event.setLocation(locationRepository.save(LocationMapper.toModel(request.getLocation())));
        }
        if (request.getPaid() != null) {
            event.setPaid(request.getPaid());
        }
        if (request.getParticipantLimit() != null) {
            event.setParticipantLimit(request.getParticipantLimit());
        }
        if (request.getRequestModeration() != null) {
            event.setRequestModeration(request.getRequestModeration());
        }
        if (request.getTitle() != null && !request.getTitle().isBlank()) {
            event.setTitle(request.getTitle());
        }
    }

    private void updateEventStateAdmin(Event event, StateAdmin stateAction) {
        switch (stateAction) {
            case PUBLISH_EVENT:
                publishEvent(event);
                break;
            case REJECT_EVENT:
                rejectEvent(event);
                break;
            default:
                throw new ConflictException("Неверное действие с состоянием");
        }
    }

    private void updateEventStatePrivate(Event event, StatePrivate stateAction) {
        switch (stateAction) {
            case SEND_TO_REVIEW:
                sendToReview(event);
                break;
            case CANCEL_REVIEW:
                cancelReview(event);
                break;
            default:
                throw new ConflictException("Неверное действие с состоянием");
        }
    }

    private void publishEvent(Event event) {
        if (!event.getState().equals(State.PENDING)) {
            throw new ConflictException("Событие можно публиковать только в состоянии ожидания");
        }
        event.setState(State.PUBLISHED);
        event.setPublishedOn(LocalDateTime.now());
    }

    private void rejectEvent(Event event) {
        if (!event.getState().equals(State.PENDING)) {
            throw new ConflictException("Событие можно отклонить только в состоянии ожидания");
        }
        event.setState(State.CANCELED);
    }

    private void sendToReview(Event event) {
        if (!event.getState().equals(State.CANCELED)) {
            throw new ConflictException("Только отмененные события можно отправить на рассмотрение");
        }
        event.setState(State.PENDING);
    }

    private void cancelReview(Event event) {
        if (!event.getState().equals(State.PENDING)) {
            throw new ConflictException("Только ожидающие события можно отменить");
        }
        event.setState(State.CANCELED);
    }

    private long getEventViews(Long eventId) {
        List<StatResponseDto> stats = statsClient.getStats(LocalDateTime.MIN, LocalDateTime.now(), List.of("/events/" + eventId), true);
        return stats.stream().mapToLong(StatResponseDto::getHits).sum();
    }

}
