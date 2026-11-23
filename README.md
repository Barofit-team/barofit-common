# barofit-common

**barofit** 백엔드 서비스들이 공유하는 라이브러리 레포입니다. 원래 `backend_baro` 모노레포의
`shared/common`, `shared/runtime` 모듈을 폴리레포 전환 과정에서 분리했습니다(커밋 이력 보존).

각 서비스 레포(`barofit-identity`, `barofit-routine`, `barofit-workout`, `barofit-analytics`,
`barofit-report`)가 이 레포의 산출물(`barofit-common`, `barofit-runtime`)을 의존성으로 참조합니다.

## 모듈 구성

```
barofit-common/
├─ common/   barofit-common  — 공통 에러 응답(RFC 9457 Problem Details), ModuleSchema 계약
└─ runtime/  barofit-runtime — 공통 JWT 검증, Clock, Flyway 실행 설정, Actuator/Data JPA/보안 BOM
```

`runtime`은 `common`을 `api` 의존성으로 감쌉니다 — 서비스는 보통 `barofit-runtime`만 받으면 됩니다.

## 기술 스택

- Java 21 / Gradle 9 (멀티 모듈)
- Spring Boot 4.1 (Actuator, Data JPA, OAuth2 Resource Server)
- Flyway

## 빌드 및 배포

```bash
./gradlew build publishToMavenLocal
```

> **배포 경로**: `dev`에 push되면 `.github/workflows/publish.yml`이 GitHub Packages
> (`maven.pkg.github.com/Barofit-team/barofit-common`)에 자동 발행하고, 5개 서비스 레포에
> `repository_dispatch`를 보내 각 레포의 CI를 새 버전으로 재실행시킵니다. 로컬 개발 중에는 여전히
> `publishToMavenLocal`을 실행하면 서비스 레포가 그 로컬 빌드를 GitHub Packages보다 먼저 집어갑니다
> (각 서비스 `build.gradle`의 `repositories`에 `mavenLocal()`이 `GitHubPackages`보다 앞에 있음).

생성되는 좌표:

| artifact | groupId | version |
|---|---|---|
| `barofit-common` | `com.barofit` | `0.0.1-SNAPSHOT` |
| `barofit-runtime` | `com.barofit` | `0.0.1-SNAPSHOT` |

서비스 쪽 `build.gradle` 예시:
```gradle
repositories {
    mavenCentral()
    mavenLocal()
}
dependencies {
    implementation 'com.barofit:barofit-runtime:0.0.1-SNAPSHOT'
}
```

## 테스트

```bash
./gradlew test
```

## 관련 레포

- [barofit-identity](https://github.com/Barofit-team/barofit-identity) · [barofit-routine](https://github.com/Barofit-team/barofit-routine) · [barofit-workout](https://github.com/Barofit-team/barofit-workout) · [barofit-analytics](https://github.com/Barofit-team/barofit-analytics) · [barofit-report](https://github.com/Barofit-team/barofit-report)
- [barofit-infra](https://github.com/Barofit-team/barofit-infra) — Helm 차트, 로컬 K8s 배포
- [backend_baro](https://github.com/Barofit-team/backend_baro) — 분리 이전 모노레포(참고용, 더 이상 서비스 코드가 갱신되지 않음)
