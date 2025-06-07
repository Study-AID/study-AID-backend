import json
import logging
import os
import traceback
import uuid
from datetime import datetime

import boto3
import psycopg2
import psycopg2.extras

from openai_client import OpenAIClient
from parsed_text_models import ParsedText
from pdf_chunker import PDFChunker

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Constants and configurations

# PDF chunking configuration
DEFAULT_CHUNK_SIZE = int(os.environ.get('DEFAULT_CHUNK_SIZE', '40'))
MAX_CONCURRENT_CHUNKS = int(os.environ.get('MAX_CONCURRENT_CHUNKS', '2'))

# TODO(jin): write default sender email and frontend domain
# AWS SES configuration
SES_SENDER_EMAIL=os.environ.get('SES_SENDER_EMAIL')

# Domain configuration
FRONTEND_DOMAIN = os.environ.get('FRONTEND_DOMAIN')

# Database configuration
DB_CONFIG = {
    'host': os.environ.get('DB_HOST'),
    'user': os.environ.get('DB_USER'),
    'password': os.environ.get('DB_PASSWORD'),
    'database': os.environ.get('DB_NAME'),
    'port': int(os.environ.get('DB_PORT'))
}

# Initialize clients
s3_client = boto3.client('s3', region_name=os.environ.get('AWS_REGION', 'ap-northeast-2'))
ses_client = boto3.client('ses', region_name=os.environ.get('AWS_REGION', 'ap-northeast-2'))

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


def get_lecture_parsed_text(lecture_id) -> ParsedText:
    """Get lecture content as ParsedText object from the lecture record"""
    try:
        # Get lecture information from the database
        lecture_info = get_lecture_info(lecture_id)

        # Check if we have parsed_text in the lecture info
        if lecture_info.get('parsed_text'):
            logger.info(f"Using parsed_text from database for lecture_id: {lecture_info['id']}")

            # Parse the JSON string or use it directly if it's already a dict
            if isinstance(lecture_info['parsed_text'], str):
                parsed_text_data = json.loads(lecture_info['parsed_text'])
            else:
                parsed_text_data = lecture_info['parsed_text']

            # Validate and return ParsedText object
            return ParsedText.model_validate(parsed_text_data)

        # If no parsed_text, extract from original file
        s3_key = lecture_info.get('material_path')
        if not s3_key:
            raise ValueError(f"No material path found for lecture: {lecture_info['id']}")

        # Download from S3
        s3_bucket = os.environ.get('S3_BUCKET', 'study-aid-materials')
        local_file_path = f"/tmp/{os.path.basename(s3_key)}"

        logger.info(f"Downloading file from s3://{s3_bucket}/{s3_key} to {local_file_path}")
        s3_client.download_file(s3_bucket, s3_key, local_file_path)

        # Extract text from PDF and create ParsedText object
        parsed_text = extract_parsed_text_from_pdf(local_file_path)

        # Clean up
        os.remove(local_file_path)

        return parsed_text

    except Exception as e:
        logger.error(f"Error getting lecture parsed text: {e}")
        raise


def extract_parsed_text_from_pdf(file_path) -> ParsedText:
    """Extract text content from PDF file and return ParsedText schema"""
    try:
        import fitz  # PyMuPDF
        from parsed_text_models import ParsedPage

        pages = []
        with fitz.open(file_path) as doc:
            for page_num, page in enumerate(doc, start=1):
                page_text = page.get_text()
                pages.append(ParsedPage(page_number=page_num, text=page_text))

        parsed_text = ParsedText(total_pages=len(pages), pages=pages)
        return parsed_text
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

