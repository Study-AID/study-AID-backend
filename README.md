# Study AID Backend

Study AID API server and LLM processing jobs for Study AID service.

## 기술 스택

- **Framework**: Spring Boot
- **API Docs**: Swagger/OpenAPI
- **Database**: PostgreSQL
- **Cache**: Redis
- **Storage**: MinIO (S3 호환)
- **Containerization**: Docker
- **Build Tool**: Gradle
- **Migration**: Flyway
- **Background Jobs**: AWS Lambda (Python)
- **Local Cloud Emulation**: LocalStack

## 프로젝트 구조

- **API Server**: Spring Boot 기반 REST API
- **Background Jobs**: LLM 처리 작업 ([자세히 보기](jobs/README.md))

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
2. DB 마이그레이션:
   ```bash
   make migrate
   ```
3. API Endpoints:
    - Swagger UI: http://localhost:8080/api/swagger-ui.html
    - Health Check: http://localhost:8080/api/v1/health
4. 기타 서비스:
    - MinIO 콘솔: http://localhost:9001 (계정: minioadmin/minioadmin)

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
make test_win             # 테스트 실행 (Windows)
make test-coverage        # 테스트 실행 및 커버리지 리포트 생성
make test-coverage_win    # 테스트 실행 및 커버리지 리포트 생성 (Windows)
make open-test-report     # 브라우저로 테스트 결과 확인
make open-coverage-report # 브라우저로 테스트 커버리지 결과 확인
make migrate              # 데이터베이스 마이그레이션
make migration-info       # 데이터베이스 스키마 버전 확인
```

## API 서버 테스트

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

## Lambda Job 테스트

Lambda 함수들의 로컬 테스트와 배포에 관한 자세한 정보는 [Lambda 작업 문서](jobs/README.md)를 참고하세요.


## SQL 변경과 DB 마이그레이션
DB에 새로운 sql 반영은 make run 과 별도로 해주셔야 합니다. make run 명령어 실행 후 기다리면 LocalStack도 자동으로 초기화됩니다.

### 로컬 개발 환경

1. flyway/migrations 디렉토리에 새 SQL 파일 생성
   
   예: V2__add_school_column_to_user_table.sql   
   파일명은 반드시 V<버전번호>__<설명>.sql 형식을 따라야 합니다.   
   
2. 마이그레이션 단독 실행 
   ```bash
   make migrate
   ```
   
3. 마이그레이션 적용 상태 (현재 Schema 상태) 확인
   ```bash
   make migration-info
   ```
   make migration-info 실행 결과에서 State가 Pending이면 아직 마이그레이션 안 된 상태입니다.

   참고: 로컬 개발 단계에서 DB 스키마 롤백하려면 make clean, 새로 작성한 sql 파일을 삭제/수정, make migrate

### Dev 환경 마이그레이션
```bash
git tag migrate-db-<version>-dev
git push origin migrate-db-<version>-dev
```

### Prod 환경 마이그레이션
```bash
git tag migrate-db-<version>
git push origin migrate-db-<version>
```
