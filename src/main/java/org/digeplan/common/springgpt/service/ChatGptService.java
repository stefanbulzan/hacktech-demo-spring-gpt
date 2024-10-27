package org.digeplan.common.springgpt.service;

import org.digeplan.common.springgpt.model.DigetonPrompt;
import org.digeplan.common.springgpt.model.KnowledgeRequest;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.client.advisor.QuestionAnswerAdvisor;
import org.springframework.ai.chat.memory.InMemoryChatMemory;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;

@Service

public class ChatGptService {
    private final ChatClient chatClient;
    private final PromptService promptService;

    public ChatGptService(
            ChatClient.Builder builder,
            VectorStore vectorStore,
            PromptService promptService) {
        this.chatClient = builder
                .defaultAdvisors(
                        new MessageChatMemoryAdvisor(new InMemoryChatMemory()),
                        new QuestionAnswerAdvisor(vectorStore, SearchRequest.defaults()))
                .build();
        this.promptService = promptService;
    }


    public Flux<String> knowledge(KnowledgeRequest request) {
        DigetonPrompt digetonPrompt = promptService.knowledgePrompt();
        return chatClient.prompt()
                .user(u -> {
                    u.text(digetonPrompt.prompt());
                    u.param("question", request.question());
                })
                .options(digetonPrompt.options())
                .stream()
                .content();
    }

    public String ooo(String user) {
        DigetonPrompt digetonPrompt = promptService.oooPrompt();
        return chatClient.prompt()
                .user(u -> {
                    u.text(digetonPrompt.prompt());
                    u.param("user", user);
                })
                .call()
                .content();
    }
}
