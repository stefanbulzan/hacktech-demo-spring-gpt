package org.digeplan.common.springgpt.model;


import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DecisionPoint {
    private String topic;
    private String decision;
    private List<String> stakeholders;
    private LocalDateTime timestamp;
}
