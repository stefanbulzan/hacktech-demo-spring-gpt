package org.digeplan.common.springgpt;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.messages.UserMessage;
import org.springframework.ai.chat.prompt.Prompt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("openapi")
public class OpenAiController {
    private final ChatClient chatClient;

    public OpenAiController(ChatClient.Builder chatBuilder) {
        chatClient = chatBuilder.build();
    }

    @GetMapping
    public String tellAJoke() {
        return chatClient.prompt(new Prompt(List.of(new UserMessage("Tell me a dark joke about programmers"))))
                .call()
                .content();
    }
}
