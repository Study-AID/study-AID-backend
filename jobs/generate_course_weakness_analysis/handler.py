import boto3
import json
import logging
import os
import psycopg2
import psycopg2.extras
import traceback
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

# Initialize client
sqs_client = boto3.client('sqs', region_name='ap-northeast-2')


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


def get_course_info(course_id):
    """과목 정보와 기존 약점 분석을 가져옵니다."""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor() as cursor:
            query = """
                SELECT name, course_weakness_analysis 
                FROM app.courses 
                WHERE id = %s AND deleted_at IS NULL
            """
            cursor.execute(query, (course_id,))
            result = cursor.fetchone()

            if result:
                return {
                    'name': result[0],
                    'existing_analysis': result[1]
                }
            return None
    except Exception as e:
        logger.error(f"Error getting course info: {e}")
        raise
    finally:
        if conn:
            conn.close()


def get_new_incorrect_items_from_quiz(quiz_id, user_id):
    """퀴즈의 틀린 문제들을 가져옵니다."""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cursor:
            query = """
                SELECT 
                    qi.question, 
                    qi.explanation, 
                    qi.question_type,
                    qi.is_true_answer,
                    qi.choices,
                    qi.answer_indices,
                    qi.text_answer as correct_text_answer,
                    qr.selected_bool,
                    qr.selected_indices,
                    qr.text_answer as user_text_answer,
                    qr.score,
                    qi.points,
                    q.title as quiz_title,
                    qr.essay_criteria_analysis
                FROM app.quiz_items qi
                JOIN app.quiz_responses qr ON qi.id = qr.question_id
                JOIN app.quizzes q ON qi.quiz_id = q.id
                WHERE q.id = %s AND qr.user_id = %s 
                  AND qi.deleted_at IS NULL
                  AND qr.deleted_at IS NULL
                  AND (
                    -- OX/객관식/주관식: is_correct가 FALSE인 경우
                    (qi.question_type IN ('true_or_false', 'multiple_choice', 'short_answer') 
                     AND qr.is_correct = FALSE)
                    OR
                    -- 서술형: essay_criteria_analysis가 있으면 무조건 포함
                    (qi.question_type = 'essay' 
                     AND qr.essay_criteria_analysis IS NOT NULL)
                  )
                ORDER BY qi.display_order
            """
            cursor.execute(query, (quiz_id, user_id))
            return [dict(item) for item in cursor.fetchall()]
    except Exception as e:
        logger.error(f"Error getting incorrect items from quiz: {e}")
        raise
    finally:
        if conn:
            conn.close()


def get_new_incorrect_items_from_exam(exam_id, user_id):
    """모의시험의 틀린 문제들을 가져옵니다."""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cursor:
            query = """
                SELECT 
                    ei.question, 
                    ei.explanation, 
                    ei.question_type,
                    ei.is_true_answer,
                    ei.choices,
                    ei.answer_indices,
                    ei.text_answer as correct_text_answer,
                    er.selected_bool,
                    er.selected_indices,
                    er.text_answer as user_text_answer,
                    er.score,
                    ei.points,
                    e.title as exam_title,
                    er.essay_criteria_analysis
                FROM app.exam_items ei
                JOIN app.exam_responses er ON ei.id = er.question_id
                JOIN app.exams e ON ei.exam_id = e.id
                WHERE e.id = %s AND er.user_id = %s
                  AND ei.deleted_at IS NULL
                  AND er.deleted_at IS NULL
                  AND (
                    -- OX/객관식/주관식: is_correct가 FALSE인 경우
                    (ei.question_type IN ('true_or_false', 'multiple_choice', 'short_answer') 
                     AND er.is_correct = FALSE)
                    OR
                    -- 서술형: essay_criteria_analysis가 있으면 무조건 포함
                    (ei.question_type = 'essay' 
                     AND er.essay_criteria_analysis IS NOT NULL)
                  )
                ORDER BY ei.display_order
            """
            cursor.execute(query, (exam_id, user_id))
            return [dict(item) for item in cursor.fetchall()]
    except Exception as e:
        logger.error(f"Error getting incorrect items from exam: {e}")
        raise
    finally:
        if conn:
            conn.close()

def format_incorrect_items(items, source_type):
    """틀린 문제들을 분석용으로 간단하게 포맷팅합니다."""
    if not items:
        return ""

    source = items[0].get('quiz_title', items[0].get('exam_title', ''))
    formatted_items = [f"[{source_type}: {source}]"]

    for i, item in enumerate(items, 1):
        # 서술형은 간단히 분석만 가져오기
        if item['question_type'] == 'essay':
            essay_analysis = ""
            if item.get('essay_criteria_analysis'):
                try:
                    essay_data = json.loads(item['essay_criteria_analysis']) if isinstance(item['essay_criteria_analysis'], str) else item['essay_criteria_analysis']
                    essay_analysis = essay_data.get('analysis', '')
                except:
                    essay_analysis = "채점 결과 분석 오류"

            formatted_item = f"문제{i}|질문:{item['question']}|학습목표/설명:{item['explanation']}|사용자답안평가:{essay_analysis}"
        else:
            # 객관식/OX/단답형
            # 사용자 답안
            user_answer = ""
            if item.get('user_text_answer'):
                user_answer = item['user_text_answer']
            elif item.get('selected_bool') is not None:
                user_answer = str(item['selected_bool'])
            elif item.get('selected_indices'):
                choices = item.get('choices', [])
                if choices:
                    try:
                        selected = [choices[i] for i in item['selected_indices'] if i < len(choices)]
                        user_answer = ', '.join(selected)
                    except (TypeError, IndexError):
                        user_answer = ""
            # 정답
            correct_answer = ""
            if item.get('correct_text_answer'):
                correct_answer = item['correct_text_answer']
            elif item.get('is_true_answer') is not None:
                correct_answer = str(item['is_true_answer'])
            elif item.get('answer_indices') and item.get('choices'):
                choices = item['choices']
                try:
                    correct = [choices[i] for i in item['answer_indices'] if i < len(choices)]
                    correct_answer = ', '.join(correct)
                except (TypeError, IndexError):
                    correct_answer = ""

            formatted_item = f"문제{i}|질문:{item['question']}|학생답변:{user_answer}|정답:{correct_answer}|해설:{item['explanation']}"

        formatted_items.append(formatted_item)

    return "\n\n".join(formatted_items)


