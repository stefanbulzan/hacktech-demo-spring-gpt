package org.digeplan.common.springgpt.model;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MeetingQuestionRequest {
    private String question;
    private String meetingId;  // Optional - specific meeting
    private String department; // Optional - filter by department
    private DateRange dateRange; // Optional - filter by date range
    private List<String> tags;  // Optional - filter by tags
}
