# Study AID Backend

Study AID API server and LLM processing jobs for Study AID service.

## 기술 스택

- **Framework**: Spring Boot
- **API Docs**: Swagger/OpenAPI
- **Database**: PostgreSQL
- **Cache**: Redis
- **Containerization**: Docker
- **Build Tool**: Gradle
- **Migration**: Flyway

## 로컬 환경 셋업

### Pre-requisites

- Docker 및 Docker Compose
- JDK 17
- Gradle

### Windows 가이드

1. **필수 도구 설치**:
    - [Docker Desktop for Windows](https://www.docker.com/products/docker-desktop) 설치
    - [JDK 17](https://adoptium.net/) 설치
    - [Git for Windows](https://gitforwindows.org/) 설치

2. **Make 설치 옵션**:
    - **옵션 1 - Chocolatey 사용** (권장):
      ```powershell
      # PowerShell 관리자 권한으로 실행
      Set-ExecutionPolicy Bypass -Scope Process -Force
      iex ((New-Object System.Net.WebClient).DownloadString('https://chocolatey.org/install.ps1'))
      choco install make
      ```
    - **옵션 2 - Git Bash 사용**:
        - Git for Windows가 이미 설치되어 있다면, Git Bash에서 기본적인 Unix 명령어와 함께 make를 사용할 수 있습니다.

3. **환경 변수 설정**:
    - System properties → Advanced → Environment variables에서 다음을 추가:
        - `JAVA_HOME`: JDK 설치 경로 (예: `C:\Program Files\Eclipse Adoptium\jdk-17.0.6.10-hotspot`)
        - `Path`에 다음 경로 추가: `%JAVA_HOME%\bin`

4. **Windows에서 Docker 경로 설정**:
    - Windows에서 Docker 볼륨 마운트 시 경로 표기에 주의해야 합니다.
    - WSL2 backend를 사용하는 경우 Linux 스타일 경로를 사용하고, Hyper-V backend를 사용하는 경우 Windows 스타일 경로에 주의해야 합니다.

## 실행하기

1. 서비스 컨테이너 실행:
   ```bash
   make run
   ```

2. API Endpoints:
    - Swagger UI: http://localhost:8080/api/swagger-ui.html
    - Health Check: http://localhost:8080/api/v1/health

### 명령어 목록

```bash
make build                # Docker 이미지 빌드
make run                  # 서비스 시작
make logs                 # 로그 확인
make logs-api             # API 서비스 로그 확인
make down                 # 서비스 중지
make clean                # 모든 컨테이너/볼륨/이미지 제거
make ps                   # 실행 중인 컨테이너 목록
make pgsql-cli            # PostgreSQL 접속
make redis-cli            # Redis CLI 접속
make test                 # 테스트 실행
make test-coverage        # 테스트 실행 및 커버리지 리포트 생성
make open-test-report     # 브라우저로 테스트 결과 확인
make open-coverage-report # 브라우저로 테스트 커버리지 결과 확인
```

## 테스트

테스트를 실행하고 보고서를 확인하는 방법:

1. 기본 테스트 실행 (변경된 테스트만 실행):
   ```bash
   make test
   ```

2. 테스트 커버리지 리포트 생성:
   ```bash
   make test-coverage
   ```

3. 테스트 결과 확인:
   ```bash
   # 테스트 보고서 브라우저에서 열기
   make open-test-report
   
   # 커버리지 보고서 브라우저에서 열기
   make open-coverage-report
   ```
