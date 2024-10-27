package org.digeplan.common.springgpt.controller.filter;

import lombok.Builder;
import lombok.With;

@With
@Builder
public record MeetingFilter(
        String id
) {
}
