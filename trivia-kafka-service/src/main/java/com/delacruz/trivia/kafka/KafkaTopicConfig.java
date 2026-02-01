package com.delacruz.trivia.kafka;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaTopicConfig {

    public static final String TOPIC_ANSWER_SUBMITTED = "answer-submitted";
    public static final String TOPIC_SCORE_UPDATED = "score-updated";
    public static final String TOPIC_GAME_EVENTS = "game-events";

    @Bean
    public NewTopic answerSubmittedTopic() {
        return TopicBuilder.name(TOPIC_ANSWER_SUBMITTED)
                .partitions(10)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic scoreUpdatedTopic() {
        return TopicBuilder.name(TOPIC_SCORE_UPDATED)
                .partitions(10)
                .replicas(1)
                .build();
    }

    @Bean
    public NewTopic gameEventsTopic() {
        return TopicBuilder.name(TOPIC_GAME_EVENTS)
                .partitions(10)
                .replicas(1)
                .build();
    }
}
