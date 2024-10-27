package org.digeplan.common.springgpt.model;

import java.time.LocalDate;

public record OOODetails(
        LocalDate startDate,
        LocalDate endDate,
        String replacement) {
}
