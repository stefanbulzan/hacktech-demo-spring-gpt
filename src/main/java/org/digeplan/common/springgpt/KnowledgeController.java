package org.digeplan.common.springgpt;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
class KnowledgeController {

    private final ChatClient chatClient;

    public KnowledgeController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
                .build();
    }

    @GetMapping("/knowledge")
    String getKnowledge(KnowledgeRequest request) {
        return chatClient.prompt()
                .user(request.question())
                .call()
                .content();
    }

    @PostMapping("/knowledge")
    Flux<String> getKnowledgePost(@RequestBody KnowledgeRequest request) {
        return chatClient.prompt()
                .user(request.question())
                .stream()
                .content();
    }

    record KnowledgeRequest(String question) {
    }
}


