package org.digeplan.common.springgpt.service;

import lombok.RequiredArgsConstructor;
import org.digeplan.common.springgpt.controller.filter.MeetingFilter;
import org.digeplan.common.springgpt.model.Meeting;
import org.digeplan.common.springgpt.repository.MeetingRepo;
import org.digeplan.common.springgpt.util.MeetingConverter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Service;

import java.lang.module.ResolutionException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class MeetingService {
    private final MeetingRepo meetingRepo;
    private final MeetingConverter meetingConverter;
    private final MongoTemplate mongoTemplate;

    public List<Meeting> saveAll(String request) {
        return meetingRepo.saveAll(meetingConverter.convertJsonToMeetings(request));
    }

    public List<Meeting> findAll(MeetingFilter filter) {
        if (filter != null && filter.title() != null) {
            TextCriteria criteria = TextCriteria.forDefaultLanguage()
                    .matchingPhrase(filter.title())
                    .caseSensitive(false);
            Query query = TextQuery.queryText(criteria)
                    .sortByScore();
            return mongoTemplate.find(query, Meeting.class);
        }
        return meetingRepo.findAll();
    }

    public Meeting findById(String meetingId) {
        return meetingRepo.findById(meetingId)
                .orElseThrow(() -> new ResolutionException("Unable to find meeting with id " + meetingId));
    }
}
