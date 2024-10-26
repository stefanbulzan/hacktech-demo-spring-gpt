package org.digeplan.common.springgpt.controller;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

import java.util.List;

@RestController
class KnowledgeController {

    private final ChatClient chatClient;
    @Value("classpath:/prompts/digeton-prompt.txt")
    private Resource promptText;

    public KnowledgeController(ChatClient.Builder builder, VectorStore vectorStore) {
        this.chatClient = builder
                .defaultAdvisors(new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
                .build();
    }

    @GetMapping("/knowledge")
    String getKnowledge(KnowledgeRequest request) {
        OpenAiChatOptions options = OpenAiChatOptions.builder()
                .withFunction("userOutOfOffice")
                .build();
        return chatClient.prompt()
                .user(request.question())
                .options(options)
                .call()
                .content();
    }

    @PostMapping("/knowledge")
    Flux<String> getKnowledgePost(@RequestBody KnowledgeRequest request) {
        return chatClient.prompt()
                .user(u -> {
                    u.text(promptText);
                    u.param("question", request.question);
                })
                .stream()
                .content();
    }

    private ResponseWithRefs convertToRefs(String response) {
        return new ResponseWithRefs(response, List.of("ref1", "ref2"));
    }

    record ResponseWithRefs(String content, List<String> refs) {

    }

    record KnowledgeRequest(String question, String name) {
    }
}


