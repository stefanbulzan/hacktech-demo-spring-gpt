package org.digeplan.common.springgpt.model;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class DialogueEntry {
    private String participantId;
    private String content;
    private LocalDateTime timestamp;
    private List<String> mentions; // @mentions
    private DialogueType type; //
}
