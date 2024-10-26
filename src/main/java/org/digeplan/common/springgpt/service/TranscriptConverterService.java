package org.digeplan.common.springgpt.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.digeplan.common.springgpt.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

@Service
@Slf4j
public class TranscriptConverterService {

    public List<MeetingTranscript> convertCustomFormat(String jsonContent) {
        try {
            ObjectMapper mapper = new ObjectMapper();
            List<CustomMeetingFormat> customMeetings =
                    mapper.readValue(jsonContent,
                            new TypeReference<>() {
                            });

            return customMeetings.stream()
                    .map(this::convertToMeetingTranscript)
                    .collect(Collectors.toList());
        } catch (Exception e) {
            log.error("Error converting custom format", e);
            throw new TranscriptConversionException(
                    "Failed to convert custom format", e);
        }
    }

    private MeetingTranscript convertToMeetingTranscript(
            CustomMeetingFormat customFormat) {

        // Create participant map
        Map<String, String> participantIdMap = new HashMap<>();
        AtomicInteger participantCounter = new AtomicInteger(1);

        // Assign IDs to participants
        customFormat.getParticipants().forEach(name ->
                participantIdMap.put(
                        name,
                        "P" + participantCounter.getAndIncrement()
                )
        );

        // Create participants list
        List<Participant> participants = customFormat.getParticipants()
                .stream()
                .map(name -> Participant.builder()
                        .id(participantIdMap.get(name))
                        .name(name)
                        .role(customFormat.getOrganizers().contains(name) ?
                                "Organizer" : "Participant")
                        .department("Unknown") // Can be enriched later
                        .build())
                .collect(Collectors.toList());

        // Parse dialogue
        List<DialogueEntry> dialogue = parseDialogue(
                customFormat.getContent(),
                participantIdMap
        );

        // Extract decisions and action items
        List<DecisionPoint> decisions = List.of();
        List<ActionItem> actionItems = List.of();

        // Generate tags
        Set<String> tags = extractTags(
                customFormat.getTitle(),
                dialogue
        );

        // Create metadata
        Map<String, String> metadata = new HashMap<>();
        metadata.put("source", customFormat.getSource());
        metadata.put("duration", customFormat.getDuration());
        metadata.put("originalFormat", "custom");

        return MeetingTranscript.builder()
                .id(UUID.randomUUID().toString())
                .meetingTitle(customFormat.getTitle())
                .meetingDate(LocalDateTime.parse(customFormat.getDate()))
                .department(extractDepartmentFromTitle(customFormat.getTitle()))
                .participants(participants)
                .dialogue(dialogue)
                .decisions(decisions)
                .actionItems(actionItems)
                .tags(new ArrayList<>(tags))
                .metadata(metadata)
                .build();
    }

    private List<DialogueEntry> parseDialogue(
            String content,
            Map<String, String> participantIdMap) {

        List<DialogueEntry> dialogue = new ArrayList<>();
        String[] lines = content.split("\n");
        LocalDateTime baseTime = LocalDateTime.now();

        for (int i = 0; i < lines.length; i++) {
            String line = lines[i].trim();
            if (line.isEmpty() || !line.startsWith("User: ")) continue;

            // Remove "User: " prefix
            line = line.substring(6);

            // Split name and content
            String[] parts = line.split(": ", 2);
            if (parts.length != 2) continue;

            String participantName = parts[0];
            content = parts[1];
            String participantId = participantIdMap.get(participantName);

            dialogue.add(DialogueEntry.builder()
                    .participantId(participantId)
                    .content(content)
                    .timestamp(baseTime.plusMinutes(i))
                    .type(determineDialogueType(content))
                    .build());
        }

        return dialogue;
    }

    private Set<String> extractTags(String title, List<DialogueEntry> dialogue) {
        Set<String> tags = new HashSet<>();

        // Add keywords from title
        Arrays.stream(title.toLowerCase().split("\\W+"))
                .filter(word -> word.length() > 3)
                .forEach(tags::add);

        // Add location tags
        tags.add("oradea");


        return tags;
    }

    private String extractDepartmentFromTitle(String title) {
        String lowerTitle = title.toLowerCase();

        if (lowerTitle.contains("market") ||
                lowerTitle.contains("client")) {
            return "Market Research";
        }

        if (lowerTitle.contains("construction") ||
                lowerTitle.contains("building")) {
            return "Construction";
        }

        return "General";
    }

    /**
     * Determines the type of dialogue entry based on content analysis
     */
    private DialogueType determineDialogueType(String content) {
        String normalizedContent = content.toLowerCase().trim();

        // Check for action items first
        if (isActionItem(normalizedContent)) {
            return DialogueType.ACTION_ITEM;
        }

        // Check for decisions
        if (isDecision(normalizedContent)) {
            return DialogueType.DECISION;
        }

        // Check for questions
        if (isQuestion(normalizedContent)) {
            return DialogueType.QUESTION;
        }

        // Check for responses
        if (isResponse(normalizedContent)) {
            return DialogueType.RESPONSE;
        }

        // Default to statement
        return DialogueType.STATEMENT;
    }

    /**
     * Checks if the content represents an action item
     */
    private boolean isActionItem(String content) {
        // Action item keywords and patterns
        List<String> actionItemPatterns = Arrays.asList(
                "action item:",
                "todo:",
                "task:",
                "needs to be done",
                "must complete",
                "will handle",
                "should implement",
                "assign",
                "let's create",
                "we need to",
                "please ensure",
                "make sure to",
                "required action",
                "follow up on",
                "take care of",
                "responsible for",
                "deadline",
                "by next",
                "by tomorrow",
                "by monday",
                "will be responsible"
        );

        return containsAnyPattern(content, actionItemPatterns) ||
                containsDeadlinePattern(content);
    }

