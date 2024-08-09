package ru.practicum.explorewithme.dto;

import lombok.*;

import javax.validation.constraints.NotNull;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StatResponseDto {
    @NotNull
    private String app;
    @NotNull
    private String uri;
    @NotNull
    private Long hits;
}
