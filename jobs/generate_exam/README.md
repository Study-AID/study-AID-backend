# Job for generating exam

Python Lambda 함수로, OpenAI의 LLM을 사용하여 참조 강의 자료를 기반으로 시험을 생성합니다.

## 기술 스택

- **Runtime**: Python 3.13+
- **LLM**: OpenAI API (GPT-4o)
- **Storage**: AWS S3
- **Database**: PostgreSQL
- **Dependency Management**: pyproject.toml & uv.lock
- **PDF Processing**: PyMuPDF
- **Type Checking**: Pydantic

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
d
   # 프롬프트 버전 설정 (기본값: latest)
   export PROMPT_VERSION="latest"  # 또는 특정 버전 번호 (예: "1", "2")
   ```

## 기능 및 동작 방식

1. **요청 처리**:
   - SQS 메시지를 통해 user_id, referenced_lecture_ids 및 시험 설정 정보 수신
   - 참조 강의 자료의 내용을 수집하여 시험 생성에 사용

2. **텍스트 추출**:
   - 참조된 강의 레코드에서 요약이 있는 경우 요약 사용
   - 요약이 없는 경우 S3에서 PDF 파일 다운로드
   - PyMuPDF를 사용하여 PDF에서 텍스트 추출

3. **시험 생성**:
   - OpenAI API를 사용하여 참조 강의 내용 기반 시험 생성
   - prompts/generate_exam 디렉토리의 YAML 형식 프롬프트 템플릿 사용
   - Pydantic 모델을 사용하여 응답을 구조화된 형태로 검증 및 파싱

4. **결과 저장**:
   - 생성된 시험을 데이터베이스에 저장 (bulk insert 사용)
   - 관련 활동 기록

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

3. **시험 생성 작업 테스트 실행**:
   ```bash
   make test-job-exam
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

시험 생성은 YAML 형식의 프롬프트 템플릿을 사용합니다:

- 템플릿 위치: `../../prompts/generate_exam/v{version}.yaml`
- 프롬프트 버전 선택:
  - 기본값: `latest` (가장 높은 버전의 프롬프트 파일 자동 선택)
  - 지정 버전: `PROMPT_VERSION` 환경 변수로 설정 (예: `1`, `2`)
- 형식:
  - `system`: 시스템 프롬프트
  - `user`: 사용자 프롬프트 (강의 내용 포함)
  - `temperature`: 온도 설정 (생성 다양성 제어)
  - `max_tokens`: 최대 토큰 수

프롬프트 파일은 `v1.yaml`, `v2.yaml` 등의 명명 규칙을 따라야 합니다. 이는 프로그램이 버전을 인식하고 범위를 정하는 데 사용됩니다.

## Structured Output

시험 생성은 Pydantic 모델을 사용하여 구조화된 응답을 처리합니다:

- `exam_models.py`에 정의된 클래스 계층 구조 사용
- 여러 유형의 문제 지원 (객관식, 단답형, 서술형, 문제 해결)
- 모델 검증을 통한 데이터 무결성 보장
