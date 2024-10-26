package org.digeplan.common.springgpt.model;


import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class MeetingQuestionResponse {
    private String question;
    private String answer;
    //    private List<MeetingAnalysisService.MeetingReference> relevantMeetings;
//    private List<MeetingAnalysisService.DecisionReference> relatedDecisions;
    private List<ActionItem> relatedActionItems;
}