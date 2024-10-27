package org.digeplan.common.springgpt.util;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.digeplan.common.springgpt.model.Meeting;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
public class MeetingConverter {
    private final ObjectMapper objectMapper;

    public MeetingConverter() {
        this.objectMapper = new ObjectMapper();
        this.objectMapper.registerModule(new JavaTimeModule());
    }

    public List<Meeting> convertJsonToMeetings(String json) {
        try {
            List<Meeting> meetings = objectMapper.readValue(json, new TypeReference<>() {
            });
            return meetings.stream()
                    .map(meeting -> meeting.withId(UUID.randomUUID().toString()))
                    .toList();
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to meetings", e);
        }
    }

    public Meeting convertJsonToMeeting(String json) {
        try {
            Meeting meeting = objectMapper.readValue(json, Meeting.class);
            return meeting.withId(UUID.randomUUID().toString());
        } catch (Exception e) {
            throw new RuntimeException("Error converting JSON to meeting", e);
        }
    }

    public String convertMeetingToJson(Meeting meeting) {
        try {
            return objectMapper.writeValueAsString(meeting);
        } catch (Exception e) {
            throw new RuntimeException("Error converting meeting to JSON", e);
        }
    }
}