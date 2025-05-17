import boto3
import json
import logging
import os
import psycopg2
import psycopg2.extras
import traceback
import uuid
from botocore.config import Config
from botocore.exceptions import ClientError
from datetime import datetime

from openai_client import OpenAIClient

# Configure logging
logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

# Constants and configurations
# AWS S3 configuration
S3_ENDPOINT_URL = os.environ.get('AWS_ENDPOINT_URL')
AWS_ACCESS_KEY_ID = os.environ.get('AWS_ACCESS_KEY_ID', None)
AWS_SECRET_ACCESS_KEY = os.environ.get('AWS_SECRET_ACCESS_KEY', None)
AWS_REGION = os.environ.get('AWS_REGION', 'ap-northeast-2')

# Database configuration
DB_CONFIG = {
    'host': os.environ.get('DB_HOST'),
    'user': os.environ.get('DB_USER'),
    'password': os.environ.get('DB_PASSWORD'),
    'database': os.environ.get('DB_NAME'),
    'port': int(os.environ.get('DB_PORT'))
}

# Initialize clients
s3_client = boto3.client(
    's3',
    endpoint_url=S3_ENDPOINT_URL,
    aws_access_key_id=AWS_ACCESS_KEY_ID,
    aws_secret_access_key=AWS_SECRET_ACCESS_KEY,
    region_name=AWS_REGION,
    config=Config(
        connect_timeout=10,
        retries={'max_attempts': 3}
    )
)


def get_db_connection():
    """Create and return a database connection"""
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
        os.path.dirname(__file__), '..', '..', 'prompts', 'summarize_lecture',
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


def download_file_from_s3(bucket, key, local_path):
    """Download a file from S3 to a local path"""
    try:
        logger.info(f"Downloading file from s3://{bucket}/{key} to {local_path}")
        if S3_ENDPOINT_URL:
            logger.info(f"Using S3 endpoint URL: {S3_ENDPOINT_URL}")

        s3_client.download_file(bucket, key, local_path)
        logger.info(f"Downloaded file from s3://{bucket}/{key} to {local_path}")
        return local_path
    except ClientError as e:
        logger.error(f"Error downloading file from S3: {e}")
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


def update_lecture_status(lecture_id, status):
    """Update the lecture summary status in the database"""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor() as cursor:
            query = """
                    UPDATE app.lectures
                    SET summary_status = %s,
                        updated_at     = NOW()
                    WHERE id = %s \
                    """
            cursor.execute(query, (status, lecture_id))

        conn.commit()
        logger.info(f"Updated lecture summary status to {status} for lecture_id: {lecture_id}")
    except Exception as e:
        logger.error(f"Error updating lecture status: {e}")
        raise
    finally:
        if conn:
            conn.close()


def update_lecture_summary(lecture_id, summary):
    """Update the lecture's summary in the database"""
    conn = None
    try:
        conn = get_db_connection()
        with conn.cursor() as cursor:
            # Convert Summary object to JSON
            summary_json = json.dumps(summary.model_dump())

            # Update the lecture record
            query = """
                    UPDATE app.lectures
                    SET summary        = %s,
                        summary_status = 'completed',
                        updated_at     = NOW()
                    WHERE id = %s \
                    """
            cursor.execute(query, (summary_json, lecture_id))

        conn.commit()
        logger.info(f"Summary updated for lecture_id: {lecture_id}")
    except Exception as e:
        logger.error(f"Error updating lecture summary: {e}")
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
                    VALUES (%s, %s, %s, %s, %s, %s) \
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
            s3_bucket = message.get('s3_bucket')
            s3_key = message.get('s3_key')

            if not all([lecture_id, s3_bucket, s3_key]):
                logger.error("Missing required parameters in message")
                continue

            # Update status to in_progress
            update_lecture_status(lecture_id, 'in_progress')

            # Download file from S3
            local_file_path = f"/tmp/{os.path.basename(s3_key)}"
            download_file_from_s3(s3_bucket, s3_key, local_file_path)

            # Extract text from PDF
            lecture_content = extract_text_from_pdf(local_file_path)

            # Generate summary using OpenAI
            openai_client = OpenAIClient()
            prompt_path = get_prompt_path()
            summary = openai_client.generate_summary(lecture_content, prompt_path)

            # Update lecture with summary
            update_lecture_summary(lecture_id, summary)

            # Log the activity
            activity_details = {
                "action": "generate_summary",
                "lecture_id": lecture_id,
            }
            log_activity(course_id, user_id, 'update', 'lecture', activity_details)

            # Clean up local file
            os.remove(local_file_path)

            logger.info(f"Successfully processed the lecture of {lecture_id}")

        return {
            'statusCode': 200,
            'body': json.dumps('Lecture summarization completed successfully')
        }

    except Exception as e:
        logger.error(f"Error in lambda_handler: {e}")
        logger.error(traceback.format_exc())

        # Update lecture status to 'failed' if lecture_id is available
        if 'lecture_id' in locals():
            try:
                update_lecture_status(lecture_id, 'failed')
            except Exception as db_error:
                logger.error(f"Error updating lecture status: {db_error}")

        return {
            'statusCode': 500,
            'body': json.dumps(f'Error processing lecture: {str(e)}')
        }
