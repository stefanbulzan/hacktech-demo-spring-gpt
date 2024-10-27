package org.digeplan.common.springgpt.service;

import lombok.RequiredArgsConstructor;
import org.digeplan.common.springgpt.model.Meeting;
import org.digeplan.common.springgpt.repository.MeetingRepo;
import org.digeplan.common.springgpt.util.MeetingConverter;
import org.springframework.stereotype.Service;

import java.lang.module.ResolutionException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepo meetingRepo;
    private final MeetingConverter meetingConverter;

    public List<Meeting> saveAll(String request) {
        return meetingRepo.saveAll(meetingConverter.convertJsonToMeetings(request));
    }

    public List<Meeting> findAll() {
        return meetingRepo.findAll();
    }

    public Meeting findById(String meetingId) {
        return meetingRepo.findById(meetingId)
                .orElseThrow(() -> new ResolutionException("Unable to find meeting with id " + meetingId));
    }
}
