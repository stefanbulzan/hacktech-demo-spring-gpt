package org.digeplan.common.springgpt.config;

import org.digeplan.common.springgpt.service.UserOutOfOfficeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.function.Function;

@Configuration
public class FunctionConfiguration {


    @Bean
    public Function<UserOutOfOfficeService.OOORequest, UserOutOfOfficeService.OOOResponse> userOutOfOffice(UserOutOfOfficeService oooService) {
        return oooService;
    }
}
