package com.example.secondservice;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/second-service")
@RequiredArgsConstructor
@Slf4j
public class SecondServiceController {
    private final Environment env;

    @GetMapping("/welcome")
    public String welcome() {
        return "Welcome to the second service";
    }

    @GetMapping("/message")
    public String message(@RequestHeader(value = "second-request", defaultValue = "Unknown") String header) {
        // @RequestHeader(value = "second-request", defaultValue = "Unknown") String header
        // second-request 헤더가 없을 경우 기본 값으로 "Unknown"을 사용
        log.info(header);
        return "Hello in second service with header.";
    }

    @GetMapping("/check")
    public String check(HttpServletRequest request) {
        log.info("Server port={}", request.getServerPort());

        return String.format("This is a message from second service on PORT %s."
                , env.getProperty("local.server.port")); // 환경변수에 등록된 포트 가져옴

    }
}
