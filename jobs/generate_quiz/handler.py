import boto3
import json
import logging
import os
import psycopg2
import psycopg2.extras
import traceback
import uuid
from datetime import datetime
from typing import List, Dict, Any, Optional

from openai_client import OpenAIClient
from parsed_text_models import ParsedPage, ParsedText

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Initialize clients
s3_endpoint_url = os.environ.get('AWS_ENDPOINT_URL')
s3_client = boto3.client('s3', region_name=os.environ.get('AWS_REGION', 'ap-northeast-2'))

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


def get_prompt_path():
    """Get the prompt file path based on environment variable settings"""
    # Get prompt version from environment variable
    prompt_version = os.environ.get('PROMPT_VERSION', 'latest')

    # Determine the prompt file to use
    prompt_dir = os.path.join(
        os.path.dirname(__file__), '..', '..', 'prompts', 'generate_quiz',
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


def get_lecture_info(lecture_id):
    """Get lecture information from the database"""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor(cursor_factory=psycopg2.extras.DictCursor) as cursor:
            query = """
                    SELECT l.id, l.course_id, l.user_id, l.material_path, l.title, l.parsed_text, c.id as course_id
                    FROM app.lectures l
                             JOIN app.courses c ON l.course_id = c.id
                    WHERE l.id = %s
                      AND l.deleted_at IS NULL 
                    """
            cursor.execute(query, (lecture_id,))
            lecture = cursor.fetchone()

            if not lecture:
                raise ValueError(f"No lecture found with id: {lecture_id}")

            return dict(lecture)
    except Exception as e:
        logger.error(f"Error getting lecture info: {e}")
        raise
    finally:
        if conn:
            conn.close()


def process_parsed_text(parsed_text_json: str) -> str:
    """Process parsed_text JSON into a text string for quiz generation"""
    try:
        # Either parse the JSON string or use it directly if it's already a dict
        if isinstance(parsed_text_json, str):
            parsed_text_data = json.loads(parsed_text_json)
        else:
            parsed_text_data = parsed_text_json

        # Validate the parsed_text data against our model
        parsed_text = ParsedText.model_validate(parsed_text_data)

        # Combine all page texts into a single string
        combined_text = ""
        for page in parsed_text.pages:
            combined_text += f"\n\n--- Page {page.page_number} ---\n\n"
            combined_text += page.text

        return combined_text
    except Exception as e:
        logger.error(f"Error processing parsed_text: {e}")
        raise


def get_lecture_content(lecture_id):
    """Get lecture content from parsed_text from the lecture record"""
    try:
        # Get lecture information from the database
        lecture_info = get_lecture_info(lecture_id)
        
        # Check if we have parsed_text in the lecture info
        if lecture_info.get('parsed_text'):
            logger.info(f"Using parsed_text from database for lecture_id: {lecture_info['id']}")
            return process_parsed_text(lecture_info['parsed_text'])

        # If no parsed_text, get the content from the original file
        s3_key = lecture_info.get('material_path')
        if not s3_key:
            raise ValueError(f"No material path found for lecture: {lecture_info['id']}")

        # Download from S3 - we need to handle the S3 bucket (this would need to be configured)
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


def update_quiz_in_db(quiz_id, lecture_id, user_id, quiz_data, title=None):
    """Update the quiz in the database with the generated items"""
    conn = None
    value_params = []  # Initialize value_params at function level
    query = ""  # Initialize query at function level
    
    try:
        conn = get_db_connection()

        # Default title if not provided
        if not title:
            title = f"Quiz on {datetime.now().strftime('%Y-%m-%d %H:%M')}"

        with conn.cursor() as cursor:
            # First, check if the quiz exists
            query = """
                    SELECT id
                    FROM app.quizzes
                    WHERE id = %s 
                    """
            cursor.execute(query, (quiz_id,))
            existing_quiz = cursor.fetchone()

            if not existing_quiz:
                logger.error(f"Quiz with id {quiz_id} not found")
                raise ValueError(f"Quiz with id {quiz_id} not found")

            # Update quiz status and title
            query = """
                    UPDATE app.quizzes
                    SET title                 = %s,
                        status                = %s,
                        contents_generated_at = NOW(),
                        updated_at            = NOW()
                    WHERE id = %s 
                    """
            cursor.execute(query, (title, 'not_started', quiz_id))

            # Delete existing quiz items to replace with new ones
            query = """
                    DELETE
                    FROM app.quiz_items
                    WHERE quiz_id = %s 
                    """
            cursor.execute(query, (quiz_id,))

            # Debug: Log quiz data structure
            logger.info(f"Quiz data structure: {quiz_data}")

            # Prepare bulk data for quiz items
            bulk_values = []
            value_params = []  # Reset value_params

            # Process each question from the quiz_questions array
            if 'quiz_questions' in quiz_data and quiz_data['quiz_questions']:
                for idx, quiz_item in enumerate(quiz_data['quiz_questions']):
                    # Create question UUID
                    question_id = str(uuid.uuid4())

                    # Prepare question data
                    question_text = quiz_item.get('question')
                    question_type = quiz_item.get('question_type', 'multiple_choice')
                    explanation = quiz_item.get('explanation', '')

                    # Initialize fields
                    choices = []
                    answer_indices = None
                    is_true_answer = None
                    text_answer = None

                    # Process question based on type
                    if question_type == 'multiple_choice':
                        # Get options and correct answer for multiple choice
                        options = quiz_item.get('options', [])
                        answer_indices = []  # Initialize as list for multiple choice
                        for opt_idx, option in enumerate(options):
                            choices.append(option.get('text', ''))
                            if option.get('is_correct'):
                                answer_indices.append(opt_idx)

                    elif question_type == 'true_or_false':
                        is_true_answer = quiz_item.get('answer', False)

                    elif question_type == 'short_answer' or question_type == 'essay':
                        text_answer = quiz_item.get('answer', '')

                    # Add to bulk values
                    bulk_values.append("(%s, %s, %s, %s, %s, %s, %s, %s, %s, %s, %s, NOW())")
                    value_params.extend([
                        question_id,
                        quiz_id,
                        user_id,
                        question_text,
                        question_type,
                        explanation,
                        is_true_answer,
                        choices,
                        answer_indices,
                        text_answer,
                        idx
                    ])

            # Perform bulk insert
            if bulk_values:
                query = f"""
                INSERT INTO app.quiz_items 
                (id, quiz_id, user_id, question, question_type, explanation, 
                 is_true_answer, choices, answer_indices, text_answer, display_order,
                 created_at)
                VALUES {', '.join(bulk_values)}
                """
                logger.info(f"Executing bulk insert with {len(bulk_values)} questions")
                cursor.execute(query, value_params)
                logger.info("Bulk insert completed successfully")
            else:
                logger.warning("No quiz items to insert!")

        conn.commit()
        logger.info(f"Quiz updated in database with id: {quiz_id} for lecture_id: {lecture_id}")
        return quiz_id

    except Exception as e:
        if conn:
            conn.rollback()
        logger.error(f"Error updating quiz in database: {e}")
        logger.error(f"Error type: {type(e)}")
        logger.error(f"Error details: {str(e)}")
        # Log the actual query for debugging - now query and value_params are always defined
        logger.error(f"Failed query: {query}")
        if value_params:
            logger.error(f"Query params: {value_params}")
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
            lecture_id = message.get('lecture_id')
            quiz_title = message.get('title')
            quiz_id = message.get('quiz_id')  # Get the quiz_id for update

            # Get question counts by type with default values
            question_counts = {
                'true_or_false_count': message.get('true_or_false_count', 0),
                'multiple_choice_count': message.get('multiple_choice_count', 0),
                'short_answer_count': message.get('short_answer_count', 0),
                'essay_count': message.get('essay_count', 0)
            }

            # Ensure at least one question is requested
            if sum(question_counts.values()) == 0:
                logger.warning("No question counts specified, defaulting to 3 multiple choice questions")
                question_counts['multiple_choice_count'] = 3

            if not lecture_id:
                logger.error("Missing required lecture_id in message")
                continue

            if not quiz_id:
                logger.error("Missing required quiz_id in message")
                continue

            # Get lecture content from the parsed_text field in the lecture table
            lecture_content = get_lecture_content(lecture_id)

            # Generate quiz using OpenAI
            openai_client = OpenAIClient()
            prompt_path = get_prompt_path()
            quiz_data = openai_client.generate_quiz(lecture_content, question_counts, prompt_path)

            # Update quiz in database
            update_quiz_in_db(quiz_id, lecture_id, user_id, quiz_data, quiz_title)

            # Log the activity
            activity_details = {
                "action": "generate_quiz",
                "lecture_id": lecture_id,
                "quiz_id": quiz_id,
                "quiz_title": quiz_title,
                "true_or_false_count": question_counts['true_or_false_count'],
                "multiple_choice_count": question_counts['multiple_choice_count'],
                "short_answer_count": question_counts['short_answer_count'],
                "essay_count": question_counts['essay_count'],
            }
            log_activity(course_id, user_id, 'update', 'quiz', activity_details)

            logger.info(f"Successfully updated quiz {quiz_id} for lecture {lecture_id}")

        return {
            'statusCode': 200,
            'body': json.dumps('Quiz update completed successfully')
        }

    except Exception as e:
        logger.error(f"Error in lambda_handler: {e}")
        logger.error(traceback.format_exc())

        return {
            'statusCode': 500,
            'body': json.dumps(f'Error updating quiz: {str(e)}')
        }
