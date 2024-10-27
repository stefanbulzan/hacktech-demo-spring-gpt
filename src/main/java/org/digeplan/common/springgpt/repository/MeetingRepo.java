package org.digeplan.common.springgpt.repository;

import org.digeplan.common.springgpt.model.Meeting;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MeetingRepo extends MongoRepository<Meeting, String> {
}
