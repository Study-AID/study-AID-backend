# Study AID Jobs

AWS Lambda로 실행되는 백그라운드 LLM Job들을 관리합니다.

## 디렉토리 구조

```
jobs/
├── course_weakness_analysis/ # 강의 약점 분석 및 학습 제안 생성 작업
├── grade_quiz_essay    # 퀴즈 서술형 채점 작업
├── grade_exam_essay    # 시험 서술형 채점 작업
├── generate_exam/      # 시험 문제 생성 작업
├── generate_quiz/      # 퀴즈 생성 작업
├── summarize_lecture/  # 강의 요약 작업
└── test_env/          # 테스트 환경 (개발용)
```

## 각 Job 별 문서

- **Summarize Lecture**: [README](summarize_lecture/README.md)
- **Generate Quiz**: [README](generate_quiz/README.md)
- **Generate Exam**: [README](generate_exam/README.md)
- **Grade Quiz Essay**: [README](grade_quiz_essay/README.md)
- **Grade Exam Essay**: [README](grade_exam_essay/README.md)
- **Generate Course Weakness Analysis**: [README](generate_course_weakness_analysis/README.md)

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

# 퀴즈 서술형 채점 기능 테스트
make test-job-grade-quiz-essay

# 시험 서술형 채점 기능 테스트
make test-job-grade-exam-essay

# 과목 약점 분석 및 학습 제안 레포트 생성 테스트
make test-job-generate-course-weakness-analysis
```

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
git tag deploy-job-summarize-lecture-1.0.0-dev
git push origin deploy-job-summarize-lecture-1.0.0-dev

# Generate Exam
git tag deploy-job-generate-exam-1.0.0-dev
git push origin deploy-job-generate-exam-1.0.0-dev

# Generate Quiz
git tag deploy-job-generate-quiz-1.0.0-dev
git push origin deploy-job-generate-quiz-1.0.0-dev

# Grade Quiz Essay
git tag deploy-job-grade-quiz-essay-1.0.0-dev
git push origin deploy-job-grade-quiz-essay-1.0.0-dev

# Grade Exam Essay
git tag deploy-job-grade-exam-essay-1.0.0-dev
git push origin deploy-job-grade-exam-essay-1.0.0-dev

# Generate Course Weakness Analysis
git tag deploy-job-generate-course-weakness-analysis-1.0.0-dev
git push origin deploy-job-generate-course-weakness-analysis-1.0.0-dev
```

#### Prod 환경
```bash
# Summarize Lecture
git tag deploy-job-summarize-lecture-1.0.0
git push origin deploy-job-summarize-lecture-1.0.0

# Generate Exam
git tag deploy-job-generate-exam-1.0.0
git push origin deploy-job-generate-exam-1.0.0

# Generate Quiz
git tag deploy-job-generate-quiz-1.0.0
git push origin deploy-job-generate-quiz-1.0.0
```

### Lambda 함수 이름

- **Dev**: `dev-{job-name}`
- **Prod**: `prod-{job-name}`

### 배포 태그 규칙

- **Dev**: `deploy-job-{job-name}-x.y.z-dev`
- **Prod**: `deploy-job-{job-name}-x.y.z`

여기서 `{job-name}`는:
- `summarize-lecture`: 강의 요약
- `generate-exam`: 시험 생성
- `generate-quiz`: 퀴즈 생성
- `grade-quiz-essay`: 퀴즈 서술형 채점
- `grade-exam-essay`: 시험 서술형 채점
- `generate-course-weakness-analysis`: 과목 약점 분석 및 학습 제안 생성

## 프롬프트 관리

모든 job은 `/prompts` 디렉토리의 프롬프트 템플릿을 사용합니다. 각 job은 자체적인 프롬프트 버전 관리를 지원합니다.
