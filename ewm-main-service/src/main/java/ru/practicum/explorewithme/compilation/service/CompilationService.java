package ru.practicum.explorewithme.compilation.service;

import ru.practicum.explorewithme.compilation.dto.CompilationDto;
import ru.practicum.explorewithme.compilation.dto.NewCompilationDto;
import ru.practicum.explorewithme.compilation.dto.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {

    List<CompilationDto> getCompilations(Boolean pinned, int from, int size);

    CompilationDto getCompilationById(Long compId);

    CompilationDto addCompilation(NewCompilationDto compilationDto);

    CompilationDto updateCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest);

    void deleteCompilation(Long compId);
}