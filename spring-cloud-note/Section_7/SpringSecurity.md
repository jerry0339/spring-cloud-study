# Spring Security
* Spring Security의 실제 동작은 Servlet Filter (`javax.servlet.Filter` 인터페이스 구현체) 를 통해 이루어 짐
* Spring Security는 필터의 집합체
* Spring Security는 Servlet Filter 레벨에서 인증 및 인가를 처리하기 위한 프레임워크

<br>

## Spring Security 사용하는 이유?
* 인증과 인가 기능을 표준화하고 자동화할 수 있어
* 개발자가 보안 관련 코드를 직접 작성하지 않고도 높은 수준의 보안 기능을 쉽게 구현할 수 있음
* 이를 통해 핵심 비즈니스 로직 개발에 집중할 수 있다는 장점이 있음 (개발 및 유지보수 복잡성 낮춰줌)
* 인증, 인가, 세션 관리, CSRF 방어, XSS 방어 등의 보안 기능을 제공함

<br>

## DelegatingFilterProxy
* Spring Security는 Servlet Filter로 동작함
* 하지만 Servlet Filter는 Servlet Container에서 직접 관리되기 때문에,
* Spring IoC Container에서 관리되는 스프링 빈을 주입받지 못해 Spring의 여러 기능을 활용할 수 없음
* 여기서 이를 가능하게 해주는 것이 `DelegatingFilterProxy`임
  * Servlet Container와 Spring IoC Container 사이를 **연결해 주는 역할**을 함
* 자세한 동작 방식 설명
  * Spring Framework에서는 DelegatingFilterProxy를 사용하여 필터의 관리를 Spring의 ApplicationContext로 위임한다고 표현
  * 클라이언트의 요청이 들어오면 DelegatingFilterProxy가 요청을 가로채서 등록된 필터 Bean을 찾아서 실행하는 방식
  * 즉, DelegatingFilterProxy는 서블릿 필터의 대리자 역할을 수행하여 Spring ApplicationContext 내에서 등록된 필터 빈을 실행시키는 역할을 함
  * 이를 통해 Spring의 의존성 주입(Dependency Injection)이 가능하며, 필터가 Bean으로 선언되어 다른 Spring Container내에서 동작이 가능함

<br>

## SecurityFilterChain
* Spring Security에서 사용하는 필터 체인으로 인증과 인가에 대한 여러 필터들이 정의될 수 있음
* DelegatingFilterProxy를 통해 SecurityFilterChain이라는 Bean을 사용가능
* [공식 문서 참고](https://docs.spring.io/spring-security/reference/servlet/architecture.html)
* ![](2024-11-15-03-23-27.png)
* SecurityFilterChain 빈 등록 예시
    ```java
    // SecurityFilterChain 빈 등록 예시
    @Configuration
    @EnableWebSecurity
    public class SecurityConfig {

        @Bean
        public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                .csrf(Customizer.withDefaults())
                .authorizeHttpRequests(authorize -> authorize
                    .anyRequest().authenticated()
                )
                .httpBasic(Customizer.withDefaults())
                .formLogin(Customizer.withDefaults());
            return http.build();
        }

    }
    ```

* SecurityFilterChain 빈 및 Custom Filter 등록하는 방법 예시
  * Custom Filter를 SecurityFilterChain에 등록하고 싶다면,
  * @Component를 통해 WAS의 FilterChain에 등록할 것이 아니라, 다음과 같이 SecurityFilterChain에 등록해야 한다.
    ```java
    @Configuration
    @EnableWebSecurity
    public class SecurityConfig {

        @Bean
        SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
            http
                // ...
                .addFilterBefore(new TenantFilter(), AuthorizationFilter.class); 
            return http.build();
        }
    }
    ```
    ```java
    // @Component이용하여 WAS의 FilterChain에 등록하면 안됨!
    public class TenantFilter implements Filter {

        @Override
        public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
            HttpServletRequest request = (HttpServletRequest) servletRequest;
            HttpServletResponse response = (HttpServletResponse) servletResponse;

            String tenantId = request.getHeader("X-Tenant-Id"); 
            boolean hasAccess = isUserAllowed(tenantId); 
            if (hasAccess) {
                filterChain.doFilter(request, response); 
                return;
            }
            throw new AccessDeniedException("Access denied"); 
        }

    }
    ```

<br>
