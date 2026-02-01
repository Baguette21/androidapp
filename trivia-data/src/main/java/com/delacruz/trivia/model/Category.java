package com.delacruz.trivia.model;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class Category {
    private Long id;
    private String name;
    private String description;
    private Boolean isActive;
    private Integer questionCount;
    private LocalDateTime createdAt;
}
