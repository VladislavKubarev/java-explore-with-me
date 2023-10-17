package ru.practicum.mapper.category;

import ru.practicum.model.category.Category;
import ru.practicum.model.category.dtos.CategoryDto;
import ru.practicum.model.category.dtos.NewCategoryDto;

public class CategoryMapper {
    public static Category mapToCategory(NewCategoryDto newCategoryDto) {
        Category category = new Category();
        category.setName(newCategoryDto.getName());

        return category;
    }

    public static CategoryDto mapToCategoryDto(Category category) {
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setName(category.getName());

        return categoryDto;
    }
}
