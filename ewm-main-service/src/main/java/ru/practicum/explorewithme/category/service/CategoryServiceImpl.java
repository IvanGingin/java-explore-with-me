package ru.practicum.explorewithme.category.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.explorewithme.category.dto.CategoryDto;
import ru.practicum.explorewithme.category.mapper.CategoryMapper;
import ru.practicum.explorewithme.category.model.Category;
import ru.practicum.explorewithme.category.repository.CategoryRepository;
import ru.practicum.explorewithme.event.repository.EventRepository;
import ru.practicum.explorewithme.exception.ConflictException;
import ru.practicum.explorewithme.exception.NotFoundException;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j

public class CategoryServiceImpl implements CategoryService {
    private final CategoryRepository categoryRepository;
    private final EventRepository eventRepository;

    @Override
    @Transactional
    public CategoryDto addCategory(CategoryDto categoryDto) {
        if (categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Категория с таким именем уже существует");
        }
        Category category = CategoryMapper.toModel(categoryDto);
        Category savedCategory = categoryRepository.save(category);
        return CategoryMapper.toCategoryDto(savedCategory);
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        log.debug("Получение всех категорий с параметрами from={} и size={}", from, size);
        PageRequest pageRequest = PageRequest.of(from, size);
        List<Category> categories = categoryRepository.findAll(pageRequest).getContent();
        return categories.stream()
                .map(CategoryMapper::toCategoryDto)
                .collect(Collectors.toList());
    }

    @Override
    public CategoryDto getCategoryById(Long categoryId) {
        Optional<Category> category = categoryRepository.findById(categoryId);
        if (category.isPresent()) {
            return CategoryMapper.toCategoryDto(category.get());
        } else {
            throw new NotFoundException("Категория не найдена");
        }
    }

    @Override
    @Transactional
    public void deleteCategory(Long categoryId) {
        if (eventRepository.existsByCategoryId(categoryId)) {
            throw new ConflictException("Невозможно удалить категорию, так как она привязана к событиям");
        }
        if (!categoryRepository.existsById(categoryId)) {
            throw new NotFoundException("Категория не найдена");
        }
        categoryRepository.deleteById(categoryId);
        log.debug("Категория с id={} успешно удалена", categoryId);
    }

    @Override
    @Transactional
    public CategoryDto updateCategory(Long categoryId, CategoryDto categoryDto) {
        Optional<Category> existingCategory = categoryRepository.findById(categoryId);
        if (!existingCategory.isPresent()) {
            log.error("Категория с id={} не найдена для обновления", categoryId);
            throw new NotFoundException("Категория не найдена");
        }
        Category category = existingCategory.get();
        if (!category.getName().equals(categoryDto.getName()) && categoryRepository.existsByName(categoryDto.getName())) {
            throw new ConflictException("Категория с таким именем уже существует");
        }
        category.setName(categoryDto.getName());
        Category updatedCategory = categoryRepository.save(category);
        log.debug("Категория с id={} успешно обновлена", categoryId);
        return CategoryMapper.toCategoryDto(updatedCategory);
    }
}
