package org.digeplan.common.springgpt.service;

import lombok.extern.slf4j.Slf4j;

import java.util.function.Function;

@Slf4j
public class UserOutOfOfficeService implements Function<UserOutOfOfficeService.Request, UserOutOfOfficeService.Response> {

    @Override
    public UserOutOfOfficeService.Response apply(UserOutOfOfficeService.Request s) {
        log.info("Get out of office date");
        return new Response("The current user was out of office from 2024-10-22T13:00:00 until 2024-10-24T13:00:00");
    }

    public record Request(String user) {
    }

    public record Response(String outOfOffice) {
    }
}
