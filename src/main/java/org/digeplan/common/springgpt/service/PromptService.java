package org.digeplan.common.springgpt.service;

import org.digeplan.common.springgpt.model.DigetonPrompt;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

@Service
public class PromptService {
    private final Resource digetonPrompt;
    private final Resource oooPrompt;
    private final Resource oooDetailsPrompt;
    private final Resource oooSummaryPrompt;

    public PromptService(@Value("classpath:/prompts/digeton-prompt.txt")
                         Resource digetonPrompt,
                         @Value("classpath:/prompts/ooo-prompt.txt")
                         Resource oooPrompt,
                         @Value("classpath:/prompts/ooo-details-prompt.txt")
                         Resource oooDetailsPrompt,
                         @Value("classpath:/prompts/ooo-summary-prompt.txt")
                         Resource oooSummaryPrompt) {
        this.digetonPrompt = digetonPrompt;
        this.oooPrompt = oooPrompt;
        this.oooDetailsPrompt = oooDetailsPrompt;
        this.oooSummaryPrompt = oooSummaryPrompt;
    }

    public DigetonPrompt knowledgePrompt() {
        return new DigetonPrompt(digetonPrompt, OpenAiChatOptions.builder()
                .withFunction("userOutOfOffice")
                .build());
    }

    public DigetonPrompt oooPrompt() {
        return new DigetonPrompt(oooPrompt);
    }

    public DigetonPrompt oooDetailsPrompt() {
        return new DigetonPrompt(oooDetailsPrompt);
    }

    public DigetonPrompt oooSummaryPrompt() {
        return new DigetonPrompt(oooSummaryPrompt);
    }
}
