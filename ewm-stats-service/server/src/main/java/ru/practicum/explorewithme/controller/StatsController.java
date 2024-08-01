package ru.practicum.explorewithme.controller;

import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.explorewithme.dto.StatDto;
import ru.practicum.explorewithme.dto.StatResponseDto;
import ru.practicum.explorewithme.service.StatService;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequiredArgsConstructor
@Validated
public class StatsController {

    private final StatService statService;

    @PostMapping("/hit")
    public ResponseEntity<StatDto> postStat(@RequestBody @Valid StatDto statDto) {
        StatDto savedStat = statService.postStat(statDto);
        return ResponseEntity.status(201).body(savedStat);
    }

    @GetMapping("/stats")
    public ResponseEntity<List<StatResponseDto>> getStat(
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime start,
            @RequestParam @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss") LocalDateTime end,
            @RequestParam(required = false) List<String> uris,
            @RequestParam(defaultValue = "false") boolean unique) {
        List<StatResponseDto> stats = statService.getStat(start, end, uris, unique);
        return ResponseEntity.ok(stats);
    }
}