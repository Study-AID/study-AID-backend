import json
import logging
import os
import traceback
import uuid
from datetime import datetime

import boto3
import psycopg2
import psycopg2.extras
from botocore.config import Config

from openai_client import OpenAIClient

# Register UUID adapter for psycopg2
psycopg2.extras.register_uuid()

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Constants and configurations
# AWS S3 configuration
s3_endpoint_url = os.environ.get('AWS_ENDPOINT_URL')
s3_client = boto3.client(
    's3',
    endpoint_url=s3_endpoint_url,
    aws_access_key_id=os.environ.get('AWS_ACCESS_KEY_ID', None),
    aws_secret_access_key=os.environ.get('AWS_SECRET_ACCESS_KEY', None),
    region_name=os.environ.get('AWS_REGION', 'ap-northeast-2'),
    config=Config(
        connect_timeout=10,
        retries={'max_attempts': 3}
    )
)

# Database configuration
DB_CONFIG = {
    'host': os.environ.get('DB_HOST'),
    'user': os.environ.get('DB_USER'),
    'password': os.environ.get('DB_PASSWORD'),
    'database': os.environ.get('DB_NAME'),
    'port': int(os.environ.get('DB_PORT'))
}


def get_db_connection():
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


def get_course_lectures(course_id):
    """Get all lectures for a course"""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cursor:
            query = """
            SELECT id, title, material_path, summary
            FROM app.lectures
            WHERE course_id = %s AND deleted_at IS NULL
            ORDER BY display_order_lex
            """
            cursor.execute(query, (course_id,))
            lectures = cursor.fetchall()

            return [dict(lecture) for lecture in lectures]
    except Exception as e:
        logger.error(f"Error getting course lectures: {e}")
        raise
    finally:
        if conn:
            conn.close()


def get_prompt_path():
    """Get the prompt file path based on environment variable settings"""
    # Get prompt version from environment variable
    prompt_version = os.environ.get('PROMPT_VERSION', 'latest')

    # Determine the prompt file to use
    prompt_dir = os.path.join(
        os.path.dirname(__file__), '..', '..', 'prompts', 'generate_exam',
    )

    # If 'latest' is specified or no version is provided, find the latest version
    if prompt_version == 'latest':
        # List all files in the prompt directory
        prompt_files = [f for f in os.listdir(prompt_dir) if f.startswith('v') and f.endswith('.yaml')]

        if not prompt_files:
            raise ValueError(f"No prompt files found in {prompt_dir}")

        # Sort files by version number (v1.yaml, v2.yaml, etc.) and get the highest version
        prompt_files.sort(key=lambda x: int(x.replace('v', '').replace('.yaml', '')))
        prompt_file = prompt_files[-1]
        logger.info(f"Using latest prompt version: {prompt_file}")
    else:
        # Use the specified version
        prompt_file = f"v{prompt_version}.yaml"
        if not os.path.exists(os.path.join(prompt_dir, prompt_file)):
            raise ValueError(f"Prompt version {prompt_version} not found")
        logger.info(f"Using specified prompt version: {prompt_file}")

    return os.path.join(prompt_dir, prompt_file)


def get_lecture_content(lecture_info):
    """Get lecture content either from summary or from the original file"""
    try:
        # First check if we have a summary
        summary = lecture_info.get('summary')
        if summary and isinstance(summary, dict) and 'content' in summary:
            logger.info(f"Using summary for lecture_id: {lecture_info['id']}")
            return summary['content']

        # If no summary, get the content from the original file
        s3_key = lecture_info.get('material_path')
        if not s3_key:
            raise ValueError(f"No material path found for lecture: {lecture_info['id']}")

        # Download from S3
        s3_bucket = os.environ.get('S3_BUCKET', 'study-aid-materials')  # Use environment variable or default
        local_file_path = f"/tmp/{os.path.basename(s3_key)}"

        if s3_endpoint_url:
            logger.info(f"Using S3 endpoint URL: {s3_endpoint_url}")

        # Download from S3
        logger.info(f"Downloading file from s3://{s3_bucket}/{s3_key} to {local_file_path}")
        s3_client.download_file(s3_bucket, s3_key, local_file_path)

        # Extract text from PDF
        content = extract_text_from_pdf(local_file_path)

        # Clean up
        os.remove(local_file_path)

        return content

    except Exception as e:
        logger.error(f"Error getting lecture content: {e}")
        raise


