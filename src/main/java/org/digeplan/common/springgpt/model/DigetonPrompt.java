package org.digeplan.common.springgpt.model;

import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.core.io.Resource;

public record DigetonPrompt(
        Resource prompt,
        OpenAiChatOptions options
) {
    public DigetonPrompt(Resource prompt) {
        this(prompt, null);
    }
}
