# Catalog Microservice
> Catalog 서비스 프로젝트 설정 및 구현

## data.sql 이용한 데이터 초기화
* Spring Boot 2.5버전 이후부터 hibernate 초기화 이전에 data.sql이 실행되도록 바뀜
* 설정 정보에서 옵션 추가로 해결
   * jpa.defer-datasource-initialization: true
     * hibernate 초기화 이후 data.sql가 실행되도록 변경하는 설정
* 설정 정보 예시
  ```yaml
  spring:
  application:
    name: catalog-service
  jpa:
    hibernate:
      ddl-auto: create-drop
    show-sql: true # sql 쿼리 로그로 보기
    generate-ddl: true # true일 경우 ddl-auto 설정을 활성화, false일 경우 비활성화
    database: h2
    defer-datasource-initialization: true #hibernate 초기화 이후 data.sql 실행되도록 함
  h2:
    console:
      enabled: true
      settings:
        web-allow-others: true
      path: /h2-console
  datasource:
    driver-class-name: org.h2.Driver
    url: jdbc:h2:mem:testdb
    username: sa
    password:
  ```

## @ColumnDefault(value = "CURRENT_TIMESTAMP")
* hibernate 어노테이션
* `@ColumnDefault(value = "")` : 컬럼의 default값을 설정할 수 있음
* `value = "CURRENT_TIMESTAMP"` 옵션 : 현재 시간을 기준으로 데이터 초기화
* 사용 예시
  ```java
  public class TestEntity {
    // ...

    @Column(nullable = false, updatable = false, insertable = false) // 초기화된 값 계속 사용하도록 설정
    @ColumnDefault(value = "CURRENT_TIMESTAMP")
    private Date createdAt;
  }
  ```
