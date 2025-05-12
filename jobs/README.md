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
