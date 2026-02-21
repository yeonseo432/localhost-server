# Spring Boot + Kotlin 프로젝트 구조 가이드

> 이 문서를 참고하여 동일한 아키텍처의 새 프로젝트를 세팅할 수 있습니다.
> Claude에게 이 파일을 읽게 한 후 "이 구조대로 새 프로젝트를 만들어줘"라고 요청하세요.

---

## 1. 기술 스택

| 항목 | 기술 | 버전 |
|------|------|------|
| 언어 | Kotlin | 2.2.21 |
| JDK | Temurin | 21 |
| 프레임워크 | Spring Boot | 4.0.1 |
| 빌드 도구 | Gradle (Kotlin DSL) | - |
| DB | MySQL | 8.4 |
| 캐시 | Redis | 7 |
| ORM | Spring Data JPA | - |
| DB 마이그레이션 | Flyway | - |
| 인증 | JWT (jjwt 0.12.5) | - |
| OAuth | Spring Security OAuth2 Client (Kakao, Google) | - |
| API 문서 | springdoc-openapi (Swagger UI) 2.8.3 | - |
| 파일 저장소 | AWS S3 (SDK v2) | 2.25.0 |
| 코드 품질 | ktlint | 13.1.0 |
| 테스트 | JUnit 5 + TestContainers | - |
| 컨테이너 | Docker + Kubernetes (k3s) | - |
| CI/CD | GitHub Actions | - |

---

## 2. 프로젝트 디렉토리 구조

```
project-root/
├── build.gradle.kts
├── settings.gradle.kts
├── Dockerfile
├── docker-compose.yaml
├── .github/
│   └── workflows/
│       └── workflow.yml              # CI/CD 파이프라인
├── k8s/
│   └── backend.yaml                  # Kubernetes Deployment + Service
├── src/
│   ├── main/
│   │   ├── kotlin/com/{group}/{project}/
│   │   │   ├── {Project}Application.kt       # 메인 애플리케이션
│   │   │   ├── config/                        # 설정 클래스들
│   │   │   ├── common/                        # 공통 유틸리티
│   │   │   └── {도메인별 패키지}/              # 도메인 모듈들
│   │   └── resources/
│   │       ├── application.yaml               # 공통 설정
│   │       ├── application-local.yaml         # 로컬 개발 설정
│   │       ├── application-prod.yaml          # 프로덕션 설정
│   │       └── db/migration/                  # Flyway SQL 파일
│   └── test/
│       └── kotlin/com/{group}/{project}/
│           ├── TestcontainersConfiguration.kt # 테스트 컨테이너 설정
│           ├── Test{Project}Application.kt    # 테스트 진입점
│           └── {도메인}ControllerTest.kt      # 통합 테스트
└── .env                                       # Docker Compose 환경변수 (gitignore)
```

---

## 3. 빌드 설정

### settings.gradle.kts

```kotlin
rootProject.name = "{project-name}"
```

### build.gradle.kts

