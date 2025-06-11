# Async LLM Jobs

A Python Lambda function that analyzes the user's weakness in a course and provides learning suggestions using OpenAI's LLM.

## 기술 스택

- **Runtime**: Python 3.13+
- **LLM**: OpenAI API (GPT-4o)
- **Database**: PostgreSQL

## 로컬 환경 셋업

### Pre-requisites

- Python 3.13 or higher
- OpenAI API key 

### 환경 설정

1. **환경 변수 설정**:
   ```bash
   # OpenAI API 키 설정 (필수, 설정 안하면 오류 발생)
   export OPENAI_API_KEY="your-api-key"
   
   # 프롬프트 버전 설정 (기본값: latest)
   export PROMPT_VERSION="latest"  # 또는 특정 버전 번호 (예: "1", "2")
   ```

## 기능 및 동작 방식

0. **트리거 조건**:
    - 서술형 채점 Lambda에서: 서술형 문제가 포함된 퀴즈/모의시험이 `graded` 상태로 변경될 때
    - Spring에서: 서술형 문제가 없는 퀴즈/모의시험이 `graded` 상태로 변경될 때
   
1. **요청 처리**:
    - SQS 메시지를 통해 user_id, quiz_id, exam_id 수신
    - 메시지 형식(새롭게 graded 된 것이 퀴즈인 경우):
      ```json
      {
        "user_id": "user_uuid",
        "quiz_id" : "quiz_uuid",
        "exam_id" : null,
        "course_id": "course_uuid"
      }
      ```
      
2. **기존 분석 조회**:
    - 해당 과목의 기존 course_weakness_analysis 데이터 조회
    - 기존 분석이 없으면 새로 생성, 있으면 기존 분석에 새로운 분석을 추가

3. **새로운 퀴즈/모의시험 오답 데이터 조회 및 약점 분석 & 학습 제안 내용 생성**:
    - 새롭게 `graded`된 퀴즈/모의시험의 틀린 문제들만 조회 (문제, 정답, 해설, 학생 답안, 점수 등 상세 정보 수집)
    - 토큰 절약을 위해 새 데이터만 GPT에 전송

4. ** 분석 결과 저장**:
    - courses 테이블의 `course_weakness_analysis` 컬럼에 통합된 분석 결과 저장
    - JSON 형식으로 약점(weaknesses), 학습 제안(suggestions), 분석 일시(analyzed_at) str으로 저장

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

3. **과목 약점 분석 & 학습 제안 레포트 생성 테스트 실행**:
   ```bash
   make test-job-generate-course-weakness-analysis
   ```

4. **과목 약점 분석 & 학습 제안 레포트 생성 결과 조회**:
    ```bash
    docker exec -it test_env-postgres-test-1 psql -U postgres -d studyaid
    ```
    ```sql
    SELECT course_weakness_analysis 
    FROM app.courses
    WHERE id = 'your_course_id_here';
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

과목 약점 분석 & 학습 제안 레포트 생성은 YAML 형식의 프롬프트 템플릿을 사용합니다:

- 템플릿 위치: `../../prompts/generate_course_weakness_analysis/v{version}.yaml`
- 프롬프트 버전 선택:
    - 기본값: `latest` (가장 높은 버전의 프롬프트 파일 자동 선택)
    - 지정 버전: `PROMPT_VERSION` 환경 변수로 설정 (예: `1`, `2`)
- 형식:
    - `system`: 시스템 프롬프트
    - `user`: 사용자 프롬프트 (강의 내용과 문제 유형별 개수 포함)
    - `A`: (선택사항) 응답 형식

프롬프트 파일은 `v1.yaml`, `v2.yaml` 등의 명명 규칙을 따라야 합니다. 이는 프로그램이 버전을 인식하고 범위를 정하는 데 사용됩니다.

## Structured Output

과목 약점 분석 & 학습 제안 레포트 생성은 Pydantic 모델을 사용하여 구조화된 응답을 처리합니다:

- `weakness_analyze_models`에 정의된 클래스 계층 구조 사용

## 에러 처리

- **OpenAI API 오류**: API 호출 실패 시 로깅 후 예외 발생
- **이외 오류**: 