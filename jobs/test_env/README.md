# Lambda Job 테스트 유틸리티

이 디렉토리에는 Lambda 작업을 개별적으로 테스트하기 위한 유틸리티가 포함되어 있습니다.

## 개별 Lambda 작업 테스트 (Docker 기반)

각 Lambda 작업은 독립적인 Docker 컨테이너에서 테스트됩니다. 로컬 환경에 파이썬이나 다른 의존성을 설치할 필요 없이 간편하게 테스트할 수 있습니다.

### 사전 요구 사항

- 테스트시 실제 OpenAI API를 호출하기 때문에 API Key가 필요합니다. 

### 테스트 명령어

각 테스트는 자동으로 샘플 PDF 파일을 생성하고 S3에 업로드한 후 진행됩니다:

1. **요약 기능 테스트** (summarize_lecture):
```bash
make test-job-summarize
```

2. **퀴즈 생성 기능 테스트** (generate_quiz):
```bash
make test-job-quiz
```

3. **모의시험 생성 기능 테스트** (generate_exam):
```bash
make test-job-exam
```

4. **퀴즈 서술형 채점 기능 테스트** (grade_quiz_essay):
```bash
make test-job-grade-quiz-essay
```

5. **시험 서술형 채점 기능 테스트** (grade_exam_essay):
```bash
make test-job-grade-exam-essay
```

### OpenAI API 키 설정

테스트에 실제 OpenAI API 키를 사용하려면:
```bash
export OPENAI_API_KEY=your_api_key_here
make test-job-summarize  # 또는 test-job-quiz, test-job-exam
```

### 디버깅

테스트 환경 내에서 직접 명령을 실행하고 싶을 때:
```bash
make test-job-shell
```

## 테스트 인프라 관리

테스트 인프라(PostgreSQL, LocalStack)를 별도로 관리할 수 있습니다:

```bash
# 테스트 환경(DB, S3) 시작
make test-env-start

# 테스트 데이터베이스 초기화
make test-env-setup

# 테스트 환경 중지
make test-env-stop
```

## 중요 정보

### 네트워크 구성
- PostgreSQL: `localhost:5433` (외부 포트)
- LocalStack S3: `http://localhost:4567`

### 테스트 데이터
테스트 데이터는 자동으로 생성되며 각 작업 실행 시 다음 정보를 사용합니다:
- 샘플 PDF 파일: `test/sample_lecture.pdf` (자동 생성 및 업로드됨)
- 테스트 사용자, 강의, 강좌 ID (자동 생성됨)

## 테스트 프로세스 설명

각 테스트 명령 실행 시 다음 과정이 자동으로 수행됩니다:

1. PostgreSQL 및 LocalStack 컨테이너 시작
2. 테스트 데이터베이스 스키마 생성 및 샘플 데이터 삽입
3. 샘플 PDF 파일 생성 및 S3 업로드
4. 실제 Lambda 작업 테스트 실행

## 문제 해결

### 데이터베이스 연결 오류

PostgreSQL 연결 오류가 발생하는 경우:
```bash
make test-env-stop
make test-env-start
make test-env-setup
```
