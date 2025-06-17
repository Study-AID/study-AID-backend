import boto3
import json
import logging
import os
import psycopg2
import psycopg2.extras
import traceback
import uuid
from datetime import datetime

from openai_client import OpenAIClient

# Register UUID adapter for psycopg2
psycopg2.extras.register_uuid()

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Database configuration
DB_CONFIG = {
    'host': os.environ.get('DB_HOST'),
    'user': os.environ.get('DB_USER'),
    'password': os.environ.get('DB_PASSWORD'),
    'database': os.environ.get('DB_NAME'),
    'port': int(os.environ.get('DB_PORT'))
}

# Initialize SQS client
sqs_client = boto3.client('sqs', region_name=os.environ.get('AWS_REGION', 'ap-northeast-2'))


def get_db_connection():
    """데이터베이스 연결을 생성합니다."""
    try:
        conn = psycopg2.connect(
            host=DB_CONFIG['host'],
            user=DB_CONFIG['user'],
            password=DB_CONFIG['password'],
            dbname=DB_CONFIG['database'],
            port=DB_CONFIG['port']
        )
        return conn
    except Exception as e:
        logger.error(f"Error connecting to database: {e}")
        raise


def get_exam_essay_items(exam_id):
    """시험의 모든 서술형 문항을 가져옵니다."""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cursor:
            query = """
                SELECT id, question, explanation, text_answer, points
                FROM app.exam_items
                WHERE exam_id = %s 
                  AND question_type = 'essay'
                  AND deleted_at IS NULL
                ORDER BY created_at
            """
            cursor.execute(query, (exam_id,))
            exam_items = cursor.fetchall()

            return [dict(item) for item in exam_items]

    except Exception as e:
        logger.error(f"Error getting exam essay items: {e}")
        raise
    finally:
        if conn:
            conn.close()


def get_exam_response_data(question_id, user_id):
    """exam_responses 테이블에서 사용자 답변을 가져옵니다."""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cursor:
            query = """
                SELECT text_answer, score
                FROM app.exam_responses
                WHERE question_id = %s 
                  AND user_id = %s
                  AND deleted_at IS NULL
            """
            cursor.execute(query, (question_id, user_id))
            exam_response = cursor.fetchone()

            if exam_response:
                return dict(exam_response)
            else:
                logger.warning(f"Exam response not found for question: {question_id}, user: {user_id}")
                return None

    except Exception as e:
        logger.error(f"Error getting exam response data: {e}")
        raise
    finally:
        if conn:
            conn.close()


def is_question_already_graded(question_id, user_id):
    """이미 성공적으로 채점된 문항인지 확인합니다."""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor() as cursor:
            query = """
                SELECT score, essay_criteria_analysis 
                FROM app.exam_responses
                WHERE question_id = %s AND user_id = %s 
                  AND score IS NOT NULL 
                  AND essay_criteria_analysis IS NOT NULL
                  AND deleted_at IS NULL
            """
            cursor.execute(query, (question_id, user_id))
            result = cursor.fetchone()
            return result is not None
    except Exception as e:
        logger.error(f"Error checking if question is graded: {e}")
        return False
    finally:
        if conn:
            conn.close()

def is_empty_answer(exam_response):
    """답변이 비어있는지 검사합니다"""

    if not exam_response:
        return True

    text_answer = exam_response.get('text_answer')
    if not text_answer:  # None or ""
        return True

    if not text_answer.strip():  # 공백만 있는 경우
        return True

    return False


def get_prompt_path():
    """환경변수 설정에 따라 프롬프트 파일 경로를 가져옵니다."""
    # 환경변수에서 프롬프트 버전 가져오기
    prompt_version = os.environ.get('PROMPT_VERSION', 'latest')

    # 사용할 프롬프트 파일 결정
    prompt_dir = os.path.join(
        os.path.dirname(__file__), '..', '..', 'prompts', 'grade_exam_essay',
    )

    # 'latest'가 지정되거나 버전이 제공되지 않은 경우, 최신 버전 찾기
    if prompt_version == 'latest':
        # 프롬프트 디렉토리의 모든 파일 목록 가져오기
        prompt_files = [f for f in os.listdir(prompt_dir) if f.startswith('v') and f.endswith('.yaml')]

        if not prompt_files:
            raise ValueError(f"No prompt files found in {prompt_dir}")

        # 버전 번호로 파일 정렬 (v1.yaml, v2.yaml 등) 후 가장 높은 버전 선택
        prompt_files.sort(key=lambda x: int(x.replace('v', '').replace('.yaml', '')))
        prompt_file = prompt_files[-1]
        logger.info(f"Using latest prompt version: {prompt_file}")
    else:
        # 지정된 버전 사용
        prompt_file = f"v{prompt_version}.yaml"
        if not os.path.exists(os.path.join(prompt_dir, prompt_file)):
            raise ValueError(f"Prompt version {prompt_version} not found")
        logger.info(f"Using specified prompt version: {prompt_file}")

    return os.path.join(prompt_dir, prompt_file)