    /**
     * Checks if the content represents a decision
     */
    private boolean isDecision(String content) {
        // Decision keywords and patterns
        List<String> decisionPatterns = Arrays.asList(
                "decision:",
                "decided:",
                "agreed:",
                "approved:",
                "consensus:",
                "we have decided",
                "we agree",
                "let's proceed with",
                "we will go with",
                "final decision",
                "moving forward with",
                "it's decided",
                "we've chosen",
                "the team has selected",
                "we're going to",
                "we have selected",
                "will implement",
                "approved approach"
        );

        return containsAnyPattern(content, decisionPatterns);
    }

    /**
     * Checks if the content is a question
     */
    private boolean isQuestion(String content) {
        // Question patterns
        if (content.contains("?")) {
            return true;
        }

        List<String> questionPatterns = Arrays.asList(
                "what",
                "when",
                "where",
                "who",
                "why",
                "how",
                "could you",
                "can we",
                "should we",
                "shall we",
                "do you think",
                "are we",
                "will this",
                "anyone know"
        );

        return startsWithAnyPattern(content, questionPatterns);
    }

    /**
     * Checks if the content is a response
     */
    private boolean isResponse(String content) {
        // Response patterns
        List<String> responsePatterns = Arrays.asList(
                "yes",
                "no",
                "agree",
                "disagree",
                "correct",
                "incorrect",
                "that's right",
                "that's wrong",
                "exactly",
                "definitely",
                "absolutely",
                "makes sense",
                "i think so",
                "not really",
                "good point"
        );

        // Check if it's a direct response to a previous question
        boolean isDirectResponse = startsWithAnyPattern(content, responsePatterns);

        // Check for explanation patterns that might indicate a response
        List<String> explanationPatterns = Arrays.asList(
                "because",
                "the reason is",
                "this is due to",
                "that's because",
                "let me explain",
                "to answer your question"
        );

        return isDirectResponse ||
                containsAnyPattern(content, explanationPatterns);
    }

    /**
     * Checks if content contains deadline-related patterns
     */
    private boolean containsDeadlinePattern(String content) {
        // Regular expressions for common deadline patterns
        List<Pattern> deadlinePatterns = Arrays.asList(
                Pattern.compile("by \\w+day"),  // by Monday, by Tuesday, etc.
                Pattern.compile("by \\d{1,2}(st|nd|rd|th)"), // by 1st, 2nd, etc.
                Pattern.compile("by (tomorrow|next week|month end)"),
                Pattern.compile("due (on|by|before|until)"),
                Pattern.compile("deadline[: ]"),
                Pattern.compile("\\d{1,2}/\\d{1,2}(/\\d{2,4})?"), // dates like 1/1/23
                Pattern.compile("\\d{1,2}[-.]\\d{1,2}([-.]\\d{2,4})?") // dates like 1-1-23
        );

        return deadlinePatterns.stream()
                .anyMatch(pattern -> pattern.matcher(content).find());
    }

    /**
     * Helper method to check if content contains any of the patterns
     */
    private boolean containsAnyPattern(String content, List<String> patterns) {
        return patterns.stream()
                .anyMatch(pattern -> content.contains(pattern.toLowerCase()));
    }

    /**
     * Helper method to check if content starts with any of the patterns
     */
    private boolean startsWithAnyPattern(String content, List<String> patterns) {
        return patterns.stream()
                .anyMatch(pattern ->
                        content.startsWith(pattern.toLowerCase()) ||
                                content.matches("^[\\s,.;:]+?" + Pattern.quote(pattern.toLowerCase()) + ".*")
                );
    }

    /**
     * Optional: More detailed dialogue type detection
     */
    private DialogueType determineDetailedDialogueType(String content) {
        String normalizedContent = content.toLowerCase().trim();

        // First check the basic types
        if (isActionItem(normalizedContent)) return DialogueType.ACTION_ITEM;
        if (isDecision(normalizedContent)) return DialogueType.DECISION;
        if (isQuestion(normalizedContent)) return DialogueType.QUESTION;
        if (isResponse(normalizedContent)) return DialogueType.RESPONSE;

        // Then check more specific types
        if (isClarification(normalizedContent)) return DialogueType.CLARIFICATION;
        if (isSuggestion(normalizedContent)) return DialogueType.SUGGESTION;
        if (isConcern(normalizedContent)) return DialogueType.CONCERN;

        return DialogueType.STATEMENT;
    }

    /**
     * Helper methods for detailed type detection
     */
    private boolean isClarification(String content) {
        List<String> patterns = Arrays.asList(
                "to clarify",
                "let me explain",
                "in other words",
                "to be clear",
                "meaning",
                "specifically",
                "for example"
        );
        return containsAnyPattern(content, patterns);
    }

    private boolean isSuggestion(String content) {
        List<String> patterns = Arrays.asList(
                "maybe we could",
                "how about",
                "we might",
                "suggest",
                "consider",
                "what if",
                "could we",
                "one option"
        );
        return containsAnyPattern(content, patterns);
    }

    private boolean isConcern(String content) {
        List<String> patterns = Arrays.asList(
                "worried about",
                "concern",
                "risk",
                "issue",
                "problem",
                "challenge",
                "careful",
                "warning"
        );
        return containsAnyPattern(content, patterns);
    }

    @Data
    private static class CustomMeetingFormat {
        private String title;
        private String date;
        private String duration;
        private String source;
        private List<String> organizers;
        private List<String> participants;
        private String content;
    }
}