```kotlin
plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "4.0.1"
    id("io.spring.dependency-management") version "1.1.7"
    kotlin("plugin.jpa") version "2.2.21"
    id("org.jlleitschuh.gradle.ktlint") version "13.1.0"
}

group = "com.{group}"
version = "0.0.1-SNAPSHOT"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // === Spring Boot 핵심 ===
    implementation("org.springframework.boot:spring-boot-starter-webmvc")
    implementation("org.springframework.boot:spring-boot-starter-data-jpa")
    implementation("org.springframework.boot:spring-boot-starter-data-jdbc")
    implementation("org.springframework.boot:spring-boot-starter-data-redis")
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("org.springframework.boot:spring-boot-starter-security-oauth2-client")
    implementation("org.springframework.boot:spring-boot-starter-validation")
    implementation("org.springframework.boot:spring-boot-starter-flyway")
    implementation("org.springframework.boot:spring-boot-h2console")
    implementation("org.flywaydb:flyway-mysql")

    // === Kotlin ===
    implementation("org.jetbrains.kotlin:kotlin-reflect")
    implementation("tools.jackson.module:jackson-module-kotlin")

    // === JWT ===
    implementation("io.jsonwebtoken:jjwt-api:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.12.5")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.12.5")

    // === API 문서 (Swagger) ===
    implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.8.3")

    // === AWS S3 ===
    implementation(platform("software.amazon.awssdk:bom:2.25.0"))
    implementation("software.amazon.awssdk:s3")

    // === DB 드라이버 ===
    runtimeOnly("com.mysql:mysql-connector-j")
    runtimeOnly("com.h2database:h2") // 로컬 개발용

    // === 테스트 ===
    testImplementation("org.springframework.boot:spring-boot-starter-webmvc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jpa-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-jdbc-test")
    testImplementation("org.springframework.boot:spring-boot-starter-data-redis-test")
    testImplementation("org.springframework.boot:spring-boot-starter-flyway-test")
    testImplementation("org.springframework.boot:spring-boot-starter-validation-test")
    testImplementation("org.springframework.boot:spring-boot-starter-security-oauth2-client-test")
    testImplementation("org.springframework.boot:spring-boot-testcontainers")
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")
    testImplementation("org.testcontainers:testcontainers-junit-jupiter")
    testImplementation("org.testcontainers:testcontainers-mysql")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xannotation-default-target=param-property")
    }
}

// JPA Entity 클래스들을 open으로 만들어주는 설정
allOpen {
    annotation("jakarta.persistence.Entity")
    annotation("jakarta.persistence.MappedSuperclass")
    annotation("jakarta.persistence.Embeddable")
}

tasks.withType<Test> {
    useJUnitPlatform()
}
```

---

## 4. 메인 애플리케이션

### {Project}Application.kt

```kotlin
package com.{group}.{project}

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@ConfigurationPropertiesScan
@EnableScheduling
class {Project}Application

fun main(args: Array<String>) {
    runApplication<{Project}Application>(*args)
}
```

---

## 5. config 패키지 (설정 클래스들)

### 5-1. SecurityConfig.kt — Spring Security + JWT

```kotlin
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
) {
    @Bean
    fun passwordEncoder(): PasswordEncoder = BCryptPasswordEncoder()

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .exceptionHandling { exception ->
                exception.authenticationEntryPoint { _, response, _ ->
                    response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Unauthorized")
                }
            }
            .authorizeHttpRequests { auth ->
                auth
                    // 관리자 전용
                    .requestMatchers("/api/admin/**").hasRole("ADMIN")
                    // 인증 불필요
                    .requestMatchers(
                        "/api/auth/signup", "/api/auth/login",
                        "/swagger-ui/**", "/v3/api-docs/**",
                    ).permitAll()
                    // 나머지는 인증 필요
                    .anyRequest().authenticated()
            }
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
        return http.build()
    }

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val config = CorsConfiguration()
        config.allowedOrigins = listOf("http://localhost:3000", "http://localhost:5173")
        config.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS")
        config.allowedHeaders = listOf("*")
        config.allowCredentials = true
        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", config)
        return source
    }
}
```

### 5-2. JwtAuthenticationFilter.kt — JWT 토큰 검증 필터

```kotlin
@Component
class JwtAuthenticationFilter(
    private val jwtTokenProvider: JwtTokenProvider,
    private val tokenBlacklistService: TokenBlacklistService,
) : OncePerRequestFilter() {

    override fun doFilterInternal(
        request: HttpServletRequest,
        response: HttpServletResponse,
        filterChain: FilterChain,
    ) {
        try {
            val token = extractTokenFromRequest(request)
            if (token != null && jwtTokenProvider.validateToken(token)) {
                if (!tokenBlacklistService.isBlacklisted(token)) {
                    val userId = jwtTokenProvider.getUserIdFromToken(token)
                    val role = jwtTokenProvider.getRoleFromToken(token)
                    val authorities = listOf(SimpleGrantedAuthority("ROLE_$role"))
                    val authentication = UsernamePasswordAuthenticationToken(userId, null, authorities)
                    authentication.details = WebAuthenticationDetailsSource().buildDetails(request)
                    SecurityContextHolder.getContext().authentication = authentication
                }
            }
        } catch (e: Exception) {
            logger.error("JWT 토큰 검증 실패: ${e.message}")
        }
        filterChain.doFilter(request, response)
    }

    private fun extractTokenFromRequest(request: HttpServletRequest): String? {
        val bearerToken = request.getHeader("Authorization")
        return if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            bearerToken.substring(7)
        } else null
    }
}
```