def update_exam_response_scoring(question_id, user_id, score, essay_criteria_analysis):
    """exam_responses 테이블의 채점 결과를 업데이트합니다."""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor() as cursor:
            query = """
                UPDATE app.exam_responses
                SET score = %s, 
                    essay_criteria_analysis = %s,
                    updated_at = NOW()
                WHERE question_id = %s AND user_id = %s
            """
            cursor.execute(query, (score, json.dumps(essay_criteria_analysis, ensure_ascii=False), question_id, user_id))

        conn.commit()
        logger.info(f"Exam response scoring updated: question_id={question_id}, user_id={user_id}, score={score}")

    except Exception as e:
        if conn:
            conn.rollback()
        logger.error(f"Error updating exam response scoring: {e}")
        raise
    finally:
        if conn:
            conn.close()


def update_exam_result_score(exam_id, user_id, additional_score):
    """exam_result 테이블의 사용자 총획득점수를 업데이트합니다 (기존 점수에 추가)."""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor() as cursor:
            update_query = """
                UPDATE app.exam_results
                SET score = COALESCE(score, 0) + %s,
                    updated_at = NOW()
                WHERE exam_id = %s 
                  AND user_id = %s 
                  AND deleted_at IS NULL
            """
            cursor.execute(update_query, (additional_score, exam_id, user_id))

            if cursor.rowcount == 0:
                logger.warning(f"Exam result not found: exam_id={exam_id}, user_id={user_id}")
            else:
                logger.info(f"Exam result score updated: exam_id={exam_id}, user_id={user_id}, added_score={additional_score}")

        conn.commit()

    except Exception as e:
        if conn:
            conn.rollback()
        logger.error(f"Error updating exam result score: {e}")
        raise
    finally:
        if conn:
            conn.close()


def update_exam_status_to_graded(exam_id):
    """시험 상태를 'graded'로 업데이트합니다."""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor() as cursor:
            update_query = """
                UPDATE app.exams
                SET status = 'graded',
                    updated_at = NOW()
                WHERE id = %s 
                  AND deleted_at IS NULL
            """
            cursor.execute(update_query, (exam_id,))

            if cursor.rowcount == 0:
                logger.warning(f"Exam not found for status update: exam_id={exam_id}")
            else:
                logger.info(f"Exam status updated to graded: exam_id={exam_id}")

        conn.commit()

    except Exception as e:
        if conn:
            conn.rollback()
        logger.error(f"Error updating exam status: {e}")
        raise
    finally:
        if conn:
            conn.close()


def grade_single_question(openai_client, prompt_path, exam_item, exam_response, user_id):
    """단일 서술형 문항을 채점합니다."""
    question_id = exam_item['id']
    epsilon = 0.01  # 점수 비교에 사용할 허용 오차

    # 이미 채점된 문항인지 확인 (추후 문항별 재시도 로직 도입 시 중복 처리 방지)
    if is_question_already_graded(question_id, user_id):
        logger.info(f"Question {question_id} already graded, skipping")
        return 0.0

    try:
        # 답변이 비어있을 경우
        if is_empty_answer(exam_response):
            logger.warning(f"No meaningful text answer provided for question: {question_id}")
            empty_criteria_result = {
                "criteria": [],
                "analysis": "빈 답변은 0점으로 처리됩니다."
            }
            update_exam_response_scoring(question_id, user_id, 0.0, empty_criteria_result)
            return 0.0

        # OpenAI를 통한 채점 진행
        grade_result = openai_client.grade_exam_essay(
            question=exam_item['question'],
            model_answer=exam_item['text_answer'],
            explanation=exam_item['explanation'],
            student_answer=exam_response['text_answer'],
            max_points=exam_item['points'],
            prompt_path=prompt_path
        )

        # 점수 일관성 검증 (평가항목별 점수 합계와 score로 표시해준 점수 일치여부 확인: gpt가 실수할 수 있음)
        criteria_total = sum(c['earned_points'] for c in grade_result['essay_criteria_analysis']['criteria'])
        if abs(criteria_total - grade_result['score']) > epsilon:
            logger.warning(f"Score mismatch: criteria_total={criteria_total}, score={grade_result['score']}")
            grade_result['score'] = criteria_total

        # exam_responses 테이블 업데이트 (점수 + 채점 기준 결과)
        update_exam_response_scoring(
            question_id,
            user_id,
            grade_result['score'],
            grade_result['essay_criteria_analysis']
        )

        logger.info(f"Successfully evaluated question {question_id} for user {user_id}, score: {grade_result['score']}")
        return grade_result['score']

    except Exception as e:
        logger.error(f"Error evaluating question {question_id}: {e}")
        logger.error(traceback.format_exc())

        # 시스템 오류 시 학생에게 불이익이 없도록 만점 부여
        max_points = exam_item['points']
        error_criteria_result = {
            "criteria": [
                {
                    "name": "시스템 오류 보상",
                    "description": "채점 시스템 오류로 인한 만점 보상",
                    "max_points": max_points,
                    "earned_points": max_points
                }
            ],
            "analysis": "채점 중 시스템 오류가 발생하여 만점으로 처리되었습니다. 정확한 채점을 원하시면 아래 오류 신고를 통해 알려주세요."
        }
        update_exam_response_scoring(
            question_id,
            user_id,
            max_points,
            error_criteria_result
        )

        logger.warning(f"Question {question_id} grading failed - awarded full points ({max_points}) but not counting as completed for retry possibility")
        return max_points


