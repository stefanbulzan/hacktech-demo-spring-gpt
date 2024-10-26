package org.digeplan.common.springgpt.controller;

import lombok.RequiredArgsConstructor;
import org.digeplan.common.springgpt.model.MeetingQuestionRequest;
import org.digeplan.common.springgpt.model.MeetingQuestionResponse;
import org.digeplan.common.springgpt.model.MeetingTranscript;
import org.digeplan.common.springgpt.service.MeetingAnalysisService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingAnalysisController {

    private final MeetingAnalysisService analysisService;

    @PostMapping("/analyze")
    public ResponseEntity<MeetingQuestionResponse> analyzeMeeting(
            @RequestBody MeetingQuestionRequest request) {
        MeetingQuestionResponse response =
                analysisService.answerQuestion(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/transcripts")
    public ResponseEntity<String> uploadTranscript(
            @RequestBody MeetingTranscript transcript) {
        // Save transcript to MongoDB
        return ResponseEntity.ok("Transcript uploaded successfully");
    }
}