### 5-3. JpaConfig.kt — JPA Auditing 활성화

```kotlin
@Configuration
@EnableJpaAuditing
class JpaConfig
```

### 5-4. WebMvcConfig.kt — 커스텀 ArgumentResolver 등록

```kotlin
@Configuration
class WebMvcConfig(
    private val loggedInUserIdResolver: LoggedInUserIdResolver,
) : WebMvcConfigurer {
    override fun addArgumentResolvers(resolvers: MutableList<HandlerMethodArgumentResolver>) {
        resolvers.add(loggedInUserIdResolver)
    }
}
```

### 5-5. OpenApiConfig.kt — Swagger UI 설정

```kotlin
@Configuration
class OpenApiConfig {
    @Bean
    fun openApi(): OpenAPI {
        val securitySchemeName = "Bearer Authentication"
        return OpenAPI()
            .info(
                Info()
                    .title("{Project} API")
                    .description("API 문서")
                    .version("v1.0.0"),
            )
            .components(
                Components()
                    .addSecuritySchemes(
                        securitySchemeName,
                        SecurityScheme()
                            .type(SecurityScheme.Type.HTTP)
                            .scheme("bearer")
                            .bearerFormat("JWT"),
                    ),
            )
    }
}
```

### 5-6. PropertiesConfig.kt

```kotlin
@Configuration
@ConfigurationPropertiesScan(basePackages = ["com.{group}.{project}"])
class PropertiesConfig
```

---

## 6. common 패키지 (공통 인프라)

### 6-1. common/auth/LoggedInUserId.kt — 커스텀 어노테이션

```kotlin
@Target(AnnotationTarget.VALUE_PARAMETER)
@Retention(AnnotationRetention.RUNTIME)
annotation class LoggedInUserId
```

### 6-2. common/auth/LoggedInUserIdResolver.kt

```kotlin
@Component
class LoggedInUserIdResolver : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean =
        parameter.hasParameterAnnotation(LoggedInUserId::class.java)

    override fun resolveArgument(
        parameter: MethodParameter,
        mavContainer: ModelAndViewContainer?,
        webRequest: NativeWebRequest,
        binderFactory: WebDataBinderFactory?,
    ): Long {
        val authentication = SecurityContextHolder.getContext().authentication
            ?: throw UnauthorizedException("인증 정보를 찾을 수 없습니다")
        return when (val principal = authentication.principal) {
            is Long -> principal
            is String -> principal.toLongOrNull()
                ?: throw UnauthorizedException("유효하지 않은 사용자 ID 형식")
            else -> throw UnauthorizedException("지원하지 않는 principal 타입")
        }
    }
}
```

### 6-3. common/exception/BusinessException.kt — 커스텀 예외 계층

```kotlin
// sealed class로 모든 비즈니스 예외를 컴파일러가 체크
sealed class BusinessException(
    override val message: String,
    val errorCode: String,
) : RuntimeException(message)

class ResourceNotFoundException(message: String)
    : BusinessException(message, "RESOURCE_NOT_FOUND")

class BadRequestException(message: String)
    : BusinessException(message, "BAD_REQUEST")

class ResourceForbiddenException(message: String)
    : BusinessException(message, "RESOURCE_FORBIDDEN")

class UnauthorizedException(message: String)
    : BusinessException(message, "UNAUTHORIZED")

class DuplicateEmailException(email: String)
    : BusinessException("이미 사용 중인 이메일입니다: $email", "DUPLICATE_EMAIL")
```

