package org.digeplan.common.springgpt.service;

import lombok.extern.slf4j.Slf4j;
import org.digeplan.common.springgpt.model.MeetingQuestionRequest;
import org.digeplan.common.springgpt.model.MeetingQuestionResponse;
import org.digeplan.common.springgpt.model.MeetingTranscript;
import org.digeplan.common.springgpt.model.Participant;
import org.digeplan.common.springgpt.repository.MeetingTranscriptRepository;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.SystemMessage;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
@Slf4j
public class MeetingAnalysisService {

    private final MeetingTranscriptRepository transcriptRepository;
    private final String systemPrompt;
    private final ChatClient chatClient;

    public MeetingAnalysisService(ChatClient.Builder chatBuilder,
                                  MeetingTranscriptRepository transcriptRepository) {
        chatClient = chatBuilder.build();
        this.transcriptRepository = transcriptRepository;
        this.systemPrompt = initializeSystemPrompt();
    }

    private String initializeSystemPrompt() {
        return """
                You are an AI assistant specialized in analyzing meeting transcripts and providing 
                detailed answers about meeting contents. Your role is to:
                1. Understand and summarize key points from meetings
                2. Identify and explain decisions made
                3. Track action items and their assignees
                4. Provide context about discussions
                5. Reference specific parts of the conversation when answering questions
                6. Maintain confidentiality and professional tone
                
                When answering questions:
                - Be specific and reference the exact part of the transcript
                - Include relevant context about who said what
                - Highlight any decisions or action items related to the question
                - If information is not in the transcript, clearly state that
                """;
    }

    public MeetingQuestionResponse answerQuestion(MeetingQuestionRequest request) {
        try {
            // Find relevant meeting transcripts
            List<MeetingTranscript> relevantTranscripts =
                    findRelevantTranscripts(request);

            // Build context from transcripts
            String context = buildTranscriptContext(relevantTranscripts);

            // Generate AI response
            String response = generateAIResponse(request.getQuestion(), context);

            return MeetingQuestionResponse.builder()
                    .question(request.getQuestion())
                    .answer(response)
                    .build();

        } catch (Exception e) {
            log.error("Error analyzing meeting transcript", e);
            throw new MeetingAnalysisException("Failed to analyze meeting", e);
        }
    }

    private List<MeetingTranscript> findRelevantTranscripts(
            MeetingQuestionRequest request) {
        Set<MeetingTranscript> relevantTranscripts = new HashSet<>();

        // If specific meeting ID is provided
        if (request.getMeetingId() != null) {
            transcriptRepository.findById(request.getMeetingId())
                    .ifPresent(relevantTranscripts::add);
            return new ArrayList<>(relevantTranscripts);
        }

        // Search by department
        if (request.getDepartment() != null) {
            relevantTranscripts.addAll(
                    transcriptRepository.findByDepartmentIgnoreCase(
                            request.getDepartment()
                    )
            );
        }

        // Search by date range
        if (request.getDateRange() != null) {
            relevantTranscripts.addAll(
                    transcriptRepository.findByMeetingDateBetween(
                            request.getDateRange().getStart(),
                            request.getDateRange().getEnd()
                    )
            );
        }

        // Search by tags
        if (request.getTags() != null && !request.getTags().isEmpty()) {
            request.getTags().forEach(tag ->
                    relevantTranscripts.addAll(
                            transcriptRepository.findByTagsContainingIgnoreCase(tag)
                    )
            );
        }

        return new ArrayList<>(relevantTranscripts);
    }

