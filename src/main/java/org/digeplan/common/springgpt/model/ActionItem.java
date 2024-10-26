package org.digeplan.common.springgpt.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ActionItem {
    private String description;
    private String assignee;
    private LocalDateTime dueDate;
    private String status;
}
