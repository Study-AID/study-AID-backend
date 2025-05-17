# Async LLM Jobs

A Python Lambda function that generates quizzes from lecture materials using OpenAI's LLM.

## 기술 스택

- **Runtime**: Python 3.13+
- **LLM**: OpenAI API (GPT-4o)
- **Storage**: AWS S3
- **Database**: PostgreSQL
- **PDF Processing**: PyMuPDF

## 로컬 환경 셋업

### Pre-requisites

- Python 3.13 or higher
- AWS credentials (when using AWS S3)
- OpenAI API key

### 환경 설정

1. **Python 가상환경 생성 및 의존성 설치**:
   ```bash
   # 가상환경 생성
   python -m venv .venv
   
   # 가상환경 활성화
   # Windows
   .venv\Scripts\activate
   # macOS/Linux
   source .venv/bin/activate
   
   # 의존성 설치
   pip install -r requirements.txt
   ```

2. **환경 변수 설정**:
   ```bash
   # OpenAI API 키 설정
   export OPENAI_API_KEY="your-api-key"
   
   # 프롬프트 버전 설정 (기본값: latest)
   export PROMPT_VERSION="latest"  # 또는 특정 버전 번호 (예: "1", "2")
   ```

## 기능 및 동작 방식

1. **요청 처리**:
   - SQS 메시지를 통해 lecture_id, 문제 유형별 개수 정보 수신
   - 메시지 형식:
     ```json
     {
       "lecture_id": "uuid",
       "title": "optional quiz title",
       "true_or_false_count": 2,
       "multiple_choice_count": 3,
       "short_answer_count": 1,
       "essay_count": 1
     }
     ```

2. **콘텐츠 획득**:
   - 강의 요약이 있는 경우 요약 사용
   - 요약이 없는 경우 S3에서 원본 PDF 다운로드 후 텍스트 추출

3. **퀴즈 생성**:
   - OpenAI API를 사용하여 지정된 문제 유형별로 퀴즈 생성
   - prompts/generate_quiz 디렉토리의 YAML 형식 프롬프트 템플릿 사용
   - 지원하는 문제 유형:
     - 참/거짓 (true_or_false)
     - 객관식 (multiple_choice)
     - 단답형 (short_answer)
     - 서술형 (essay)

4. **결과 저장**:
   - 생성된 퀴즈와 문제들을 데이터베이스에 저장
   - 각 문제의 설명(explanation)과 정답 정보 포함

## 테스트

로컬에서 함수를 테스트하는 방법:

1. **테스트 환경 시작**:
   ```bash
   # 루트 디렉토리에서 실행
   make test-env-start
   ```

2. **테스트 데이터베이스 설정**:
   ```bash
   make test-env-setup
   ```

3. **퀴즈 생성 작업 테스트 실행**:
   ```bash
   make test-job-quiz
   ```

4. **테스트 환경 중지 및 정리**:
   ```bash
   # 테스트 환경 중지
   make test-env-stop
   
   # 모든 리소스 정리 (볼륨 포함)
   make test-env-clean
   ```

5. **디버깅을 위한 셸 접근**:
   ```bash
   make test-job-shell
   ```

## 프롬프트 템플릿

퀴즈 생성은 YAML 형식의 프롬프트 템플릿을 사용합니다:

- 템플릿 위치: `../../prompts/generate_quiz/v{version}.yaml`
- 프롬프트 버전 선택:
  - 기본값: `latest` (가장 높은 버전의 프롬프트 파일 자동 선택)
  - 지정 버전: `PROMPT_VERSION` 환경 변수로 설정 (예: `1`, `2`)
- 형식:
  - `system`: 시스템 프롬프트
  - `user`: 사용자 프롬프트 (강의 내용과 문제 유형별 개수 포함)
  - `A`: (선택사항) 응답 형식

프롬프트 파일은 `v1.yaml`, `v2.yaml` 등의 명명 규칙을 따라야 합니다. 이는 프로그램이 버전을 인식하고 범위를 정하는 데 사용됩니다.
