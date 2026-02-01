package com.delacruz.trivia.service;

import com.delacruz.trivia.model.Category;

import java.util.List;

public interface CategoryService {
    List<Category> getAllActiveCategories();
    Category getCategoryById(Long id);
}
