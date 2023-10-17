package ru.practicum.service.category;

import ru.practicum.model.category.dtos.CategoryDto;
import ru.practicum.model.category.dtos.NewCategoryDto;

import java.util.List;

public interface CategoryService {
    CategoryDto addCategory(NewCategoryDto newCategoryDto);

    void deleteCategory(Long catId);

    CategoryDto changeCategory(NewCategoryDto newCategoryDto, Long catId);

    List<CategoryDto> getAllCategories(Integer from, Integer size);

    CategoryDto getCategoryById(Long catId);
}
