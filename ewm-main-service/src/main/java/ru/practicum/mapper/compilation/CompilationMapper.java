package ru.practicum.mapper.compilation;

import ru.practicum.mapper.event.EventMapper;
import ru.practicum.model.compilation.Compilation;
import ru.practicum.model.compilation.dtos.CompilationDto;
import ru.practicum.model.compilation.dtos.NewCompilationDto;

import java.util.stream.Collectors;

public class CompilationMapper {
    public static Compilation mapToCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = new Compilation();
        compilation.setTitle(newCompilationDto.getTitle());
        compilation.setPinned(newCompilationDto.getPinned());

        return compilation;
    }

    public static CompilationDto mapToCompilationDto(Compilation compilation) {
        CompilationDto compilationDto = new CompilationDto();
        compilationDto.setId(compilation.getId());
        compilationDto.setEvents(compilation.getEvents().stream().map(EventMapper::mapToEventShortDto).collect(Collectors.toList()));
        compilationDto.setTitle(compilation.getTitle());
        compilationDto.setPinned(compilation.getPinned());

        return compilationDto;
    }
}