def get_course_id_from_exam(exam_id):
    """시험으로부터 과목 ID를 가져옵니다."""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor() as cursor:
            query = """
                SELECT course_id
                FROM app.exams
                WHERE id = %s AND deleted_at IS NULL
            """
            cursor.execute(query, (exam_id,))
            result = cursor.fetchone()

            if result:
                return result[0]
            else:
                logger.error(f"Course not found for exam: {exam_id}")
                return None

    except Exception as e:
        logger.error(f"Error getting course ID from exam: {e}")
        raise
    finally:
        if conn:
            conn.close()


def send_generate_course_weakness_analysis_message(user_id, exam_id, course_id):
    """과목 약점 분석을 위한 SQS 메시지를 전송합니다."""
    try:
        message = {
            "schema_version": "1.0.0",
            "request_id": str(uuid.uuid4()),
            "occurred_at": datetime.utcnow().isoformat() + "Z",
            "user_id": str(user_id),
            "quiz_id": None,  # 시험인 경우 None
            "exam_id": str(exam_id),
            "course_id": str(course_id)
        }

        # SQS 큐 URL
        queue_url = os.environ.get('GENERATE_COURSE_WEAKNESS_ANALYSIS_QUEUE_URL')
        if not queue_url:
            logger.error("GENERATE_COURSE_WEAKNESS_ANALYSIS_QUEUE_URL environment variable not set")
            return

        # 메시지 전송
        response = sqs_client.send_message(
            QueueUrl=queue_url,
            MessageGroupId=str(user_id),
            MessageBody=json.dumps(message)
        )

        logger.info(f"Successfully sent generate course weakness analysis message: exam_id={exam_id}, user_id={user_id}, course_id={course_id}, message_id={response['MessageId']}")

    except Exception as e:
        logger.error(f"Failed to send generate course weakness analysis message: exam_id={exam_id}, user_id={user_id}, error={e}")


def lambda_handler(event, context):
    """Main function to handle the event from SQS - 한 시험의 모든 서술형 문항 처리"""
    try:
        for record in event.get('Records', []):
            message = json.loads(record['body'])

            # SQS 메세지에서 user_id와 exam_id 추출
            user_id = message.get('user_id')
            exam_id = message.get('exam_id')

            if not all([user_id, exam_id]):
                logger.error("Missing required fields in message: user_id or exam_id")
                continue

            logger.info(f"Processing exam evaluation for exam taker: exam_id={exam_id}, user_id={user_id}")

            # 1. 시험의 모든 서술형 문항 조회
            essay_items = get_exam_essay_items(exam_id)
            if not essay_items:
                logger.warning(f"No essay questions found for exam: {exam_id}")
                continue

            logger.info(f"Found {len(essay_items)} essay questions to evaluate for user {user_id}")

            # 2. OpenAI 클라이언트 및 프롬프트 설정 초기화
            openai_client = OpenAIClient()
            prompt_path = get_prompt_path()

            # 3. 각 서술형 문항에 대해 순차적으로 채점
            total_essay_score = 0.0

            for exam_item in essay_items:
                question_id = exam_item['id']
                logger.info(f"Processing question {question_id}")

                # 사용자 답변 조회
                exam_response = get_exam_response_data(question_id, user_id)
                if not exam_response:
                    logger.error(f"System error - Exam response not found for question: {question_id}, user: {user_id}")
                    continue

                # 단일 문항 채점
                question_score = grade_single_question(
                    openai_client,
                    prompt_path,
                    exam_item,
                    exam_response,
                    user_id
                )

                total_essay_score += question_score

            # 4. exam_result 테이블 업데이트 (시험 총 점수)
            update_exam_result_score(exam_id, user_id, total_essay_score)

            # 5. 시험 상태를 'graded'로 업데이트
            update_exam_status_to_graded(exam_id)
            logger.info(f"Exam evaluation completed for exam {exam_id}, user {user_id}, total score added: {total_essay_score}")

            # 6. 과목 약점 분석 SQS 메시지 전송
            course_id = get_course_id_from_exam(exam_id)
            if course_id:
                send_generate_course_weakness_analysis_message(user_id, exam_id, course_id)
            else:
                logger.warning(f"Could not send weakness analysis message - course_id not found for exam: {exam_id}")


        return {
            'statusCode': 200,
            'body': json.dumps('Exam evaluation completed successfully')
        }

    except Exception as e:
        logger.error(f"Error in lambda_handler: {e}")
        logger.error(traceback.format_exc())

        return {
            'statusCode': 500,
            'body': json.dumps(f'Error evaluating exam: {str(e)}')
        }