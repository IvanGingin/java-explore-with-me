package ru.practicum.explorewithme.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.category.mapper.CategoryMapper;
import ru.practicum.explorewithme.event.dto.*;
import ru.practicum.explorewithme.event.model.Event;
import ru.practicum.explorewithme.user.mapper.UserMapper;

import java.time.format.DateTimeFormatter;

@UtilityClass
public class EventMapper {
    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public EventFullDto toEventFullDto(Event event) {
        return new EventFullDto(
                event.getId(),
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                event.getConfirmedRequests() == null ? 0L : event.getConfirmedRequests(),
                event.getCreatedOn().format(formatter),
                event.getDescription(),
                event.getEventDate().format(formatter),
                UserMapper.toUserShortDto(event.getInitiator()),
                LocationMapper.toDto(event.getLocation()),
                event.getPaid(),
                event.getParticipantLimit(),
                event.getPublishedOn() != null ? event.getPublishedOn().format(formatter) : null,
                event.getRequestModeration(),
                event.getState().toString(),
                event.getTitle(),
                event.getViews() == null ? 0L : event.getViews()
        );
    }

    public EventShortDto toEventShortDto(Event event) {
        return new EventShortDto(
                event.getId(),
                event.getTitle(),
                event.getDescription(),
                event.getAnnotation(),
                CategoryMapper.toCategoryDto(event.getCategory()),
                UserMapper.toUserShortDto(event.getInitiator()),
                event.getEventDate(),
                event.getConfirmedRequests() == null ? 0L : event.getConfirmedRequests(),
                event.getPaid(),
                event.getViews() == null ? 0L : event.getViews() // Обработка null
        );
    }

    public Event toModel(NewEventDto newEventDto) {
        return Event.builder()
                .annotation(newEventDto.getAnnotation())
                .description(newEventDto.getDescription())
                .category(null)
                .eventDate(newEventDto.getEventDate())
                .location(LocationMapper.toModel(newEventDto.getLocation()))
                .paid(newEventDto.getPaid())
                .participantLimit(newEventDto.getParticipantLimit())
                .requestModeration(newEventDto.getRequestModeration())
                .title(newEventDto.getTitle())
                .views(0L) // Установка значения по умолчанию
                .build();
    }
}
