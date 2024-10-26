package org.digeplan.common.springgpt.repository;

import org.digeplan.common.springgpt.model.MeetingTranscript;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MeetingTranscriptRepository extends MongoRepository<MeetingTranscript, String> {
    List<MeetingTranscript> findByDepartmentIgnoreCase(String department);

    List<MeetingTranscript> findByMeetingDateBetween(LocalDateTime start, LocalDateTime end);

    List<MeetingTranscript> findByTagsContainingIgnoreCase(String tag);
}
