package com.example.apigatewayservice.config;

import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

//@Configuration
// yaml 설정파일 이용하지 않고 java코드로 filter 작성하기 - @Configuration으로 설정정보로 등록
public class FilterConfig {
//    @Bean
    public RouteLocator gatewayRoutes(RouteLocatorBuilder builder) {
        return builder.routes()
                .route(r -> r.path("/first-service/**") // 해당 path로 요청이 들어오면,
                        .filters(f -> f.addRequestHeader("first-request", "first-request-header-by-java") // 요청헤더 추가하여 요청 라우팅
                                .addResponseHeader("first-response", "first-response-header-from-java") // 응답헤더 추가하여 응답 라우팅
                        ) // filter를 이용하여 요청헤더와 응답헤더를 추가 가능하다.
                        .uri("http://localhost:8081")) // 도착지 설정
                .route(r -> r.path("/second-service/**")
                        .filters(f -> f.addRequestHeader("second-request", "second-request-header-by-java")
                                .addResponseHeader("second-response", "second-response-header-from-java"))
                        .uri("http://localhost:8082"))
                .build();
    }
}
