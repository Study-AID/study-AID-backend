#!/usr/bin/env python3

import argparse
import importlib.util
import json
import os
import sys
import time
from pathlib import Path

import boto3
import psycopg2
from botocore.config import Config
from botocore.exceptions import ClientError

# Constants
S3_BUCKET_NAME = 'study-aid-materials'
SAMPLE_PDF_PATH = '/app/jobs/test_env/samples/sample_lecture.pdf'
S3_KEY = 'sample_lecture.pdf'
USER_ID = '3da59f94-0b80-48cc-a891-7d1ab58dd308'
COURSE_ID = 'd993e3bc-f563-457e-a8b4-8f8d83a889cd'
LECTURE_ID = '743b2baa-16a5-4982-aa21-010ba83ca283'
QUIZ_ID = 'a82f7c3e-5a9d-4e2a-b48f-32fc4539d6d0'


def parse_arguments():
    parser = argparse.ArgumentParser(description='Test Lambda jobs locally')
    parser.add_argument('--job', required=True, help='Name of the job to test (e.g., summarize_lecture)')
    parser.add_argument('--event', help='Path to event JSON file', default=None)
    parser.add_argument('--endpoint-url', help='AWS endpoint url', default='http://localstack-test:4566')
    parser.add_argument('--skip-upload', action='store_true', help='Skip creating and uploading test file')
    return parser.parse_args()


def check_database_connection():
    """Check if the database is accessible"""
    print("Checking database connection...")
    max_attempts = 5
    attempts = 0

    db_host = os.environ.get('DB_HOST', 'postgres-test')  # Use postgres-test as hostname inside container
    db_port = int(os.environ.get('DB_PORT', 5432))  # Use 5432 inside container (mapped to 5433 outside)

    while attempts < max_attempts:
        try:
            conn = psycopg2.connect(
                host=db_host,
                user=os.environ.get('DB_USER', 'postgres'),
                password=os.environ.get('DB_PASSWORD', 'postgres'),
                dbname=os.environ.get('DB_NAME', 'studyaid'),
                port=db_port
            )
            conn.close()
            print("Database connection successful")
            return True
        except Exception as e:
            attempts += 1
            print(f"Database connection failed (attempt {attempts}/{max_attempts}): {e}")
            if attempts >= max_attempts:
                print("Max database connection attempts reached. Tests may fail.")
                return False
            time.sleep(2)


def check_s3_connection(endpoint_url):
    """Check if S3 is accessible"""
    print(f"Checking S3 connection to {endpoint_url}...")
    max_attempts = 5
    attempts = 0

    while attempts < max_attempts:
        try:
            s3_client = boto3.client(
                's3',
                endpoint_url=endpoint_url,
                aws_access_key_id='test',
                aws_secret_access_key='test',
                region_name='ap-northeast-2',
                config=Config(
                    connect_timeout=10,
                    retries={'max_attempts': 3}
                )
            )

            # Test connection by listing buckets
            buckets = s3_client.list_buckets()
            print(f"S3 connection successful. Available buckets: {[b['Name'] for b in buckets.get('Buckets', [])]}")

            # Ensure test bucket exists
            try:
                s3_client.head_bucket(Bucket=S3_BUCKET_NAME)
                print(f"Test bucket '{S3_BUCKET_NAME}' exists.")
            except ClientError:
                print(f"Creating test bucket '{S3_BUCKET_NAME}'...")
                s3_client.create_bucket(Bucket=S3_BUCKET_NAME)
                print(f"Test bucket '{S3_BUCKET_NAME}' created.")

            return s3_client
        except Exception as e:
            attempts += 1
            print(f"S3 connection failed (attempt {attempts}/{max_attempts}): {e}")
            if attempts >= max_attempts:
                print("Max S3 connection attempts reached. Tests may fail if they depend on S3.")
                return None
            time.sleep(2)


def create_sample_pdf():
    """Create a sample PDF file for testing"""
    try:
        import reportlab.pdfgen.canvas
        from reportlab.lib.pagesizes import letter
        from reportlab.platypus import SimpleDocTemplate, Paragraph, Spacer
        from reportlab.lib.styles import getSampleStyleSheet

        print(f"Creating sample PDF at {SAMPLE_PDF_PATH}...")

        # Check if the samples directory exists
        samples_dir = os.path.dirname(SAMPLE_PDF_PATH)
        if not os.path.exists(samples_dir):
            os.makedirs(samples_dir)

        # Create a simple PDF
        doc = SimpleDocTemplate(SAMPLE_PDF_PATH, pagesize=letter)
        styles = getSampleStyleSheet()

        # Create content
        content = []
        content.append(Paragraph("Sample Lecture Material for Testing", styles['Title']))
        content.append(Spacer(1, 20))

        content.append(Paragraph("Introduction", styles['Heading1']))
        content.append(Paragraph(
            "This is a sample lecture document created for testing purposes. "
            "It contains some basic text content that can be used to test the summarization, "
            "quiz generation, and exam generation features.",
            styles['Normal']
        ))
        content.append(Spacer(1, 10))

        content.append(Paragraph("Main Concepts", styles['Heading1']))
        for i in range(1, 6):
            content.append(Paragraph(f"Concept {i}", styles['Heading2']))
            content.append(Paragraph(
                f"This is an explanation of concept {i}. It includes important details "
                f"about how this concept works and why it's important. Students should "
                f"understand this concept thoroughly before proceeding to the next section.",
                styles['Normal']
            ))
            content.append(Spacer(1, 5))

        content.append(Paragraph("Summary", styles['Heading1']))
        content.append(Paragraph(
            "In summary, we've covered several important concepts in this lecture. "
            "Students should now have a basic understanding of these topics and be "
            "prepared for the upcoming assessments.",
            styles['Normal']
        ))

        # Build the PDF
        doc.build(content)
        print("Sample PDF created successfully")
        return True
    except Exception as e:
        print(f"Error creating sample PDF: {e}")
        return False


