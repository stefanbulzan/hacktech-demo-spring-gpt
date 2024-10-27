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

    public PromptService(@Value("classpath:/prompts/digeton-prompt.txt")
                         Resource digetonPrompt,
                         @Value("classpath:/prompts/ooo-prompt.txt")
                         Resource oooPrompt) {
        this.digetonPrompt = digetonPrompt;
        this.oooPrompt = oooPrompt;
    }

    public DigetonPrompt knowledgePrompt() {
        return new DigetonPrompt(digetonPrompt, OpenAiChatOptions.builder()
                .withFunction("userOutOfOffice")
                .build());
    }

    public DigetonPrompt oooPrompt() {
        return new DigetonPrompt(oooPrompt);
    }
}
