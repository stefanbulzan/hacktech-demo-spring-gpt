package org.digeplan.common.springgpt.config;

import jakarta.annotation.PostConstruct;
import org.digeplan.common.springgpt.model.Meeting;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.index.TextIndexDefinition;

@Configuration
public class MongoTextIndexConfig {

    private final MongoTemplate mongoTemplate;

    public MongoTextIndexConfig(MongoTemplate mongoTemplate) {
        this.mongoTemplate = mongoTemplate;
    }

    @PostConstruct
    public void initializeTextIndex() {
        TextIndexDefinition textIndex = new TextIndexDefinition.TextIndexDefinitionBuilder()
                .onField("title")
                .named("meeting_title_text_index")
                .build();

        mongoTemplate.indexOps(Meeting.class).ensureIndex(textIndex);
    }
}