### 6-4. common/exception/GlobalExceptionHandler.kt — 전역 예외 처리

```kotlin
@Schema(description = "에러 응답")
data class ErrorResponse(
    val timestamp: LocalDateTime = LocalDateTime.now(),
    val status: Int,
    val error: String,
    val message: String,
    val errorCode: String? = null,
    val validationErrors: Map<String, String?>? = null,
)

@RestControllerAdvice
class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException::class)
    fun handleNotFound(e: ResourceNotFoundException): ResponseEntity<ErrorResponse> { /* 404 */ }

    @ExceptionHandler(BadRequestException::class)
    fun handleBadRequest(e: BadRequestException): ResponseEntity<ErrorResponse> { /* 400 */ }

    @ExceptionHandler(UnauthorizedException::class)
    fun handleUnauthorized(e: UnauthorizedException): ResponseEntity<ErrorResponse> { /* 401 */ }

    @ExceptionHandler(ResourceForbiddenException::class)
    fun handleForbidden(e: ResourceForbiddenException): ResponseEntity<ErrorResponse> { /* 403 */ }

    @ExceptionHandler(DuplicateEmailException::class)
    fun handleConflict(e: DuplicateEmailException): ResponseEntity<ErrorResponse> { /* 409 */ }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleValidation(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> { /* 400 */ }

    @ExceptionHandler(Exception::class)
    fun handleUnexpected(e: Exception): ResponseEntity<ErrorResponse> { /* 500 */ }
}
```

### 6-5. common/dto/PageInfo.kt — 페이지네이션 DTO

```kotlin
data class PageInfo(
    val page: Int,
    val size: Int,
    val totalElements: Long,
    val totalPages: Int,
    val hasNext: Boolean,
)
```

### 6-6. common/extension/NullSafetyExtensions.kt

```kotlin
fun <T> T?.ensureNotNull(message: String = "데이터가 존재하지 않습니다"): T =
    this ?: throw ResourceNotFoundException(message)
```

### 6-7. common/time/TimeProvider.kt — 시간 추상화 (테스트 용이)

```kotlin
interface TimeProvider {
    fun currentTimeMillis(): Long
}

@Component
class SystemTimeProvider : TimeProvider {
    override fun currentTimeMillis(): Long = System.currentTimeMillis()
}
```

---

## 7. 도메인 모듈 패키지 구조 (핵심 패턴)

각 도메인(기능 영역)은 아래와 같은 하위 패키지로 구성합니다:

```
{도메인}/
├── model/           # JPA Entity 클래스
├── repository/      # Spring Data JPA Repository 인터페이스
├── service/         # 비즈니스 로직
├── controller/      # REST API 엔드포인트
├── dto/             # Request/Response DTO
└── (필요 시) util/, enum/, config/
```

### 예시: user 도메인

```
user/
├── model/
│   ├── User.kt              # @Entity 사용자
│   ├── LocalCredential.kt   # 이메일+비밀번호 인증 정보
│   ├── SocialCredential.kt  # OAuth 인증 정보
│   ├── UserRole.kt          # enum: USER, ADMIN
│   └── BaseEntity.kt        # @MappedSuperclass (createdAt, updatedAt)
├── repository/
│   ├── UserRepository.kt
│   ├── LocalCredentialRepository.kt
│   └── SocialCredentialRepository.kt
├── service/
│   ├── AuthService.kt           # 회원가입, 로그인 로직
│   ├── MyPageService.kt         # 프로필 관리
│   ├── TokenBlacklistService.kt # Redis JWT 블랙리스트
│   └── S3Service.kt             # 프로필 이미지 업로드
├── controller/
│   ├── AuthController.kt        # /api/auth/**
│   └── MyPageController.kt      # /api/mypage/**
├── dto/
│   ├── SignupRequest.kt
│   ├── SignupResponse.kt
│   ├── LoginRequest.kt
│   ├── LoginResponse.kt
│   └── MyPageResponse.kt
└── JwtTokenProvider.kt          # JWT 토큰 생성/검증
```

