package ru.practicum.service.compilation;

import ru.practicum.model.compilation.dtos.CompilationDto;
import ru.practicum.model.compilation.dtos.NewCompilationDto;
import ru.practicum.model.compilation.dtos.UpdateCompilationRequest;

import java.util.List;

public interface CompilationService {
    CompilationDto createCompilation(NewCompilationDto newCompilationDto);

    void deleteCompilation(Long compId);

    CompilationDto changeCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest);

    CompilationDto getCompilationById(Long compId);

    List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size);
}
