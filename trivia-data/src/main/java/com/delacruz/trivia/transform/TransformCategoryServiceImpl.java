package com.delacruz.trivia.transform;

import com.delacruz.trivia.entity.CategoryData;
import com.delacruz.trivia.model.Category;
import com.delacruz.trivia.repository.QuestionRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class TransformCategoryServiceImpl implements TransformCategoryService {

    @Autowired
    private QuestionRepository questionRepository;

    @Override
    public Category transform(CategoryData categoryData) {
        if (categoryData == null) return null;

        Category category = new Category();
        category.setId(categoryData.getId());
        category.setName(categoryData.getName());
        category.setDescription(categoryData.getDescription());
        category.setIsActive(categoryData.getIsActive());
        category.setCreatedAt(categoryData.getCreatedAt());
        category.setQuestionCount(questionRepository.countByCategoryId(categoryData.getId()));

        return category;
    }

    @Override
    public CategoryData transform(Category category) {
        if (category == null) return null;

        CategoryData categoryData = new CategoryData();
        categoryData.setId(category.getId());
        categoryData.setName(category.getName());
        categoryData.setDescription(category.getDescription());
        categoryData.setIsActive(category.getIsActive());

        return categoryData;
    }
}
