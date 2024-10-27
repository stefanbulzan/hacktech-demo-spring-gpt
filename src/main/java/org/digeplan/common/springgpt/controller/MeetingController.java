package org.digeplan.common.springgpt.controller;

import lombok.RequiredArgsConstructor;
import org.digeplan.common.springgpt.model.Meeting;
import org.digeplan.common.springgpt.service.MeetingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("meetings")
@RequiredArgsConstructor
public class MeetingController {
    private final MeetingService service;

    @GetMapping
    public List<Meeting> getMeetings() {
        return service.findAll();
    }

    @GetMapping("{id}")
    public Meeting getOneMeeting(@PathVariable("id") String meetingId) {
        return service.findById(meetingId);
    }

    @PostMapping("bulk")
    public List<Meeting> bulkAddMeetings(@RequestBody String request) {
        return service.saveAll(request);
    }
}