---

## 8. Entity 작성 패턴

### BaseEntity — 모든 엔티티의 부모 클래스

```kotlin
@MappedSuperclass
@EntityListeners(AuditingEntityListener::class)
abstract class BaseEntity(
    @CreatedDate
    @Column(nullable = false, updatable = false)
    var createdAt: LocalDateTime = LocalDateTime.now(),

    @LastModifiedDate
    @Column(nullable = false)
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)
```

### User Entity 예시

```kotlin
@Entity
@Table(name = "users")
class User(
    @Column(nullable = false, length = 50)
    var nickname: String,

    @Column(length = 255)
    var profileImageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var role: UserRole = UserRole.USER,

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long? = null,
) : BaseEntity()
```

### JwtTokenProvider

```kotlin
@Component
class JwtTokenProvider(
    @Value("\${jwt.secret}") private val secretKey: String,
    @Value("\${jwt.expiration-in-ms}") private val expirationInMs: Long,
) {
    private val key: SecretKey by lazy { Keys.hmacShaKeyFor(secretKey.toByteArray()) }

    fun createToken(userId: Long?, role: String): String {
        val now = Date()
        return Jwts.builder()
            .subject(userId.ensureNotNull().toString())
            .claim("role", role)
            .issuedAt(now)
            .expiration(Date(now.time + expirationInMs))
            .signWith(key)
            .compact()
    }

    fun getUserIdFromToken(token: String): Long = parseClaims(token).subject.toLong()
    fun getRoleFromToken(token: String): String = parseClaims(token)["role"] as? String ?: "USER"
    fun validateToken(token: String): Boolean = try { parseClaims(token); true } catch (e: Exception) { false }

    private fun parseClaims(token: String): Claims =
        Jwts.parser().verifyWith(key).build().parseSignedClaims(token).payload
}
```

---

## 9. 설정 파일

### application.yaml (공통)

```yaml
spring:
  application:
    name: {project-name}
  datasource:
    driver-class-name: com.mysql.cj.jdbc.Driver
  jpa:
    properties:
      hibernate:
        format_sql: true
  flyway:
    enabled: true
  servlet:
    multipart:
      max-file-size: 20MB
      max-request-size: 20MB
```

### application-local.yaml (개발)

```yaml
spring:
  config:
    activate:
      on-profile: local
  datasource:
    url: jdbc:mysql://localhost:3306/{db_name}?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=Asia/Seoul&characterEncoding=utf8
    username: user
    password: password
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
  data:
    redis:
      host: localhost
      port: 6379
      password:

jwt:
  secret: local-secret-key-must-be-very-long-for-security
  expiration-in-ms: 3600000

server:
  port: 8080
```

### application-prod.yaml (프로덕션)

```yaml
spring:
  config:
    activate:
      on-profile: prod
  datasource:
    url: ${SPRING_DATASOURCE_URL}
    username: ${SPRING_DATASOURCE_USERNAME}
    password: ${SPRING_DATASOURCE_PASSWORD}
  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: false
  data:
    redis:
      host: ${SPRING_REDIS_HOST}
      port: ${SPRING_REDIS_PORT}
      password: ${SPRING_REDIS_PASSWORD}

jwt:
  secret: ${JWT_SECRET}
  expiration-in-ms: 86400000

server:
  port: 8080
```

---

## 10. DB 마이그레이션 (Flyway)

파일 위치: `src/main/resources/db/migration/`

네이밍 규칙: `V{번호}__{설명}.sql`

```
db/migration/
├── V1__create_users_table.sql
├── V2__create_local_credentials_table.sql
├── V3__create_social_credentials_table.sql
└── ...
```

### 예시: V1__create_users_table.sql

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    nickname VARCHAR(50) NOT NULL,
    profile_image_url VARCHAR(255),
    role VARCHAR(20) NOT NULL DEFAULT 'USER',
    created_at DATETIME(6) NOT NULL,
    updated_at DATETIME(6) NOT NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;
