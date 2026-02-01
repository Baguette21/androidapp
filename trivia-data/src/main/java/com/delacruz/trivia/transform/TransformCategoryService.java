package com.delacruz.trivia.transform;

import com.delacruz.trivia.entity.CategoryData;
import com.delacruz.trivia.model.Category;

public interface TransformCategoryService {
    Category transform(CategoryData categoryData);
    CategoryData transform(Category category);
}
