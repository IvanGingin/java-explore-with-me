package ru.practicum.explorewithme.event.mapper;

import lombok.experimental.UtilityClass;
import ru.practicum.explorewithme.event.dto.LocationDto;
import ru.practicum.explorewithme.event.model.Location;

@UtilityClass
public class LocationMapper {

    public static LocationDto toDto(Location location) {
        return new LocationDto(
                location.getLat(),
                location.getLon()
        );
    }

    public static Location toModel(LocationDto locationDto) {
        return new Location(
                null,
                locationDto.getLat(),
                locationDto.getLon()
        );
    }
}
