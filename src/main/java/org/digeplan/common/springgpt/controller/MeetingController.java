package org.digeplan.common.springgpt.controller;

import lombok.RequiredArgsConstructor;
import org.digeplan.common.springgpt.model.MeetingTranscript;
import org.digeplan.common.springgpt.repository.MeetingTranscriptRepository;
import org.digeplan.common.springgpt.service.TranscriptConverterService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/meetings")
@RequiredArgsConstructor
public class MeetingController {

    private final TranscriptConverterService converterService;
    private final MeetingTranscriptRepository transcriptRepository;

    @PostMapping("/convert/custom")
    public ResponseEntity<List<MeetingTranscript>> convertCustomFormat(
            @RequestBody String jsonContent) {
        List<MeetingTranscript> transcripts =
                converterService.convertCustomFormat(jsonContent);

        // Save all transcripts
        transcriptRepository.saveAll(transcripts);

        return ResponseEntity.ok(transcripts);
    }
}
