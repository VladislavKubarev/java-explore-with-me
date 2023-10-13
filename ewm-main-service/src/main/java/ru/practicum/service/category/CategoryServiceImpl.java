package ru.practicum.service.category;

import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exception.DataConflictException;
import ru.practicum.exception.DataNotFoundException;
import ru.practicum.mapper.category.CategoryMapper;
import ru.practicum.model.category.Category;
import ru.practicum.model.category.dtos.CategoryDto;
import ru.practicum.model.category.dtos.NewCategoryDto;
import ru.practicum.model.event.Event;
import ru.practicum.repository.category.CategoryRepository;
import ru.practicum.repository.event.EventRepository;

import java.util.List;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    public CategoryDto addCategory(NewCategoryDto newCategoryDto) {
        Category category = CategoryMapper.mapToCategory(newCategoryDto);

        try {
            return CategoryMapper.mapToCategoryDto(categoryRepository.save(category));
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException(String.format("Category with name %s already exists", newCategoryDto.getName()));
        }
    }

    @Override
    public void deleteCategory(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new DataNotFoundException(String.format("Category with id %d not found", catId)));

        List<Event> eventsWithThisCategory = eventRepository.findAllByCategoryId(catId);
        if (!eventsWithThisCategory.isEmpty()) {
            throw new DataConflictException(String.format("The category with id %d is not empty", catId));
        }
        categoryRepository.deleteById(catId);
    }

    @Override
    public CategoryDto changeCategory(NewCategoryDto newCategoryDto, Long catId) {
        Category updatedCategory = categoryRepository.findById(catId).orElseThrow(
                () -> new DataNotFoundException(String.format("Category with id %d not found", catId)));

        updatedCategory.setName(newCategoryDto.getName());

        try {
            return CategoryMapper.mapToCategoryDto(categoryRepository.save(updatedCategory));
        } catch (DataIntegrityViolationException e) {
            throw new DataConflictException(String.format("Category with name %s already exists", newCategoryDto.getName()));
        }
    }

    @Override
    public List<CategoryDto> getAllCategories(Integer from, Integer size) {
        Pageable pageable = PageRequest.of(from / size, size);

        return categoryRepository.findAll(pageable).stream().map(CategoryMapper::mapToCategoryDto).collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long catId) {
        Category category = categoryRepository.findById(catId).orElseThrow(
                () -> new DataNotFoundException(String.format("Category with id %d not found", catId)));

        return CategoryMapper.mapToCategoryDto(category);
    }
}
