package com.example.userservice.security;

import com.example.userservice.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.security.web.util.matcher.IpAddressMatcher;

import java.util.function.Supplier;

// Note: 설정 정보 클래스 이므로 아래의 내용들 빈 등록 가능(UserService, BCryptPasswordEncoder, Environment, SecurityFilterChain)
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class WebSecurity {
    private final UserService userService;
    private final BCryptPasswordEncoder bCryptPasswordEncoder; // UserServiceApplication 클래스에서 등록한 Bean
    private final Environment env;

    public static final String ALLOWED_IP_ADDRESS = "127.0.0.1";
    public static final String SUBNET = "/32";
    public static final IpAddressMatcher ALLOWED_IP_ADDRESS_MATCHER = new IpAddressMatcher(ALLOWED_IP_ADDRESS + SUBNET);

    @Bean // Note: 권한과 관련된 필터 체인 설정
    protected SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        // Note: 인증에 관련된 작업은 아래의 AuthenticationManager를 통해서 이루어 짐
        AuthenticationManagerBuilder authenticationManagerBuilder = http.getSharedObject(AuthenticationManagerBuilder.class);

        // 매니저에 등록할 UserService는 UserDetailsService를 상속받은 클래스여야 함
        // UserService 구현체에는 loadUserByUsername 함수를 오버라이딩하여 유저 객체를 불러올 수 있음
        // 인증시에 유저가 입력한 패스워드는 아래의 인코더에 의해 변환된 값(암호화된 패스워드)으로 서비스 로직을 수행함 -> 유저 엔티티 생성시 암호화된 패스워드를 저장했기 때문에 비교 가능
        authenticationManagerBuilder.userDetailsService(userService).passwordEncoder(bCryptPasswordEncoder);
        AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

        http.csrf((csrf) -> csrf.disable());
//                .cors(cors -> cors.disable());

        http.authorizeHttpRequests((authz) -> authz // HTTP 요청에 대한 인가 설정
//                                .requestMatchers(new AntPathRequestMatcher("/actuator/**")).permitAll() // 해당 경로는 모든 사용자에게 허용
                                .requestMatchers(new AntPathRequestMatcher("/h2-console/**")).permitAll()
//                                .requestMatchers(new AntPathRequestMatcher("/users", "POST")).permitAll()
//                                .requestMatchers(new AntPathRequestMatcher("/welcome")).permitAll()
                                .requestMatchers(new AntPathRequestMatcher("/health-check")).permitAll()
//                                .requestMatchers(new AntPathRequestMatcher("/swagger-ui/**")).permitAll()
//                                .requestMatchers(new AntPathRequestMatcher("/swagger-resources/**")).permitAll()
//                                .requestMatchers(new AntPathRequestMatcher("/v3/apidocs/**")).permitAll()
//                                .requestMatchers("/**").access(this::hasIpAddress)-
                                .requestMatchers("/**") // 위의 허용 내용을 제외한 모든 접근은 권한이 있어야 접근이 가능함(=접근 허용 x)
                                .access(new WebExpressionAuthorizationManager(
                                        "hasIpAddress('127.0.0.1') or hasIpAddress('::1') or hasIpAddress('125.132.101.231')"
                                ))
//                                .access(new WebExpressionAuthorizationManager("hasIpAddress('::1')")) // '::1'은 IPv6의 루프백 주소로, IPv4의 127.0.0.1과 동일
                                .anyRequest().authenticated() // 위에서 설정된 리소스를 제외한 모든 리소스들은 인증을 해야함
                )
                .authenticationManager(authenticationManager)
                .sessionManagement((session) -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS));

        http.addFilter(getAuthenticationFilter(authenticationManager));
        http.headers((headers) -> headers.frameOptions((frameOptions) -> frameOptions.sameOrigin()));

        return http.build();
    }

    private AuthorizationDecision hasIpAddress(Supplier<Authentication> authentication, RequestAuthorizationContext object) {
        return new AuthorizationDecision(ALLOWED_IP_ADDRESS_MATCHER.matches(object.getRequest()));
    }

    private AuthenticationFilter getAuthenticationFilter(AuthenticationManager authenticationManager) throws Exception {
        return new AuthenticationFilter(authenticationManager, userService, env); // 미리 구현해 둔 AuthenticationFilter 인스턴스 생성
    }
}