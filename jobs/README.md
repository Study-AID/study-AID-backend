# Study AID Jobs

AWS Lambda로 실행되는 백그라운드 LLM Job들을 관리합니다.

## 디렉토리 구조

```
jobs/
├── generate_exam/      # 시험 문제 생성 작업
├── generate_quiz/      # 퀴즈 생성 작업
├── summarize_lecture/  # 강의 요약 작업
└── test_env/          # 테스트 환경 (개발용)
```

## 각 Job 별 문서

- **Summarize Lecture**: [README](summarize_lecture/README.md)
- **Generate Quiz**: [README](generate_quiz/README.md)
- **Generate Exam**: [README](generate_exam/README.md)

## Lambda Job 테스트

백그라운드 작업으로 실행되는 Lambda 함수들을 로컬에서 테스트하기 위한 명령어입니다.

### 테스트 환경 설정

```bash
# 테스트 환경 시작 (PostgreSQL, LocalStack)
make test-env-start

# 테스트 데이터베이스 초기화 및 샘플 데이터 설정
make test-env-setup
```

### 개별 작업 테스트

```bash
# 강의 요약 기능 테스트
make test-job-summarize

# 퀴즈 생성 기능 테스트
make test-job-quiz

# 시험 생성 기능 테스트
make test-job-exam

# 테스트용 셸 실행 (디버깅용)
make test-job-shell
```

### 테스트 환경 정리

```bash
# 테스트 환경 중지
make test-env-stop

# 테스트 환경 완전 정리 (볼륨 포함)
make test-env-clean
```

## Lambda Job 배포

### 태그 기반 자동 배포

GitHub Actions를 통해 태그 푸시 시 자동으로 Lambda에 배포됩니다.

### 배포 방법

#### Dev 환경
```bash
# Summarize Lecture
git tag deploy-summarize-1.0.0-dev
git push origin deploy-summarize-1.0.0-dev

# Generate Exam
git tag deploy-exam-1.0.0-dev
git push origin deploy-exam-1.0.0-dev

# Generate Quiz
git tag deploy-quiz-1.0.0-dev
git push origin deploy-quiz-1.0.0-dev
```

#### Prod 환경
```bash
# Summarize Lecture
git tag deploy-summarize-1.0.0
git push origin deploy-summarize-1.0.0

# Generate Exam
git tag deploy-exam-1.0.0
git push origin deploy-exam-1.0.0

# Generate Quiz
git tag deploy-quiz-1.0.0
git push origin deploy-quiz-1.0.0
```

### Lambda 함수 이름

- **Dev**: `dev-{job-name}`
- **Prod**: `prod-{job-name}`

### 배포 태그 규칙

- **Dev**: `deploy-{job}-x.y.z-dev`
- **Prod**: `deploy-{job}-x.y.z`

여기서 `{job}`는:
- `summarize`: summarize_lecture
- `exam`: generate_exam
- `quiz`: generate_quiz

## 프롬프트 관리

모든 job은 `/prompts` 디렉토리의 프롬프트 템플릿을 사용합니다. 각 job은 자체적인 프롬프트 버전 관리를 지원합니다.