def get_prompt_path():
    """환경변수 설정에 따라 프롬프트 파일 경로를 가져옵니다."""
    # 환경변수에서 프롬프트 버전 가져오기
    prompt_version = os.environ.get('PROMPT_VERSION', 'latest')

    # 사용할 프롬프트 파일 결정
    prompt_dir = os.path.join(
        os.path.dirname(__file__), '..', '..', 'prompts', 'generate_course_weakness_analysis',
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


def merge_analysis(existing_analysis, new_analysis):
    """기존 분석과 새로운 분석을 통합합니다."""
    try:
        if existing_analysis:
            if isinstance(existing_analysis, str):
                existing_data = json.loads(existing_analysis)
            else:
                existing_data = existing_analysis

            existing_weaknesses = existing_data.get('weaknesses', '')
            existing_suggestions = existing_data.get('suggestions', '')
        else:
            existing_weaknesses = ''
            existing_suggestions = ''

        merged_weaknesses = merge_text(existing_weaknesses, new_analysis.get('weaknesses', ''))
        merged_suggestions = merge_text(existing_suggestions, new_analysis.get('suggestions', ''))

        return {
            'weaknesses': merged_weaknesses,
            'suggestions': merged_suggestions,
            'analyzed_at': datetime.now().isoformat()
        }

    except Exception as e:
        logger.warning(f"Error merging analysis, using new analysis only: {e}")
        new_analysis['analyzed_at'] = datetime.now().isoformat()
        return new_analysis


def merge_text(existing_text, new_text):
    """기존 텍스트와 새로운 텍스트를 자연스럽게 통합합니다."""
    if not existing_text:
        return new_text
    if not new_text:
        return existing_text

    return f"{existing_text} {new_text}"


def update_course_weakness_analysis(course_id, analysis_result):
    """과목의 약점 분석을 DB에 업데이트합니다."""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor() as cursor:
            query = """
                UPDATE app.courses 
                SET course_weakness_analysis = %s, updated_at = NOW()
                WHERE id = %s AND deleted_at IS NULL
            """
            cursor.execute(query, (json.dumps(analysis_result, ensure_ascii=False), course_id))
        conn.commit()
        logger.info(f"Course weakness analysis updated for course_id: {course_id}")
    except Exception as e:
        if conn:
            conn.rollback()
        logger.error(f"Error updating course weakness analysis: {e}")
        raise
    finally:
        if conn:
            conn.close()

def lambda_handler(event, context):
    try:
        for record in event.get('Records', []):
            message = json.loads(record['body'])

            # SQS 메시지에서 필수 필드 추출
            user_id = message.get('user_id')
            course_id = message.get('course_id')
            quiz_id = message.get('quiz_id')
            exam_id = message.get('exam_id')

            if not user_id or not course_id:
                logger.error("Missing required fields: user_id, course_id")
                continue

            if not quiz_id and not exam_id:
                logger.error("Missing quiz_id or exam_id")
                continue

            # 새로운 틀린 문제들 가져오기
            if quiz_id:
                source_type = "퀴즈"
                new_incorrect_items = get_new_incorrect_items_from_quiz(quiz_id, user_id)
            else:
                source_type = "모의시험"
                new_incorrect_items = get_new_incorrect_items_from_exam(exam_id, user_id)

            logger.info(f"Processing weakness analysis for course {course_id}, triggered by {source_type}")

            # 새로운 틀린 문제가 없으면 스킵
            if not new_incorrect_items:
                logger.info("No new incorrect items found, skipping analysis")
                continue

            # 과목 정보와 기존 분석 가져오기
            course_info = get_course_info(course_id)
            if not course_info:
                logger.error(f"Course not found: {course_id}")
                continue

            # 새로운 틀린 문제들 포맷팅
            formatted_new_items = format_incorrect_items(new_incorrect_items, source_type)

            # OpenAI 클라이언트 및 프롬프트 설정
            openai_client = OpenAIClient()
            prompt_path = get_prompt_path()

            # 새로운 분석만 요청
            new_analysis = openai_client.analyze_new_weakness(
                course_name=course_info['name'],
                new_incorrect_items=formatted_new_items,
                prompt_path=prompt_path
            )

            # 기존 분석과 통합
            final_result = merge_analysis(course_info['existing_analysis'], new_analysis)

            # 결과 저장
            update_course_weakness_analysis(course_id, final_result)

            logger.info(f"Incremental weakness analysis completed for course {course_id}")

        return {
            'statusCode': 200,
            'body': json.dumps('Weakness analysis completed successfully')
        }

    except Exception as e:
        logger.error(f"Error in lambda_handler: {e}")
        logger.error(traceback.format_exc())
        return {
            'statusCode': 500,
            'body': json.dumps(f'Error in weakness analysis: {str(e)}')
        }