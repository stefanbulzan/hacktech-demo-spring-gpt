package org.digeplan.common.springgpt.controller;

import lombok.RequiredArgsConstructor;
import org.digeplan.common.springgpt.model.KnowledgeRequest;
import org.digeplan.common.springgpt.model.OOODetails;
import org.digeplan.common.springgpt.service.ChatGptService;
import org.digeplan.common.springgpt.service.OOOService;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;

@RestController
@RequiredArgsConstructor
class KnowledgeController {
    private final ChatGptService chatGptService;
    private final OOOService oooService;

    @GetMapping("/knowledge")
    Flux<String> getKnowledge(KnowledgeRequest request) {
        return chatGptService.knowledge(request);
    }

    @PostMapping("/knowledge")
    Flux<String> getKnowledgePost(@RequestBody KnowledgeRequest request) {
        return chatGptService.knowledge(request);
    }

    @GetMapping("/knowledge/ooo")
    OOODetails getOOO(@RequestParam String name) {
        return oooService.oooDetails(name);
    }

    @GetMapping("/knowledge/ooo/summary")
    String getOOOSummary(@RequestParam String name) {
        return oooService.oooSummary(name);
    }
}