```

---

## 11. Docker

### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre
WORKDIR /app
COPY build/libs/*.jar app.jar
ENV TZ=Asia/Seoul
ENTRYPOINT ["java", "-jar", "app.jar"]
```

### docker-compose.yaml

```yaml
services:
  mysql:
    image: mysql:8.4
    container_name: mysql-db
    ports:
      - "3306:3306"
    environment:
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
      MYSQL_DATABASE: ${MYSQL_DATABASE}
      MYSQL_USER: ${MYSQL_USER}
      MYSQL_PASSWORD: ${MYSQL_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql

  redis:
    image: redis:7-alpine
    container_name: redis-cache
    ports:
      - "6379:6379"
    volumes:
      - redis_data:/data
    command: redis-server --notify-keyspace-events Ex

volumes:
  mysql_data:
  redis_data:
```

### .env (docker-compose용, gitignore 추가)

```
MYSQL_ROOT_PASSWORD=rootpassword
MYSQL_DATABASE=mydb
MYSQL_USER=user
MYSQL_PASSWORD=password
```

---

## 12. 테스트 설정 (TestContainers)

### TestcontainersConfiguration.kt

```kotlin
@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {
    @Bean
    @ServiceConnection
    fun mysqlContainer(): MySQLContainer<*> =
        MySQLContainer(DockerImageName.parse("mysql:latest"))

    @Bean
    @ServiceConnection(name = "redis")
    fun redisContainer(): GenericContainer<*> =
        GenericContainer(DockerImageName.parse("redis:latest"))
            .withExposedPorts(6379)
            .withCommand("redis-server", "--notify-keyspace-events", "Ex")
}
```

### Test{Project}Application.kt

```kotlin
fun main(args: Array<String>) {
    fromApplication<{Project}Application>()
        .with(TestcontainersConfiguration::class)
        .run(*args)
}
```

---

## 13. CI/CD (GitHub Actions)

### .github/workflows/workflow.yml

```yaml
name: Java CI & CD to k3s

on:
  push:
    branches: ["main"]
  pull_request:
    branches: ["main"]

env:
  DOCKER_IMAGE: ${{ secrets.DOCKER_USERNAME }}/my-backend-app

jobs:
  ci:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: "21"
          distribution: "temurin"
          cache: gradle
      - run: chmod +x ./gradlew
      - name: Run Ktlint Check
        run: ./gradlew ktlintCheck --no-daemon
      - name: Run Tests
        run: ./gradlew test -Dspring.profiles.active=test --no-daemon

  cd:
    needs: ci
    if: github.event_name == 'push' && github.ref == 'refs/heads/main'
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - name: Set up JDK 21
        uses: actions/setup-java@v4
        with:
          java-version: "21"
          distribution: "temurin"
          cache: gradle
      - run: chmod +x ./gradlew
      - name: Build with Gradle
        run: ./gradlew clean build -x test --no-daemon
      - name: Login to Docker Hub
        uses: docker/login-action@v3
        with:
          username: ${{ secrets.DOCKER_USERNAME }}
          password: ${{ secrets.DOCKER_PASSWORD }}
      - name: Build and Push Docker Image
        run: |
          docker build -t $DOCKER_IMAGE:latest .
          docker push $DOCKER_IMAGE:latest
      - name: Install envsubst
        run: sudo apt-get update && sudo apt-get install -y gettext-base
      - name: Set up Kubectl
        uses: azure/setup-kubectl@v3
      - name: Configure Kubernetes Client
        run: |
          echo "${{ secrets.KUBE_CONFIG }}" | base64 -d > kubeconfig
          chmod 600 kubeconfig
      - name: Deploy to k3s
        env:
          KUBECONFIG: ./kubeconfig
          DOCKER_IMAGE_NAME: ${{ env.DOCKER_IMAGE }}:latest
          DB_PASSWORD: ${{ secrets.DB_PASSWORD }}
          REDIS_PASSWORD: ${{ secrets.REDIS_PASSWORD }}
          JWT_SECRET: ${{ secrets.JWT_SECRET }}
        run: |
          envsubst < k8s/backend.yaml | kubectl apply -f -
          kubectl rollout restart deployment/backend-app
```

