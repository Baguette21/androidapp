package com.delacruz.trivia.config;

import com.delacruz.trivia.entity.AnswerData;
import com.delacruz.trivia.entity.CategoryData;
import com.delacruz.trivia.entity.QuestionData;
import com.delacruz.trivia.repository.AnswerRepository;
import com.delacruz.trivia.repository.CategoryRepository;
import com.delacruz.trivia.repository.QuestionRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger logger = LoggerFactory.getLogger(DataSeeder.class);

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private QuestionRepository questionRepository;

    @Autowired
    private AnswerRepository answerRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        if (categoryRepository.count() == 0) {
            logger.info("No categories found. Seeding database...");
            seedData();
        } else {
            logger.info("Database already seeded. Skipping.");
        }
    }

    private void seedData() {
        createCategoryWithQuestions(
            "Pop Culture", 
            "Movies, TV shows, celebrities, and entertainment",
            List.of(
                createQuestion(
                    "Which movie won the Academy Award for Best Picture in 2020?",
                    1, 15,
                    List.of("1917", "Parasite", "Joker", "Once Upon a Time in Hollywood"),
                    1
                ),
                createQuestion(
                    "Who played Iron Man in the Marvel Cinematic Universe?",
                    2, 15,
                    List.of("Chris Evans", "Chris Hemsworth", "Robert Downey Jr.", "Mark Ruffalo"),
                    2
                ),
                createQuestion(
                    "Which TV series features a chemistry teacher turned drug lord?",
                    3, 15,
                    List.of("Breaking Bad", "Better Call Saul", "Ozark", "Narcos"),
                    0
                ),
                createQuestion(
                    "What is the name of the fictional continent in Game of Thrones?",
                    4, 15,
                    List.of("Middle-earth", "Narnia", "Azeroth", "Westeros"),
                    3
                ),
                createQuestion(
                    "Which artist released the album \"Thriller\"?",
                    5, 15,
                    List.of("Prince", "Michael Jackson", "Whitney Houston", "Madonna"),
                    1
                )
            )
        );

        createCategoryWithQuestions(
            "Science and Nature",
            "Physics, chemistry, biology, and the natural world",
            List.of(
                createQuestion(
                    "What is the chemical symbol for gold?",
                    1, 15,
                    List.of("Ag", "Go", "Au", "Gd"),
                    2
                ),
                createQuestion(
                    "How many planets are in our solar system?",
                    2, 15,
                    List.of("7", "8", "9", "10"),
                    1
                ),
                createQuestion(
                    "What is the largest organ in the human body?",
                    3, 15,
                    List.of("Skin", "Liver", "Heart", "Brain"),
                    0
                ),
                createQuestion(
                    "What gas do plants absorb from the atmosphere?",
                    4, 15,
                    List.of("Oxygen", "Nitrogen", "Carbon Dioxide", "Hydrogen"),
                    2
                ),
                createQuestion(
                    "What is the speed of light in vacuum (approximately)?",
                    5, 15,
                    List.of("300 km/s", "3,000 km/s", "30,000 km/s", "300,000 km/s"),
                    3
                )
            )
        );

        // Additional placeholder categories
        createCategory("Sports", "Athletic competitions, teams, and famous athletes");
        createCategory("Video Games", "Gaming history, characters, and franchises");
        createCategory("History", "World events, historical figures, and civilizations");
        createCategory("Geography", "Countries, capitals, landmarks, and world facts");

        logger.info("Database seeding completed successfully.");
    }

    private void createCategory(String name, String description) {
        CategoryData category = new CategoryData();
        category.setName(name);
        category.setDescription(description);
        categoryRepository.save(category);
    }

    private void createCategoryWithQuestions(String name, String description, List<QuestionHelper> questionHelpers) {
        CategoryData category = new CategoryData();
        category.setName(name);
        category.setDescription(description);
        category = categoryRepository.save(category);

        for (QuestionHelper qh : questionHelpers) {
            QuestionData question = new QuestionData();
            question.setCategory(category);
            question.setQuestionText(qh.text);
            question.setQuestionOrder(qh.order);
            question.setCorrectAnswerIndex(qh.correctIndex);
            question.setTimerSeconds(qh.timer);
            question = questionRepository.save(question);

            for (int i = 0; i < qh.answers.size(); i++) {
                AnswerData answer = new AnswerData();
                answer.setQuestion(question);
                answer.setAnswerText(qh.answers.get(i));
                answer.setAnswerIndex(i);
                answerRepository.save(answer);
            }
        }
    }

    private QuestionHelper createQuestion(String text, int order, int timer, List<String> answers, int correctIndex) {
        return new QuestionHelper(text, order, timer, answers, correctIndex);
    }

    private static class QuestionHelper {
        String text;
        int order;
        int timer;
        List<String> answers;
        int correctIndex;

        public QuestionHelper(String text, int order, int timer, List<String> answers, int correctIndex) {
            this.text = text;
            this.order = order;
            this.timer = timer;
            this.answers = answers;
            this.correctIndex = correctIndex;
        }
    }
}
