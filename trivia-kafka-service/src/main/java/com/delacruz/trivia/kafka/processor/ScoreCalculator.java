package com.delacruz.trivia.kafka.processor;

import org.springframework.stereotype.Component;

@Component
public class ScoreCalculator {

    private static final int BASE_POINTS = 1000;
    private static final int MIN_POINTS = 500;
    private static final double STREAK_MULTIPLIER = 0.1;
    private static final int MAX_STREAK_BONUS = 5;

    public ScoreResult calculate(boolean isCorrect, long answerTimeMs, int timerSeconds, int currentStreak) {
        if (!isCorrect) {
            return new ScoreResult(0, 0, false);
        }

        // Speed bonus calculation
        long timerMs = timerSeconds * 1000L;
        double timeRatio = 1.0 - ((double) answerTimeMs / timerMs);
        timeRatio = Math.max(0, Math.min(1, timeRatio));

        int speedPoints = (int) (MIN_POINTS + (BASE_POINTS - MIN_POINTS) * timeRatio);

        // Streak multiplier calculation
        int newStreak = currentStreak + 1;
        int effectiveStreak = Math.min(newStreak, MAX_STREAK_BONUS);
        double multiplier = 1.0 + (effectiveStreak * STREAK_MULTIPLIER);

        int totalPoints = (int) (speedPoints * multiplier);

        return new ScoreResult(totalPoints, newStreak, true);
    }

    public static class ScoreResult {
        private final int pointsEarned;
        private final int newStreak;
        private final boolean isCorrect;

        public ScoreResult(int pointsEarned, int newStreak, boolean isCorrect) {
            this.pointsEarned = pointsEarned;
            this.newStreak = newStreak;
            this.isCorrect = isCorrect;
        }

        public int getPointsEarned() {
            return pointsEarned;
        }

        public int getNewStreak() {
            return newStreak;
        }

        public boolean isCorrect() {
            return isCorrect;
        }
    }
}
