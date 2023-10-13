package ru.practicum.service.compilation;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.mapper.compilation.CompilationMapper;
import ru.practicum.model.compilation.Compilation;
import ru.practicum.model.compilation.dtos.CompilationDto;
import ru.practicum.model.compilation.dtos.NewCompilationDto;
import ru.practicum.model.compilation.dtos.UpdateCompilationRequest;
import ru.practicum.model.event.Event;
import ru.practicum.repository.compilation.CompilationRepository;
import ru.practicum.repository.event.EventRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompilationServiceImpl implements CompilationService {
    private final CompilationRepository compilationRepository;
    private final EventRepository eventRepository;

    @Override
    public CompilationDto createCompilation(NewCompilationDto newCompilationDto) {
        Compilation compilation = CompilationMapper.mapToCompilation(newCompilationDto);

        if (newCompilationDto.getPinned() == null) {
            compilation.setPinned(false);
        }
        if (newCompilationDto.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(newCompilationDto.getEvents());
            compilation.setEvents(events);
        } else {
            compilation.setEvents(new ArrayList<>());
        }

        return CompilationMapper.mapToCompilationDto(compilationRepository.save(compilation));
    }

    @Override
    public void deleteCompilation(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new DataNotFoundException(String.format("Compilation with id %d not found.", compId)));

        compilationRepository.deleteById(compId);
    }

    @Override
    public CompilationDto changeCompilation(Long compId, UpdateCompilationRequest updateCompilationRequest) {
        Compilation updatableCompilation = compilationRepository.findById(compId).orElseThrow(
                () -> new DataNotFoundException(String.format("Compilation with id %d not found.", compId)));

        if (updateCompilationRequest.getTitle() != null) {
            updatableCompilation.setTitle(updateCompilationRequest.getTitle());
        }
        if (updateCompilationRequest.getPinned() != null) {
            updatableCompilation.setPinned(updateCompilationRequest.getPinned());
        }
        if (updateCompilationRequest.getEvents() != null) {
            List<Event> events = eventRepository.findAllById(updateCompilationRequest.getEvents());
            updatableCompilation.setEvents(events);
        }

        return CompilationMapper.mapToCompilationDto(compilationRepository.save(updatableCompilation));
    }

    @Override
    public CompilationDto getCompilationById(Long compId) {
        Compilation compilation = compilationRepository.findById(compId).orElseThrow(
                () -> new DataNotFoundException(String.format("Compilation with id %d not found.", compId)));

        return CompilationMapper.mapToCompilationDto(compilation);
    }

    @Override
    public List<CompilationDto> getAllCompilations(Boolean pinned, Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);
        if (pinned != null) {
            return compilationRepository.findAllByPinned(pinned, pageable).stream()
                    .map(CompilationMapper::mapToCompilationDto).collect(Collectors.toList());
        } else {
            return compilationRepository.findAll(pageable).stream()
                    .map(CompilationMapper::mapToCompilationDto).collect(Collectors.toList());
        }
    }
}