    private String buildTranscriptContext(List<MeetingTranscript> transcripts) {
        StringBuilder context = new StringBuilder();

        transcripts.forEach(transcript -> {
            context.append("Meeting: ").append(transcript.getMeetingTitle())
                    .append(" (").append(transcript.getMeetingDate()).append(")\n");
            context.append("Department: ").append(transcript.getDepartment())
                    .append("\n\n");

            context.append("Participants:\n");
            transcript.getParticipants().forEach(p ->
                    context.append("- ").append(p.getName())
                            .append(" (").append(p.getRole()).append(")\n")
            );

            context.append("\nDialogue:\n");
            transcript.getDialogue().forEach(entry -> {
                String speaker = transcript.getParticipants().stream()
                        .filter(p -> p.getId().equals(entry.getParticipantId()))
                        .findFirst()
                        .map(Participant::getName)
                        .orElse("Unknown");

                context.append("[").append(entry.getTimestamp().toLocalTime())
                        .append("] ").append(speaker).append(": ")
                        .append(entry.getContent()).append("\n");
            });

            if (!transcript.getDecisions().isEmpty()) {
                context.append("\nDecisions:\n");
                transcript.getDecisions().forEach(d ->
                        context.append("- ").append(d.getTopic())
                                .append(": ").append(d.getDecision()).append("\n")
                );
            }

            if (!transcript.getActionItems().isEmpty()) {
                context.append("\nAction Items:\n");
                transcript.getActionItems().forEach(a ->
                        context.append("- ").append(a.getDescription())
                                .append(" (Assignee: ").append(a.getAssignee())
                                .append(")\n")
                );
            }

            context.append("\n---\n\n");
        });

        return context.toString();
    }

