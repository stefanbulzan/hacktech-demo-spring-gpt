package org.digeplan.common.springgpt.model;

import lombok.Builder;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Document(collection = "meeting_transcripts")
@Data
@Builder
public class MeetingTranscript {
    @Id
    private String id;
    private String meetingTitle;
    private LocalDateTime meetingDate;
    private String department; // e.g., "Healthcare", "IT", "Finance"
    private List<Participant> participants;
    private List<DialogueEntry> dialogue;
    private List<String> tags;
    private Map<String, String> metadata;
    private List<DecisionPoint> decisions;
    private List<ActionItem> actionItems;
}
