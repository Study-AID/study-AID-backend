# Async LLM Jobs

A Python Lambda function that grade essay questions (for exam) using OpenAI's LLM.

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

1. **요청 처리**:
    - SQS 메시지를 통해 user_id, exam_id 수신
    - 메시지 형식:
      ```json
      {
        "user_id": "user_uuid",
        "exam_id" : "exam_uuid"
      }
      ```
      
2. **시험의 서술형 문항 채점**:
    - `exam_id`에 해당하는 시험의 서술형 문항 모두 조회 후 한 문제씩 순차적으로 한 람다에서 채점
    - OpenAI API를 사용하여 서술형 문항 채점 
    - prompts/grade_exam_essay 디렉토리의 YAML 형식 프롬프트 템플릿 사용

4. **채점 결과 저장**:
    - `exam_responses` 테이블의 `score`, `essay_criteria_analysis` 컬럼에 각 서술형 문항 채점 점수 및 채점 기준, 결과, 공부 제안 저장
    - `exam_results` 테이블에 시험 총점 업데이트

5. **시험 상태 업데이트**:
    - 모든 서술형 문항 채점 완료 후 `exams` 테이블의 `status` 컬럼을 `graded`로 업데이트

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

3. **시험 서술형 문항 채점 작업 테스트 실행**:
   ```bash
   make test-job-grade-exam-essay
   ```

4. **채점 결과 조회**:
    ```bash
    docker exec -it test_env-postgres-test-1 psql -U postgres -d studyaid
    ```
    ```sql
    SELECT 
        er.question_id,
        ei.question,
        ei.explanation as model_explanation,  
        ei.text_answer as model_answer,       
        er.text_answer as student_answer,
        er.score,
        ei.points as max_points,              
        er.essay_criteria_analysis
    FROM app.exam_responses er
    JOIN app.exam_items ei ON er.question_id = ei.id
    WHERE er.exam_id = 'b93e4f3a-6c8d-4f1a-a59f-43d65639e7e1'
    AND ei.question_type = 'essay'
    AND er.score IS NOT NULL
    ORDER BY er.question_id;
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

시험 서술형 채점은 YAML 형식의 프롬프트 템플릿을 사용합니다:

- 템플릿 위치: `../../prompts/grade_exam_essay/v{version}.yaml`
- 프롬프트 버전 선택:
    - 기본값: `latest` (가장 높은 버전의 프롬프트 파일 자동 선택)
    - 지정 버전: `PROMPT_VERSION` 환경 변수로 설정 (예: `1`, `2`)
- 형식:
    - `system`: 시스템 프롬프트
    - `user`: 사용자 프롬프트 (강의 내용과 문제 유형별 개수 포함)
    - `A`: (선택사항) 응답 형식

프롬프트 파일은 `v1.yaml`, `v2.yaml` 등의 명명 규칙을 따라야 합니다. 이는 프로그램이 버전을 인식하고 범위를 정하는 데 사용됩니다.

## Structured Output

시험 서술형 문항 채점은 Pydantic 모델을 사용하여 구조화된 응답을 처리합니다:

- `exam_grading_models`에 정의된 클래스 계층 구조 사용

## 에러 처리

- **OpenAI API 오류**: API 호출 실패 시 로깅 후 예외 발생
- **이외 오류**: 시스템 오류로 안내한 후, 만점 처리