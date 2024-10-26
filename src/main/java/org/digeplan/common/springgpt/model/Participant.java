package org.digeplan.common.springgpt.model;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class Participant {
    private String id;
    private String name;
    private String role;
    private String department;
}