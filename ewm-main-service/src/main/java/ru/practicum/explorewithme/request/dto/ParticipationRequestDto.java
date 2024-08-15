package ru.practicum.explorewithme.request.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.practicum.explorewithme.request.model.RequestStatus;

import java.time.LocalDateTime;

import static ru.practicum.explorewithme.util.Constants.DATE_FORMAT;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ParticipationRequestDto {
    private Long id;

    @JsonFormat(pattern = DATE_FORMAT)
    private LocalDateTime created;

    private Long event;

    private Long requester;

    private RequestStatus status;
}