def send_quiz_email(receiver_email, user_name, lecture_title, lecture_id, quiz_title):
    # TODO(jin): write default sender email and frontend domain
    sender_email = os.environ.get('SES_SENDER_EMAIL')
    frontend_domain = os.environ.get('FRONTEND_DOMAIN')

    # TODO(jin): write correct url
    quiz_url = f"{frontend_domain}/quizzes/lecture/{lecture_id}"

    subject = f"[Study AID] 🕊️퀴즈 생성 완료: '{lecture_title}'의 '{quiz_title}'"
    body_text = f"{user_name}님, 강의 '{lecture_title}' 퀴즈 '{quiz_title}'의 생성이 완료되었어요! {quiz_url} 에서 확인하세요."

    body_html = f"""
    <html>
      <body style="font-family: Arial, sans-serif; line-height: 1.6; color: #333; text-align: center;">
        <p>안녕하세요, {user_name}님. 기다려주셔서 감사합니다.</p>
    
        <p>
          요청하신 강의 '{lecture_title}'의 퀴즈 '<strong>{quiz_title}</strong>' 생성이
          완료되었습니다.<br/>아래 버튼을 눌러 퀴즈를 풀어보세요!
        </p>
    
        <p>
          <a href="{quiz_url}" style="
              display: inline-block;
              padding: 12px 20px;
              background-color: #007BFF;
              color: white;
              text-decoration: none;
              border-radius: 5px;
              font-weight: bold;
            ">
            퀴즈 풀이 바로가기
          </a>
        </p>
    
        <p>감사합니다.<br/>Study AID 팀 드림</p>
      </body>
    </html>
    """

    ses_client.send_email(
        Source=sender_email,
        Destination={'ToAddresses': [receiver_email]},
        Message={
            'Subject': {'Data': subject, 'Charset': 'UTF-8'},
            'Body': {
                'Text': {'Data': body_text, 'Charset': 'UTF-8'},
                'Html': {'Data': body_html, 'Charset': 'UTF-8'}
            }
        }
    )

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
            user_email = message.get("user_email")
            user_name = message.get("user_name")
            course_id = message.get('course_id')
            lecture_id = message.get('lecture_id')
            lecture_title = message.get("lecture_title")
            quiz_title = message.get('title')
            quiz_id = message.get('quiz_id')  # Get the quiz_id for update
            language = message.get('language', '한국어')  # Default to Korean if not specified

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

            # Get chunk size from message or use default
            chunk_size = message.get('chunk_size', DEFAULT_CHUNK_SIZE)
            logger.info(
                f"Using chunk size: {chunk_size} for lecture_id: {lecture_id}")

            # Get lecture content as ParsedText object
            parsed_text = get_lecture_parsed_text(lecture_id)
            logger.info(f"Retrieved parsed text with {parsed_text.total_pages} pages")

            # Initialize components
            pdf_chunker = PDFChunker(default_chunk_size=chunk_size)
            openai_client = OpenAIClient()
            prompt_path = get_prompt_path()

            # Split PDF into chunks
            chunks = pdf_chunker.split_parsed_text(parsed_text, chunk_size)
            chunk_details = ", ".join([f"{c['start_page']}-{c['end_page']}" for c in chunks])
            logger.info(f"Split PDF into {len(chunks)} chunks: [{chunk_details}]")

            # Process chunks in parallel and get merged quiz
            quiz_response = openai_client.process_chunks_in_parallel(chunks, question_counts, prompt_path, language)

            # Convert to dict for database storage
            quiz_data = quiz_response.model_dump()

            # Update quiz in database
            update_quiz_in_db(quiz_id, lecture_id, user_id, quiz_data, quiz_title)

            # Send email to user with quiz link
            if user_email and user_name and lecture_title and quiz_title:
                send_quiz_email(user_email, user_name, lecture_title, lecture_id, quiz_title)
            else:
                logger.warning("이메일 전송을 위한 정보가 부족합니다. user_email, user_name, lecture_title, quiz_title 확인 필요.")

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
                "chunk_count": len(chunks),
                "chunk_sizes": [chunk["end_page"] - chunk["start_page"] + 1 for chunk in chunks],
                "total_questions": len(quiz_data.get('quiz_questions', []))
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
