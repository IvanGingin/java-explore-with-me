package ru.practicum.explorewithme.dto;

import lombok.Data;

@Data
public class ViewStats {
    private String app;
    private String uri;
    private long hits;
}
