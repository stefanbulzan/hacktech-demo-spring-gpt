package org.digeplan.common.springgpt.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Description;
import org.springframework.stereotype.Component;

import java.util.function.Function;

@Slf4j
@Description("Get user out of office date")
@Component
@RequiredArgsConstructor
public class UserOutOfOfficeService implements Function<UserOutOfOfficeService.OOORequest, UserOutOfOfficeService.OOOResponse> {
    private final ChatGptService chatGptService;

    @Override
    public OOOResponse apply(OOORequest s) {
        log.info("Calling OOO Service for user: {}", s.user());
        return new OOOResponse(chatGptService.ooo(s.user()));
    }

    public record OOORequest(String user) {
    }

    public record OOOResponse(String outOfOffice) {
    }
}

