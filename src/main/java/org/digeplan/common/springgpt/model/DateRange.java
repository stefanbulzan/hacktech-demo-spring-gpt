package org.digeplan.common.springgpt.model;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class DateRange {
    private LocalDateTime start;
    private LocalDateTime end;
}