def upload_test_file(s3_client, file_path=SAMPLE_PDF_PATH, s3_key=S3_KEY):
    """Upload a test file to S3"""
    if not os.path.exists(file_path):
        print(f"Error: File {file_path} not found. Creating sample PDF...")
        success = create_sample_pdf()
        if not success:
            return False

    try:
        print(f"Uploading {file_path} to s3://{S3_BUCKET_NAME}/{s3_key}...")
        with open(file_path, 'rb') as data:
            s3_client.upload_fileobj(data, S3_BUCKET_NAME, s3_key)
        print(f"Successfully uploaded file to s3://{S3_BUCKET_NAME}/{s3_key}")
        return True
    except Exception as e:
        print(f"Error uploading file: {e}")
        return False


def get_event_data(job_name, event_file=None):
    """Get the event data from file or generate default event"""
    if event_file and os.path.exists(event_file):
        with open(event_file, 'r') as f:
            event_data = json.load(f)
            print(f"Loaded event data from {event_file}")
            return event_data

    # Default events for each job type
    if job_name == 'summarize_lecture':
        return {
            'Records': [{
                'body': json.dumps({
                    'user_id': USER_ID,
                    'course_id': COURSE_ID,
                    'lecture_id': LECTURE_ID,
                    's3_bucket': S3_BUCKET_NAME,
                    's3_key': S3_KEY
                })
            }]
        }
    elif job_name == 'generate_exam':
        return {
            'Records': [{
                'body': json.dumps({
                    'user_id': USER_ID,
                    'course_id': COURSE_ID,
                    'referenced_lecture_ids': [LECTURE_ID],
                    'true_or_false_count': 3,
                    'multiple_choice_count': 3,
                    'short_answer_count': 3,
                    'essay_count': 3,
                    'title': "Test Exam",
                })
            }]
        }
    elif job_name == 'generate_quiz':
        return {
            'Records': [{
                'body': json.dumps({
                    'user_id': USER_ID,
                    'course_id': COURSE_ID,
                    'lecture_id': LECTURE_ID,
                    'quiz_id': QUIZ_ID,
                    'true_or_false_count': 3,
                    'multiple_choice_count': 3,
                    'short_answer_count': 3,
                    'essay_count': 3,
                    'title': 'Test Quiz'
                })
            }]
        }
    else:
        # Generic event
        return {
            'Records': [{
                'body': json.dumps({
                    'test': True,
                    'job': job_name
                })
            }]
        }


def run_job(job_name, event):
    """Import and run the specified job handler"""
    # Get handler path
    handler_path = Path(f"/app/jobs/{job_name}/handler.py").resolve()
    if not handler_path.exists():
        print(f"Error: Handler file not found at {handler_path}")
        sys.exit(1)

    # Add the job directory to sys.path to allow importing local modules
    job_dir = os.path.dirname(handler_path)
    if job_dir not in sys.path:
        sys.path.insert(0, job_dir)

    # Import the handler module with monkey patch
    try:
        # Now import the handler module
        spec = importlib.util.spec_from_file_location("handler", handler_path)
        handler_module = importlib.util.module_from_spec(spec)
        spec.loader.exec_module(handler_module)

        # Get the lambda_handler function
        lambda_handler = getattr(handler_module, 'lambda_handler', None)
        if not lambda_handler:
            print(f"Error: No lambda_handler function found in {handler_path}")
            sys.exit(1)

        # Run the handler with the event
        print(f"Running {job_name} handler with event:")
        print(json.dumps(event, indent=2))

        result = lambda_handler(event, {})
        print("\nHandler result:")
        print(json.dumps(result, indent=2))

        return result
    except Exception as e:
        print(f"Error running job handler: {e}")
        import traceback
        print(traceback.format_exc())
        sys.exit(1)


def main():
    args = parse_arguments()

    # Check connections to required services
    check_database_connection()
    s3_client = check_s3_connection(args.endpoint_url)

    try:
        # Create and upload test file if needed
        if not args.skip_upload and s3_client:
            if not upload_test_file(s3_client):
                print("Failed to upload test file. Tests may fail.")

        # Prepare event data
        event = get_event_data(args.job, args.event)

        # Run the job
        run_job(args.job, event)

        print("Test completed successfully!")
    except Exception as e:
        print(f"Error: {e}")
        import traceback
        print(traceback.format_exc())
        sys.exit(1)


if __name__ == "__main__":
    main()