def extract_text_from_pdf(file_path):
    """Extract text content from PDF file"""
    try:
        import fitz  # PyMuPDF

        text = ""
        with fitz.open(file_path) as doc:
            for page in doc:
                text += page.get_text()

        return text
    except Exception as e:
        logger.error(f"Error extracting text from PDF: {e}")
        raise


def save_exam_to_db(course_id, user_id, exam_data, title=None, referenced_lectures=None):
    """Save the generated exam to the database using bulk insert"""
    conn = None
    try:
        conn = get_db_connection()

        # Generate a UUID for the exam
        exam_id = str(uuid.uuid4())

        # Default title if not provided
        if not title:
            title = f"Exam on {datetime.now().strftime('%Y-%m-%d %H:%M')}"

        with conn.cursor() as cursor:
            query = """
            INSERT INTO app.exams 
            (id, course_id, user_id, title, status, referenced_lectures, contents_generated_at, created_at)
            VALUES (%s, %s, %s, %s, %s, %s::uuid[], NOW(), NOW())
            """
            cursor.execute(query, (exam_id, course_id, user_id, title, 'not_started', referenced_lectures))

            # Prepare bulk data for exam items
            bulk_values = []
            value_params = []

            # Process each question from the exam_questions array
            if 'exam_questions' in exam_data and exam_data['exam_questions']:
                for idx, exam_item in enumerate(exam_data['exam_questions']):
                    # Create question UUID
                    question_id = str(uuid.uuid4())

                    # Prepare question data
                    question_text = exam_item.get('question')
                    question_type = exam_item.get('question_type', 'multiple_choice')
                    explanation = exam_item.get('explanation', '')
                    points = exam_item.get('points', 10.0)

                    # Initialize fields
                    choices = []
                    answer_indices = None
                    is_true_answer = None
                    text_answer = None

                    # Process question based on type
                    if question_type == 'multiple_choice':
                        # Get options and correct answer for multiple choice
                        options = exam_item.get('options', [])
                        answer_indices = []  # Initialize as list for multiple choice
                        for opt_idx, option in enumerate(options):
                            choices.append(option.get('text', ''))
                            if option.get('is_correct'):
                                answer_indices.append(opt_idx)

                    elif question_type == 'true_or_false':
                        is_true_answer = exam_item.get('answer', False)

                    elif question_type == 'short_answer' or question_type == 'essay':
                        text_answer = exam_item.get('answer', '')

                    # Add to bulk values
                    bulk_values.append("(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW())")
                    value_params.extend([
                        question_id,
                        exam_id,
                        user_id,
                        question_text,
                        question_type,
                        explanation,
                        is_true_answer,
                        choices,
                        answer_indices,
                        text_answer,
                        idx,  # Simple sequential display order
                        points
                    ])

            # Perform bulk insert
            if bulk_values:
                query = f"""
                INSERT INTO app.exam_items 
                (id, exam_id, user_id, question, question_type, explanation, 
                 is_true_answer, choices, answer_indices, text_answer, 
                 display_order, points, created_at)
                VALUES {', '.join(bulk_values)}
                """
                logger.info(f"Executing bulk insert with {len(bulk_values)} questions")
                cursor.execute(query, value_params)
                logger.info("Bulk insert completed successfully")
            else:
                logger.warning("No exam items to insert!")

        conn.commit()
        logger.info(f"Exam created with {exam_id} for course_id: {course_id}")
        return exam_id

    except Exception as e:
        if conn:
            conn.rollback()
        logger.error(f"Error saving exam to database: {e}")
        raise
    finally:
        if conn:
            conn.close()


def log_activity(course_id, user_id, activity_type, contents_type, details):
    """Log activity for the course"""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor() as cursor:
            activity_id = str(uuid.uuid4())
            activity_details = json.dumps(details)

            query = """
            INSERT INTO app.course_activity_logs 
            (id, course_id, user_id, activity_type, contents_type, activity_details)
            VALUES (%s, %s, %s, %s, %s, %s)
            """
            cursor.execute(query, (
                activity_id,
                course_id,
                user_id,
                activity_type,
                contents_type,
                activity_details
            ))

        conn.commit()
        logger.info(f"Activity logged for course_id: {course_id}")
    except Exception as e:
        logger.error(f"Error logging activity: {e}")
        # Non-critical operation, continue execution
        if conn:
            conn.rollback()


