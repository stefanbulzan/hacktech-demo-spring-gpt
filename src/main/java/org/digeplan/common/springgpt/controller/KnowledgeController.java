package org.digeplan.common.springgpt.controller;

import lombok.RequiredArgsConstructor;
import org.digeplan.common.springgpt.model.KnowledgeRequest;
import org.digeplan.common.springgpt.service.ChatGptService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
class KnowledgeController {
    private final ChatGptService chatGptService;

    @GetMapping("/knowledge")
    Flux<String> getKnowledge(KnowledgeRequest request) {
        return chatGptService.knowledge(request);
    }

    @PostMapping("/knowledge")
    Flux<String> getKnowledgePost(@RequestBody KnowledgeRequest request) {
        return chatGptService.knowledge(request);
    }
}
