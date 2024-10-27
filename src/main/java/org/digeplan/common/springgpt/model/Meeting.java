package org.digeplan.common.springgpt.model;

import lombok.Builder;
import lombok.With;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.List;


@With
@Builder
@Document("meetings")
public record Meeting(
        String id,
        String title,
        LocalDateTime date,
        String duration,
        String source,
        List<String> organizers,
        List<String> participants,
        String content
) {
}
