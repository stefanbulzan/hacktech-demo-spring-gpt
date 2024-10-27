package org.digeplan.common.springgpt.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.digeplan.common.springgpt.model.OOODetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OOOService {
    private final ChatGptService chatGptService;
    private final ObjectMapper objectMapper;

    @SneakyThrows
    public OOODetails oooDetails(String user) {
        return objectMapper.readValue(chatGptService.oooDetails(user), OOODetails.class);
    }

    public String oooSummary(String user) {
        return chatGptService.oooSummary(user);
    }
}
