package org.digeplan.common.springgpt;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class SpringGptApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpringGptApplication.class, args);
    }

}


@RestController
@RequestMapping("openapi")
class OpenApiController {
    ChatClient chatClient;

    public OpenApiController(ChatClient.Builder chatBuilder) {
        chatClient = chatBuilder.build();
    }

    @GetMapping
    String first() {
        return chatClient.prompt()
                .user("Who are you?")
                .call()
                .content();
    }
}
