package org.digeplan.common.springgpt;

import lombok.RequiredArgsConstructor;
import org.digeplan.common.springgpt.model.*;
import org.digeplan.common.springgpt.repository.MeetingTranscriptRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class CommandRunner implements CommandLineRunner {
    private final MeetingTranscriptRepository repository;

    @Override
    public void run(String... args) throws Exception {
        // Example data creation
        MeetingTranscript transcript = MeetingTranscript.builder()
                .id("MT-2024-001")
                .meetingTitle("Patient Monitoring System Implementation Planning")
                .meetingDate(LocalDateTime.of(2024, 3, 26, 10, 0))
                .department("Healthcare")
                .participants(Arrays.asList(
                        Participant.builder()
                                .id("P1")
                                .name("Dr. Sarah Smith")
                                .role("Chief of Medicine")
                                .department("Healthcare")
                                .build(),
                        Participant.builder()
                                .id("P2")
                                .name("John Davis")
                                .role("IT Director")
                                .department("IT")
                                .build(),
                        Participant.builder()
                                .id("P3")
                                .name("Emma Wilson")
                                .role("Head Nurse")
                                .department("Healthcare")
                                .build(),
                        Participant.builder()
                                .id("P4")
                                .name("Mike Johnson")
                                .role("Project Manager")
                                .department("IT")
                                .build()
                ))
                .dialogue(Arrays.asList(
                        DialogueEntry.builder()
                                .participantId("P1")
                                .content("Good morning everyone. Today we're discussing the implementation of the new patient monitoring system. We need to ensure it meets all our clinical requirements while maintaining HIPAA compliance.")
                                .timestamp(LocalDateTime.of(2024, 3, 26, 10, 0))
                                .type(DialogueType.STATEMENT)
                                .mentions(List.of())
                                .build(),
                        DialogueEntry.builder()
                                .participantId("P2")
                                .content("I've reviewed the technical specifications. We can implement the system within 12 weeks, but we'll need to upgrade our network infrastructure in the ICU first.")
                                .timestamp(LocalDateTime.of(2024, 3, 26, 10, 2))
                                .type(DialogueType.STATEMENT)
                                .mentions(List.of())
                                .build(),
                        DialogueEntry.builder()
                                .participantId("P3")
                                .content("The nursing staff will need comprehensive training. Can we ensure the system has an intuitive interface? The current system is too complicated.")
                                .timestamp(LocalDateTime.of(2024, 3, 26, 10, 5))
                                .type(DialogueType.QUESTION)
                                .mentions(List.of())
                                .build(),
                        DialogueEntry.builder()
                                .participantId("P2")
                                .content("Yes, the new system has a much more user-friendly interface. We can arrange training sessions two weeks before go-live.")
                                .timestamp(LocalDateTime.of(2024, 3, 26, 10, 6))
                                .type(DialogueType.RESPONSE)
                                .mentions(List.of())
                                .build(),
                        DialogueEntry.builder()
                                .participantId("P4")
                                .content("Let's create a detailed timeline. First action item: Network upgrade in ICU needs to be completed by April 15th.")
                                .timestamp(LocalDateTime.of(2024, 3, 26, 10, 10))
                                .type(DialogueType.ACTION_ITEM)
                                .mentions(List.of())
                                .build(),
                        DialogueEntry.builder()
                                .participantId("P1")
                                .content("Agreed. We also need to ensure all patient data migration is tested thoroughly. This should be a priority action item.")
                                .timestamp(LocalDateTime.of(2024, 3, 26, 10, 12))
                                .type(DialogueType.ACTION_ITEM)
                                .mentions(List.of())
                                .build()
                ))
                .decisions(Arrays.asList(
                        DecisionPoint.builder()
                                .topic("Implementation Timeline")
                                .decision("System implementation to be completed within 12 weeks, starting with network upgrade")
                                .stakeholders(Arrays.asList("P1", "P2", "P4"))
                                .timestamp(LocalDateTime.of(2024, 3, 26, 10, 15))
                                .build(),
                        DecisionPoint.builder()
                                .topic("Training Schedule")
                                .decision("Staff training to begin 2 weeks before system go-live")
                                .stakeholders(Arrays.asList("P2", "P3"))
                                .timestamp(LocalDateTime.of(2024, 3, 26, 10, 20))
                                .build()
                ))
                .actionItems(Arrays.asList(
                        ActionItem.builder()
                                .description("Complete ICU network infrastructure upgrade")
                                .assignee("John Davis")
                                .dueDate(LocalDateTime.of(2024, 4, 15, 17, 0))
                                .status("NEW")
                                .build(),
                        ActionItem.builder()
                                .description("Develop and test data migration plan")
                                .assignee("Mike Johnson")
                                .dueDate(LocalDateTime.of(2024, 4, 30, 17, 0))
                                .status("NEW")
                                .build(),
                        ActionItem.builder()
                                .description("Create training materials and schedule")
                                .assignee("Emma Wilson")
                                .dueDate(LocalDateTime.of(2024, 5, 15, 17, 0))
                                .status("NEW")
                                .build()
                ))
                .tags(Arrays.asList("healthcare", "IT", "patient monitoring", "implementation", "training"))
                .metadata(Map.of(
                        "project", "Patient Monitoring System",
                        "priority", "High",
                        "location", "Main Conference Room"
                ))
                .build();

        repository.save(transcript);
    }
}
