# Async LLM Jobs

A Python Lambda function that generates summaries of lecture materials using OpenAI's LLM.

## 기술 스택

- **Runtime**: Python 3.13+
- **LLM**: OpenAI API (GPT-4o)
- **Storage**: AWS S3
- **Database**: PostgreSQL
- **Dependency Management**: pyproject.toml & uv.lock
- **PDF Processing**: PyMuPDF

## 로컬 환경 셋업

### Pre-requisites

- Python 3.13 or higher
- uv package manager
- AWS credentials (when using AWS S3)
- OpenAI API key

### 환경 설정

1. **Python 가상환경 생성 및 의존성 설치**:
   ```bash
   # uv 설치 (없는 경우)
   pip install uv
   
   # 가상환경 생성 및 의존성 설치 (uv 사용)
   uv venv
   uv pip install -r requirements.txt
   
   # 가상환경 활성화
   # Windows
   .venv\Scripts\activate
   # macOS/Linux
   source .venv/bin/activate
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
   - SQS 메시지를 통해 lecture_id, S3 버킷 및 키 정보 수신
   - 대응하는 강의 자료가 S3에 업로드되면 처리 시작

2. **텍스트 추출**:
   - S3에서 PDF 파일 다운로드
   - PyMuPDF를 사용하여 PDF에서 텍스트 추출

3. **요약 생성**:
   - OpenAI API를 사용하여 강의 내용 요약 생성
   - prompts/summarize_lecture 디렉토리의 YAML 형식 프롬프트 템플릿 사용

4. **결과 저장**:
   - 생성된 요약을 데이터베이스에 저장
   - 강의 요약 상태 업데이트 (in_progress, completed)

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

3. **요약 작업 테스트 실행**:
   ```bash
   make test-job-summarize
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

강의 요약은 YAML 형식의 프롬프트 템플릿을 사용합니다:

- 템플릿 위치: `../../prompts/summarize_lecture/v{version}.yaml`
- 프롬프트 버전 선택:
  - 기본값: `latest` (가장 높은 버전의 프롬프트 파일 자동 선택)
  - 지정 버전: `PROMPT_VERSION` 환경 변수로 설정 (예: `1`, `2`)
- 형식:
  - `system`: 시스템 프롬프트
  - `user`: 사용자 프롬프트 (강의 내용 포함)
  - `A`: (선택사항) 응답 형식

프롬프트 파일은 `v1.yaml`, `v2.yaml` 등의 명명 규칙을 따라야 합니다. 이는 프로그램이 버전을 인식하고 범위를 정하는 데 사용됩니다.

## 에러 처리

- **데이터베이스 연결 오류**: 연결 실패 시 로깅 후 예외 발생
- **S3 파일 다운로드 오류**: ClientError 발생 시 로깅 후 예외 발생
- **PDF 처리 오류**: PyMuPDF 오류 발생 시 로깅 후 예외 발생
- **OpenAI API 오류**: API 호출 실패 시 로깅 후 예외 발생
