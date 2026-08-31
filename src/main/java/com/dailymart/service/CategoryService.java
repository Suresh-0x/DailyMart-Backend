package com.dailymart.service;

import com.dailymart.dto.*;
import com.dailymart.entity.Category;
import com.dailymart.exception.ResourceNotFoundException;
import com.dailymart.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<CategoryDto> getAllCategories() {
        return categoryRepository.findByActiveTrueOrderByDisplayOrderAsc()
            .stream().map(this::toDto).toList();
    }

    public List<CategoryDto> getRootCategories() {
        return categoryRepository.findByParentIsNullAndActiveTrueOrderByDisplayOrderAsc()
            .stream().map(this::toDto).toList();
    }

    @Transactional
    public CategoryDto createCategory(CreateCategoryRequest req) {
        String slug = req.getName().toLowerCase()
            .replaceAll("[^a-z0-9\\s]", "")
            .replaceAll("\\s+", "-")
            + "-" + System.currentTimeMillis();

        Category category = Category.builder()
            .name(req.getName())
            .slug(slug)
            .description(req.getDescription())
            .imageUrl(req.getImageUrl())
            .displayOrder(req.getDisplayOrder())
            .active(true)
            .build();

        if (req.getParentId() != null) {
            Category parent = categoryRepository.findById(req.getParentId())
                .orElseThrow(() -> new ResourceNotFoundException("Parent category not found"));
            category.setParent(parent);
        }
        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public CategoryDto updateCategory(Long id, CreateCategoryRequest req) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        if (req.getName() != null)        category.setName(req.getName());
        if (req.getDescription() != null) category.setDescription(req.getDescription());
        if (req.getImageUrl() != null)    category.setImageUrl(req.getImageUrl());
        return toDto(categoryRepository.save(category));
    }

    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found"));
        category.setActive(false);
        categoryRepository.save(category);
    }

    public CategoryDto getCategoryById(Long id) {
        return toDto(categoryRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("Category not found with id: " + id)));
    }

    private CategoryDto toDto(Category c) {
        return CategoryDto.builder()
            .id(c.getId())
            .name(c.getName())
            .slug(c.getSlug())
            .description(c.getDescription())
            .imageUrl(c.getImageUrl())
            .parentId(c.getParent() != null ? c.getParent().getId() : null)
            .parentName(c.getParent() != null ? c.getParent().getName() : null)
            .isActive(c.isActive())
            .displayOrder(c.getDisplayOrder())
            .productCount(c.getProducts() != null ? c.getProducts().size() : 0)
            .children(c.getChildren() != null
                ? c.getChildren().stream().filter(Category::isActive).map(this::toDto).toList()
                : java.util.Collections.emptyList())
            .build();
    }
}
