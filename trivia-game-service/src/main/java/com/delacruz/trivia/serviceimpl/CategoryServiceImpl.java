package com.delacruz.trivia.serviceimpl;

import com.delacruz.trivia.entity.CategoryData;
import com.delacruz.trivia.model.Category;
import com.delacruz.trivia.repository.CategoryRepository;
import com.delacruz.trivia.service.CategoryService;
import com.delacruz.trivia.transform.TransformCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private TransformCategoryService transformCategoryService;

    @Override
    public List<Category> getAllActiveCategories() {
        return categoryRepository.findByIsActiveTrue()
                .stream()
                .map(transformCategoryService::transform)
                .collect(Collectors.toList());
    }

    @Override
    public Category getCategoryById(Long id) {
        CategoryData categoryData = categoryRepository.findById(id).orElse(null);
        return transformCategoryService.transform(categoryData);
    }
}