---

## 14. Kubernetes 매니페스트

### k8s/backend.yaml

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: backend-app
  labels:
    app: backend
spec:
  replicas: 1
  selector:
    matchLabels:
      app: backend
  template:
    metadata:
      labels:
        app: backend
    spec:
      containers:
        - name: backend
          image: ${DOCKER_IMAGE_NAME}
          imagePullPolicy: Always
          ports:
            - containerPort: 8080
          envFrom:
            - secretRef:
                name: backend-secrets
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
            - name: SPRING_DATASOURCE_URL
              value: jdbc:mysql://mysql.default.svc.cluster.local:3306/my_database?useSSL=false&allowPublicKeyRetrieval=true&characterEncoding=UTF-8&serverTimezone=Asia/Seoul
            - name: SPRING_DATASOURCE_USERNAME
              value: root
            - name: SPRING_DATASOURCE_PASSWORD
              value: "${DB_PASSWORD}"
            - name: SPRING_REDIS_HOST
              value: redis-master.default.svc.cluster.local
            - name: SPRING_REDIS_PORT
              value: "6379"
            - name: SPRING_REDIS_PASSWORD
              value: "${REDIS_PASSWORD}"
            - name: JWT_SECRET
              value: "${JWT_SECRET}"
---
apiVersion: v1
kind: Service
metadata:
  name: backend-service
spec:
  type: NodePort
  selector:
    app: backend
  ports:
    - protocol: TCP
      port: 80
      targetPort: 8080
      nodePort: 30080
```

---

## 15. 아키텍처 요약

### 인증 흐름

```
Client → [Authorization: Bearer {JWT}]
       → JwtAuthenticationFilter (토큰 추출 & 검증)
       → TokenBlacklistService (Redis 블랙리스트 확인)
       → SecurityContext에 userId + role 저장
       → Controller (@LoggedInUserId로 userId 주입)
```

### 레이어 구조

```
Controller (REST API, DTO 변환)
    ↓
Service (비즈니스 로직)
    ↓
Repository (Spring Data JPA)
    ↓
Entity (JPA 엔티티, DB 매핑)
```

### 주요 설계 결정

1. **Stateless 인증**: 세션 없이 JWT만 사용, 로그아웃은 Redis 블랙리스트로 처리
2. **sealed class 예외 처리**: BusinessException을 sealed class로 선언, 컴파일러가 모든 예외 케이스 체크
3. **시간 추상화**: TimeProvider 인터페이스로 테스트에서 시간 제어 가능
4. **DB 마이그레이션**: Flyway로 스키마 버전 관리, ddl-auto는 validate만 사용
5. **프로필 분리**: local/prod 프로필로 환경별 설정 분리
6. **TestContainers**: 테스트 시 실제 MySQL + Redis 컨테이너 사용

---

## 16. 새 프로젝트 세팅 시 Claude에게 보낼 프롬프트 예시

```
이 MD 파일을 참고해서 새 Spring Boot + Kotlin 프로젝트를 만들어줘.

프로젝트 정보:
- 그룹: com.example
- 프로젝트명: myproject
- 패키지: com.example.myproject

필요한 도메인 모듈:
- user (회원가입, 로그인, JWT 인증)
- {내 도메인1}
- {내 도메인2}

다음을 모두 세팅해줘:
1. build.gradle.kts (의존성 전부)
2. config 패키지 (Security, JWT, JPA, WebMvc, OpenApi)
3. common 패키지 (예외 처리, 인증 어노테이션, 페이지네이션 DTO)
4. user 도메인 (Entity, Repository, Service, Controller, DTO, JwtTokenProvider)
5. application.yaml, application-local.yaml, application-prod.yaml
6. Flyway 초기 마이그레이션 SQL
7. Dockerfile, docker-compose.yaml
8. GitHub Actions CI/CD, K8s manifest
9. TestContainers 테스트 설정
```