def compile_course_content(course_id):
    """Compile content from all lectures in a course"""
    lectures = get_course_lectures(course_id)

    compiled_content = ""
    for lecture in lectures:
        try:
            lecture_info = {
                'id': lecture['id'],
                'material_path': lecture['material_path'],
                'summary': lecture['summary']
            }
            content = get_lecture_content(lecture_info)

            # Add a header for each lecture
            compiled_content += f"\n\n## {lecture['title']}\n\n"
            compiled_content += content
        except Exception as e:
            logger.warning(f"Error getting content for lecture {lecture['id']}: {e}")
            # Continue with other lectures

    return compiled_content


def get_reference_lectures(reference_lecture_ids):
    """Get content from reference lectures using a single query"""
    if not reference_lecture_ids or not isinstance(reference_lecture_ids, list):
        return ""

    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cursor:
            # Using parameterized query with any operator
            query = """
            SELECT l.id, l.course_id, l.user_id, l.material_path, l.title, l.summary
            FROM app.lectures l
            WHERE l.id = ANY(%s::uuid[]) AND l.deleted_at IS NULL
            ORDER BY l.id
            """
            cursor.execute(query, (reference_lecture_ids,))
            lectures = cursor.fetchall()
            lectures = [dict(lecture) for lecture in lectures]

        reference_content = """\n\n# Reference Materials:\n"""

        for lecture in lectures:
            try:
                content = get_lecture_content(lecture)

                # Add a header for each reference lecture
                reference_content += f"\n\n## {lecture['title']}\n\n"
                reference_content += content
            except Exception as e:
                logger.warning(f"Error getting content for reference lecture {lecture['id']}: {e}")
                # Continue with other lectures

        return reference_content
    except Exception as e:
        logger.error(f"Error getting reference lectures: {e}")
        raise
    finally:
        if conn:
            conn.close()


def lambda_handler(event, context):
    """Main function to handle the event from SQS"""
    try:
        # Process SQS message
        for record in event.get('Records', []):
            # Parse message body
            message = json.loads(record['body'])

            # Extract information from the message
            user_id = message.get('user_id')
            course_id = message.get('course_id')
            exam_title = message.get('title')
            referenced_lecture_ids = message.get('referenced_lecture_ids', [])

            # Exam configuration
            question_counts = {
                'true_or_false_count': message.get('true_or_false_count', 3),
                'multiple_choice_count': message.get('multiple_choice_count', 3),
                'short_answer_count': message.get('short_answer_count', 3),
                'essay_count': message.get('essay_count', 3),
            }

            # Referenced lecture IDs must be provided
            if not referenced_lecture_ids:
                logger.error("Missing required referenced_lectures in message")
                continue

            # Default title if not provided
            if not exam_title:
                exam_title = f"Exam on {datetime.now().strftime('%Y-%m-%d %H:%M')}"

            # Get content from referenced lectures
            lecture_content = get_reference_lectures(referenced_lecture_ids)

            # Generate exam using OpenAI
            openai_client = OpenAIClient()
            prompt_path = get_prompt_path()
            exam_data = openai_client.generate_exam(lecture_content, question_counts, prompt_path)

            # Save exam to database
            exam_id = save_exam_to_db(course_id, user_id, exam_data, exam_title, referenced_lecture_ids)

            # Log the activity
            activity_details = {
                "action": "generate_quiz",
                "course_id": course_id,
                "exam_id": exam_id,
                "exam_title": exam_title,
                "true_or_false_count": question_counts['true_or_false_count'],
                "multiple_choice_count": question_counts['multiple_choice_count'],
                "short_answer_count": question_counts['short_answer_count'],
                "essay_count": question_counts['essay_count'],
            }
            log_activity(course_id, user_id, 'create', 'exam', activity_details)

            logger.info(f"Successfully generated exam {exam_id} for course {course_id}")

        return {
            'statusCode': 200,
            'body': json.dumps('Exam generation completed successfully')
        }

    except Exception as e:
        logger.error(f"Error in lambda_handler: {e}")
        logger.error(traceback.format_exc())

        return {
            'statusCode': 500,
            'body': json.dumps(f'Error generating exam: {str(e)}')
        }
