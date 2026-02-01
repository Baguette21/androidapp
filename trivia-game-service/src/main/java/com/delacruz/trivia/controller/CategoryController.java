package com.delacruz.trivia.controller;

import com.delacruz.trivia.model.Category;
import com.delacruz.trivia.model.Question;
import com.delacruz.trivia.service.CategoryService;
import com.delacruz.trivia.service.QuestionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/categories")
public class CategoryController {

    private static final Logger logger = LoggerFactory.getLogger(CategoryController.class);

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private QuestionService questionService;

    @GetMapping
    public ResponseEntity<?> getAllCategories() {
        try {
            List<Category> categories = categoryService.getAllActiveCategories();
            return ResponseEntity.ok(categories);
        } catch (Exception e) {
            logger.error("Failed to get categories", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{categoryId}")
    public ResponseEntity<?> getCategory(@PathVariable Long categoryId) {
        try {
            Category category = categoryService.getCategoryById(categoryId);
            if (category == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(category);
        } catch (Exception e) {
            logger.error("Failed to get category: {}", categoryId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/{categoryId}/questions")
    public ResponseEntity<?> getQuestionsForCategory(@PathVariable Long categoryId,
                                                      @RequestParam(defaultValue = "10") Integer limit) {
        try {
            List<Question> questions = questionService.getQuestionsForCategory(categoryId, limit);
            return ResponseEntity.ok(questions);
        } catch (Exception e) {
            logger.error("Failed to get questions for category: {}", categoryId, e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", e.getMessage()));
        }
    }
}