    private String generateAIResponse(String question, String context) {
        Prompt prompt = new Prompt(Arrays.asList(
                new SystemMessage(systemPrompt),
                new UserMessage("Context:\n" + context + "\n\nQuestion: " + question)
        ));

        return chatClient.prompt(prompt)
                .call()
                .content();
    }
//
//    private List<MeetingReference> extractMeetingReferences(
//            List<MeetingTranscript> transcripts) {
//
//        return transcripts.stream()
//                .map(this::createMeetingReference)
//                .sorted(Comparator.comparing(MeetingReference::getRelevanceScore).reversed())
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Creates a meeting reference with relevance scoring
//     */
//    private MeetingReference createMeetingReference(MeetingTranscript transcript) {
//        return MeetingReference.builder()
//                .meetingId(transcript.getId())
//                .title(transcript.getMeetingTitle())
//                .date(transcript.getMeetingDate())
//                .relevanceScore(calculateRelevanceScore(transcript))
//                .keyTopics(extractKeyTopics(transcript))
//                .participants(extractKeyParticipants(transcript))
//                .snippets(extractRelevantSnippets(transcript))
//                .build();
//    }
//
//    /**
//     * Enhanced MeetingReference class with additional context
//     */
//    @Data
//    @Builder
//    public static class MeetingReference {
//        private String meetingId;
//        private String title;
//        private LocalDateTime date;
//        private String relevanceScore;
//        private List<String> keyTopics;
//        private List<ParticipantSummary> participants;
//        private List<DialogueSnippet> snippets;
//    }
//
//    @Data
//    @Builder
//    public static class ParticipantSummary {
//        private String name;
//        private String role;
//        private int contributionCount;
//    }
//
//    @Data
//    @Builder
//    public static class DialogueSnippet {
//        private String speakerName;
//        private String content;
//        private LocalDateTime timestamp;
//        private double relevanceScore;
//    }
//
//    /**
//     * Calculates relevance score based on multiple factors
//     */
//    private String calculateRelevanceScore(MeetingTranscript transcript) {
//        double score = 0.0;
//        double maxScore = 100.0;
//
//        // Factor 1: Recency (30% weight)
//        double recencyScore = calculateRecencyScore(transcript.getMeetingDate());
//        score += (recencyScore * 0.3);
//
//        // Factor 2: Content Relevance (40% weight)
//        double contentScore = calculateContentRelevanceScore(transcript);
//        score += (contentScore * 0.4);
//
//        // Factor 3: Participant Engagement (15% weight)
//        double participantScore = calculateParticipantScore(transcript);
//        score += (participantScore * 0.15);
//
//        // Factor 4: Decision/Action Item Weight (15% weight)
//        double decisionScore = calculateDecisionScore(transcript);
//        score += (decisionScore * 0.15);
//
//        // Format score to percentage with two decimal places
//        return String.format("%.2f", (score / maxScore) * 100);
//    }
//
//    /**
//     * Calculates recency score based on meeting date
//     */
//    private double calculateRecencyScore(LocalDateTime meetingDate) {
//        long daysDifference = ChronoUnit.DAYS.between(
//                meetingDate,
//                LocalDateTime.now()
//        );
//
//        // Higher score for more recent meetings
//        // Score decreases logarithmically with age
//        if (daysDifference <= 0) return 100.0;
//        return Math.max(0, 100 - (Math.log(daysDifference + 1) * 20));
//    }
//
//    /**
//     * Calculates content relevance score
//     */
//    private double calculateContentRelevanceScore(MeetingTranscript transcript) {
//        // Initialize NLP processor if needed
//        // TextRankProcessor textRank = new TextRankProcessor();
//
//        Set<String> keyTerms = new HashSet<>();
//        double totalScore = 0.0;
//
//        // Analyze dialogue content
//        for (DialogueEntry entry : transcript.getDialogue()) {
//            // Add key terms from content
//            keyTerms.addAll(extractKeyTerms(entry.getContent()));
//
//            // Check for important markers
//            if (entry.getType() == DialogueType.DECISION) {
//                totalScore += 10.0;
//            }
//            if (entry.getType() == DialogueType.ACTION_ITEM) {
//                totalScore += 8.0;
//            }
//            if (entry.getMentions() != null && !entry.getMentions().isEmpty()) {
//                totalScore += 5.0;
//            }
//        }
//
//        // Add score for unique key terms
//        totalScore += keyTerms.size() * 2.0;
//
//        return Math.min(100.0, totalScore);
//    }
//
//    /**
//     * Calculates participant engagement score
//     */
//    private double calculateParticipantScore(MeetingTranscript transcript) {
//        Map<String, Integer> participantContributions = new HashMap<>();
//
//        // Count contributions per participant
//        transcript.getDialogue().forEach(entry ->
//                participantContributions.merge(
//                        entry.getParticipantId(),
//                        1,
//                        Integer::sum
//                )
//        );
//
//        // Calculate engagement metrics
//        double avgContributions = participantContributions.values().stream()
//                .mapToInt(Integer::intValue)
//                .average()
//                .orElse(0.0);
//
//        double participationRate = (double) participantContributions.size() /
//                transcript.getParticipants().size();
//
//        return (avgContributions * 0.5 + participationRate * 50.0);
//    }
//
//    /**
//     * Calculates decision and action item score
//     */
//    private double calculateDecisionScore(MeetingTranscript transcript) {
//        double score = 0.0;
//
//        // Score based on decisions made
//        score += transcript.getDecisions().size() * 15.0;
//
//        // Score based on action items
//        score += transcript.getActionItems().size() * 10.0;
//
//        // Cap the score at 100
//        return Math.min(100.0, score);
//    }
//
//    /**
//     * Extracts key topics from the transcript
//     */
//    private List<String> extractKeyTopics(MeetingTranscript transcript) {
//        // Combine all dialogue content
//        String fullContent = transcript.getDialogue().stream()
//                .map(DialogueEntry::getContent)
//                .collect(Collectors.joining(" "));
//
//        // Extract key terms and phrases
//        return extractKeyTerms(fullContent).stream()
//                .limit(5) // Top 5 topics
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Extracts key participants with their contribution summaries
//     */
//    private List<ParticipantSummary> extractKeyParticipants(
//            MeetingTranscript transcript) {
//
//        Map<String, Integer> contributionCounts = new HashMap<>();
//
//        // Count contributions
//        transcript.getDialogue().forEach(entry ->
//                contributionCounts.merge(
//                        entry.getParticipantId(),
//                        1,
//                        Integer::sum
//                )
//        );
//
//        // Create summaries
//        return transcript.getParticipants().stream()
//                .map(participant -> ParticipantSummary.builder()
//                        .name(participant.getName())
//                        .role(participant.getRole())
//                        .contributionCount(
//                                contributionCounts.getOrDefault(participant.getId(), 0)
//                        )
//                        .build()
//                )
//                .sorted(Comparator.comparing(
//                        ParticipantSummary::getContributionCount).reversed()
//                )
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Extracts relevant dialogue snippets
//     */
//    private List<DialogueSnippet> extractRelevantSnippets(
//            MeetingTranscript transcript) {
//
//        return transcript.getDialogue().stream()
//                .filter(this::isSignificantEntry)
//                .map(entry -> {
//                    String speakerName = transcript.getParticipants().stream()
//                            .filter(p -> p.getId().equals(entry.getParticipantId()))
//                            .findFirst()
//                            .map(Participant::getName)
//                            .orElse("Unknown");
//
//                    return DialogueSnippet.builder()
//                            .speakerName(speakerName)
//                            .content(entry.getContent())
//                            .timestamp(entry.getTimestamp())
//                            .relevanceScore(calculateSnippetRelevance(entry))
//                            .build();
//                })
//                .sorted(Comparator.comparing(
//                        DialogueSnippet::getRelevanceScore).reversed()
//                )
//                .limit(5) // Top 5 most relevant snippets
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Determines if a dialogue entry is significant
//     */
//    private boolean isSignificantEntry(DialogueEntry entry) {
//        return entry.getType() == DialogueType.DECISION ||
//                entry.getType() == DialogueType.ACTION_ITEM ||
//                !entry.getMentions().isEmpty() ||
//                containsKeyPhrases(entry.getContent());
//    }
//
//    /**
//     * Calculates relevance score for a dialogue snippet
//     */
//    private double calculateSnippetRelevance(DialogueEntry entry) {
//        double score = 0.0;
//
//        // Score based on entry type
//        switch (entry.getType()) {
//            case DECISION:
//                score += 30.0;
//                break;
//            case ACTION_ITEM:
//                score += 25.0;
//                break;
//            case QUESTION:
//                score += 15.0;
//                break;
//            default:
//                score += 10.0;
//        }
//
//        // Add score for mentions
//        score += entry.getMentions().size() * 5.0;
//
//        // Add score for key phrases
//        score += countKeyPhrases(entry.getContent()) * 5.0;
//
//        return Math.min(100.0, score);
//    }
//
//    /**
//     * Extracts key terms from text
//     */
//    private Set<String> extractKeyTerms(String text) {
//        // This is a simplified implementation
//        // In a production environment, you might want to use
//        // NLP libraries like OpenNLP, Stanford NLP, or spaCy
//
//        return Arrays.stream(text.toLowerCase()
//                        .replaceAll("[^a-zA-Z0-9\\s]", "")
//                        .split("\\s+"))
//                .filter(term -> term.length() > 3) // Filter out short words
//                .filter(term -> !STOP_WORDS.contains(term)) // Filter out stop words
//                .collect(Collectors.toSet());
//    }
//
//    private static final Set<String> STOP_WORDS = new HashSet<>(Arrays.asList(
//            "the", "is", "at", "which", "on", "the", "a", "an", "and", "or", "but"
//            // Add more stop words as needed
//    ));
//
//    /**
//     * Checks if text contains key phrases
//     */
//    private boolean containsKeyPhrases(String text) {
//        String lowerText = text.toLowerCase();
//        return KEY_PHRASES.stream()
//                .anyMatch(lowerText::contains);
//    }
//
//    /**
//     * Counts key phrases in text
//     */
//    private int countKeyPhrases(String text) {
//        String lowerText = text.toLowerCase();
//        return (int) KEY_PHRASES.stream()
//                .filter(lowerText::contains)
//                .count();
//    }
//
//    private static final Set<String> KEY_PHRASES = new HashSet<>(Arrays.asList(
//            "we need to",
//            "important",
//            "critical",
//            "decision",
//            "agree",
//            "disagree",
//            "propose",
//            "implement",
//            "deadline",
//            "priority"
//            // Add more key phrases as needed
//    ));
//
//
//    /**
//     * Extracts relevant decisions with context and impact assessment
//     */
//    private List<DecisionReference> extractRelevantDecisions(
//            List<MeetingTranscript> transcripts) {
//
//        // Collect all decisions across transcripts with context
//        List<DecisionReference> allDecisions = transcripts.stream()
//                .flatMap(transcript -> extractDecisionsFromTranscript(transcript).stream())
//                .sorted(Comparator.comparing(DecisionReference::getRelevanceScore).reversed())
//                .collect(Collectors.toList());
//
//        // Group related decisions
//        return consolidateRelatedDecisions(allDecisions);
//    }
//
//    /**
//     * Enhanced decision reference model
//     */
//    @Data
//    @Builder
//    public static class DecisionReference {
//        private String decisionId;
//        private String topic;
//        private String decision;
//        private LocalDateTime timestamp;
//        private double relevanceScore;
//        private String meetingId;
//        private String meetingTitle;
//        private DecisionStatus status;
//        private List<String> stakeholders;
//        private List<String> impactedAreas;
//        private List<DialogueContext> discussionContext;
//        private List<ActionItem> relatedActionItems;
//        private List<String> relatedDecisions; // IDs of related decisions
//    }
//
//    @Data
//    @Builder
//    public static class DialogueContext {
//        private String speakerName;
//        private String speakerRole;
//        private String content;
//        private LocalDateTime timestamp;
//        private DialogueType type;
//    }
//
//    public enum DecisionStatus {
//        IMPLEMENTED,
//        IN_PROGRESS,
//        PENDING,
//        BLOCKED,
//        SUPERSEDED
//    }
//
//    /**
//     * Extracts decisions from a single transcript with context
//     */
//    private List<DecisionReference> extractDecisionsFromTranscript(
//            MeetingTranscript transcript) {
//
//        List<DecisionReference> decisions = new ArrayList<>();
//
//        // Process each decision point in the transcript
//        for (DecisionPoint decisionPoint : transcript.getDecisions()) {
//            DecisionReference.DecisionReferenceBuilder builder = DecisionReference.builder()
//                    .decisionId(generateDecisionId(transcript, decisionPoint))
//                    .topic(decisionPoint.getTopic())
//                    .decision(decisionPoint.getDecision())
//                    .timestamp(decisionPoint.getTimestamp())
//                    .meetingId(transcript.getId())
//                    .meetingTitle(transcript.getMeetingTitle())
//                    .stakeholders(decisionPoint.getStakeholders())
//                    .status(determineDecisionStatus(transcript, decisionPoint))
//                    .impactedAreas(identifyImpactedAreas(transcript, decisionPoint))
//                    .discussionContext(extractDiscussionContext(transcript, decisionPoint))
//                    .relatedActionItems(findRelatedActionItems(transcript, decisionPoint))
//                    .relevanceScore(calculateDecisionRelevance(transcript, decisionPoint));
//
//            decisions.add(builder.build());
//        }
//
//        return decisions;
//    }
//
//    /**
//     * Generates a unique decision ID
//     */
//    private String generateDecisionId(
//            MeetingTranscript transcript,
//            DecisionPoint decision) {
//        return transcript.getId() + "-D" +
//                transcript.getDecisions().indexOf(decision);
//    }
//
//    /**
//     * Determines the current status of a decision
//     */
//    private DecisionStatus determineDecisionStatus(
//            MeetingTranscript transcript,
//            DecisionPoint decision) {
//
//        // Check related action items status
//        List<ActionItem> relatedActions = findRelatedActionItems(transcript, decision);
//
//        if (relatedActions.isEmpty()) {
//            return DecisionStatus.PENDING;
//        }
//
//        boolean allCompleted = relatedActions.stream()
//                .allMatch(item -> "COMPLETED".equals(item.getStatus()));
//        boolean anyBlocked = relatedActions.stream()
//                .anyMatch(item -> "BLOCKED".equals(item.getStatus()));
//        boolean anyInProgress = relatedActions.stream()
//                .anyMatch(item -> "IN_PROGRESS".equals(item.getStatus()));
//
//        if (allCompleted) {
//            return DecisionStatus.IMPLEMENTED;
//        } else if (anyBlocked) {
//            return DecisionStatus.BLOCKED;
//        } else if (anyInProgress) {
//            return DecisionStatus.IN_PROGRESS;
//        }
//
//        return DecisionStatus.PENDING;
//    }
//
//    /**
//     * Identifies areas impacted by the decision
//     */
//    private List<String> identifyImpactedAreas(
//            MeetingTranscript transcript,
//            DecisionPoint decision) {
//
//        Set<String> impactedAreas = new HashSet<>();
//
//        // Add department of the meeting
//        impactedAreas.add(transcript.getDepartment());
//
//        // Add departments of stakeholders
//        decision.getStakeholders().forEach(stakeholderId -> {
//            transcript.getParticipants().stream()
//                    .filter(p -> p.getId().equals(stakeholderId))
//                    .map(Participant::getDepartment)
//                    .forEach(impactedAreas::add);
//        });
//
//        // Analyze decision content for department mentions
//        String decisionText = decision.getDecision().toLowerCase();
//        DEPARTMENT_KEYWORDS.forEach((dept, keywords) -> {
//            if (keywords.stream().anyMatch(decisionText::contains)) {
//                impactedAreas.add(dept);
//            }
//        });
//
//        return new ArrayList<>(impactedAreas);
//    }
//
//    private static final Map<String, List<String>> DEPARTMENT_KEYWORDS =
//            Map.of(
//                    "IT", Arrays.asList("system", "software", "technology", "infrastructure"),
//                    "Finance", Arrays.asList("budget", "cost", "funding", "expense"),
//                    "Healthcare", Arrays.asList("patient", "clinical", "medical", "treatment"),
//                    "Operations", Arrays.asList("process", "workflow", "operation", "logistics")
//            );
//
//    /**
//     * Extracts discussion context around the decision
//     */
//    private List<DialogueContext> extractDiscussionContext(
//            MeetingTranscript transcript,
//            DecisionPoint decision) {
//
//        List<DialogueContext> context = new ArrayList<>();
//        LocalDateTime decisionTime = decision.getTimestamp();
//
//        // Find relevant dialogue entries around the decision time
//        // (5 minutes before and after the decision)
//        transcript.getDialogue().stream()
//                .filter(entry -> isWithinTimeWindow(
//                        entry.getTimestamp(),
//                        decisionTime,
//                        5
//                ))
//                .forEach(entry -> {
//                    Participant speaker = findParticipant(
//                            transcript,
//                            entry.getParticipantId()
//                    );
//
//                    context.add(DialogueContext.builder()
//                            .speakerName(speaker.getName())
//                            .speakerRole(speaker.getRole())
//                            .content(entry.getContent())
//                            .timestamp(entry.getTimestamp())
//                            .type(entry.getType())
//                            .build());
//                });
//
//        return context;
//    }
//
//    /**
//     * Finds related action items for a decision
//     */
//    private List<ActionItem> findRelatedActionItems(
//            MeetingTranscript transcript,
//            DecisionPoint decision) {
//
//        return transcript.getActionItems().stream()
//                .filter(item -> isActionItemRelatedToDecision(item, decision))
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Checks if an action item is related to a decision
//     */
//    private boolean isActionItemRelatedToDecision(
//            ActionItem item,
//            DecisionPoint decision) {
//
//        // Check if the action item was created close to the decision time
//        boolean timeRelated = isWithinTimeWindow(
//                item.getDueDate(),
//                decision.getTimestamp(),
//                30
//        );
//
//        // Check for content similarity
//        boolean contentRelated = hasContentSimilarity(
//                item.getDescription(),
//                decision.getDecision()
//        );
//
//        // Check for stakeholder overlap
//        boolean stakeholderOverlap = !Collections.disjoint(
//                decision.getStakeholders(),
//                Collections.singletonList(item.getAssignee())
//        );
//
//        return timeRelated && (contentRelated || stakeholderOverlap);
//    }
//
//    /**
//     * Calculates the relevance score for a decision
//     */
//    private double calculateDecisionRelevance(
//            MeetingTranscript transcript,
//            DecisionPoint decision) {
//
//        double score = 0.0;
//
//        // Factor 1: Recency (30%)
//        double recencyScore = calculateRecencyScore(decision.getTimestamp());
//        score += (recencyScore * 0.3);
//
//        // Factor 2: Impact Scope (25%)
//        double impactScore = identifyImpactedAreas(transcript, decision).size() * 20.0;
//        score += (Math.min(100.0, impactScore) * 0.25);
//
//        // Factor 3: Stakeholder Involvement (25%)
//        double stakeholderScore = decision.getStakeholders().size() * 15.0;
//        score += (Math.min(100.0, stakeholderScore) * 0.25);
//
//        // Factor 4: Implementation Progress (20%)
//        double implementationScore = calculateImplementationScore(
//                determineDecisionStatus(transcript, decision)
//        );
//        score += (implementationScore * 0.20);
//
//        return Math.min(100.0, score);
//    }
//
//    /**
//     * Consolidates related decisions across meetings
//     */
//    private List<DecisionReference> consolidateRelatedDecisions(
//            List<DecisionReference> decisions) {
//
//        Map<String, List<DecisionReference>> topicGroups = new HashMap<>();
//
//        // Group decisions by similar topics
//        decisions.forEach(decision -> {
//            String normalizedTopic = normalizeText(decision.getTopic());
//            topicGroups.computeIfAbsent(
//                    normalizedTopic,
//                    k -> new ArrayList<>()
//            ).add(decision);
//        });
//
//        // For each group, link related decisions
//        return topicGroups.values().stream()
//                .map(group -> {
//                    if (group.size() == 1) {
//                        return group.get(0);
//                    }
//
//                    // Link related decisions in the group
//                    List<String> relatedIds = group.stream()
//                            .map(DecisionReference::getDecisionId)
//                            .collect(Collectors.toList());
//
//                    group.forEach(decision ->
//                            decision.setRelatedDecisions(
//                                    relatedIds.stream()
//                                            .filter(id -> !id.equals(decision.getDecisionId()))
//                                            .collect(Collectors.toList())
//                            )
//                    );
//
//                    return group.get(0); // Return the most recent/relevant decision
//                })
//                .collect(Collectors.toList());
//    }
//
//    /**
//     * Helper method to check if two timestamps are within a specified window
//     */
//    private boolean isWithinTimeWindow(
//            LocalDateTime time1,
//            LocalDateTime time2,
//            int minutesWindow) {
//
//        return Math.abs(ChronoUnit.MINUTES.between(time1, time2)) <= minutesWindow;
//    }
//
//    /**
//     * Helper method to find a participant by ID
//     */
//    private Participant findParticipant(MeetingTranscript transcript, String participantId) {
//        return transcript.getParticipants().stream()
//                .filter(p -> p.getId().equals(participantId))
//                .findFirst()
//                .orElse(Participant.builder()
//                        .name("Unknown")
//                        .role("Unknown")
//                        .build());
//    }
//
//    /**
//     * Helper method to calculate implementation score
//     */
//    private double calculateImplementationScore(DecisionStatus status) {
//        switch (status) {
//            case IMPLEMENTED:
//                return 100.0;
//            case IN_PROGRESS:
//                return 75.0;
//            case PENDING:
//                return 50.0;
//            case BLOCKED:
//                return 25.0;
//            case SUPERSEDED:
//                return 0.0;
//            default:
//                return 50.0;
//        }
//    }
//
//    /**
//     * Helper method to check content similarity
//     */
//    private boolean hasContentSimilarity(String text1, String text2) {
//        // This is a simplified implementation
//        // In production, you might want to use more sophisticated
//        // text similarity algorithms (e.g., cosine similarity, Jaccard similarity)
//        Set<String> words1 = new HashSet<>(Arrays.asList(
//                normalizeText(text1).split("\\s+")
//        ));
//        Set<String> words2 = new HashSet<>(Arrays.asList(
//                normalizeText(text2).split("\\s+")
//        ));
//
//        Set<String> intersection = new HashSet<>(words1);
//        intersection.retainAll(words2);
//
//        return (double) intersection.size() /
//                Math.min(words1.size(), words2.size()) > 0.3;
//    }
//
//    /**
//     * Helper method to normalize text
//     */
//    private String normalizeText(String text) {
//        return text.toLowerCase()
//                .replaceAll("[^a-zA-Z0-9\\s]", "")
//                .replaceAll("\\s+", " ")
//                .trim();
//    }

}
