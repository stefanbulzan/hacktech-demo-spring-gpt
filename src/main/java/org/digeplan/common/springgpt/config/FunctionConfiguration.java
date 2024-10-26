package org.digeplan.common.springgpt.config;

import org.digeplan.common.springgpt.service.UserOutOfOfficeService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;

import java.util.function.Function;

@Configuration
public class FunctionConfiguration {


    @Bean
    @Description("Get user out of office date")
    public Function<UserOutOfOfficeService.Request, UserOutOfOfficeService.Response> userOutOfOffice() {
        return new UserOutOfOfficeService();
    }

}
