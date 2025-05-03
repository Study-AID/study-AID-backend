#!/usr/bin/env python3
import os
import sys
import time
from pathlib import Path

import psycopg2


def get_db_connection():
    """Get a connection to the test database"""
    max_attempts = 10
    attempt = 0

    # Inside container, use the service name as hostname
    db_host = os.environ.get('DB_HOST', 'postgres-test')
    db_port = int(os.environ.get('DB_PORT', 5432))  # Inside container, use internal port

    while attempt < max_attempts:
        try:
            conn = psycopg2.connect(
                host=db_host,
                user=os.environ.get('DB_USER', 'postgres'),
                password=os.environ.get('DB_PASSWORD', 'postgres'),
                dbname=os.environ.get('DB_NAME', 'studyaid'),
                port=db_port
            )
            print(f"Connected to database {db_host}:{db_port}")
            return conn
        except psycopg2.OperationalError as e:
            attempt += 1
            print(f"Waiting for PostgreSQL to be ready... (attempt {attempt}/{max_attempts}): {e}")
            time.sleep(2)

    print("Failed to connect to database")
    sys.exit(1)


def create_schema(conn):
    """Create the app schema if it doesn't exist"""
    with conn.cursor() as cursor:
        cursor.execute("CREATE SCHEMA IF NOT EXISTS app;")
        conn.commit()
    print("Created schema 'app' if it didn't exist")


def setup_test_tables(conn):
    """Create test tables in the database"""
    # Define the schema based on the project's migrations
    schema_files = [
        Path("flyway/migrations/V1__init_schema.sql").resolve(),
        Path("flyway/migrations/V2__add_school_column_to_user_table.sql").resolve(),
        Path("flyway/migrations/V3__add_contents_generated_at_and_remove_unused_column.sql").resolve()
    ]

    for schema_file in schema_files:
        if not schema_file.exists():
            print(f"Warning: Schema file not found at {schema_file}")
            continue

        print(f"Applying schema from {schema_file.name}")
        with open(schema_file, 'r') as f:
            schema_sql = f.read()

        with conn.cursor() as cursor:
            try:
                cursor.execute(schema_sql)
                conn.commit()
                print(f"Successfully applied {schema_file.name}")
            except Exception as e:
                conn.rollback()
                # Get details of the error
                error_msg = str(e)
                if "already exists" in error_msg:
                    print(f"⚠️ Schema objects in {schema_file.name} already exist, continuing...")
                else:
                    print(f"Error applying schema {schema_file.name}: {e}")
                # Continue execution even if there are errors, as tables might already exist


def insert_test_data(conn):
    """Insert test data into the tables"""
    test_user_id = "3da59f94-0b80-48cc-a891-7d1ab58dd308"
    test_school_id = "d993e3bc-f563-457e-a8b4-8f8d83a889cd"
    test_semester_id = "d993e3bc-f563-457e-a8b4-8f8d83a889cd"
    test_course_id = "d993e3bc-f563-457e-a8b4-8f8d83a889cd"
    test_lecture_id = "743b2baa-16a5-4982-aa21-010ba83ca283"

    # Use consistent UUIDs for referencing from tests
    os.environ['TEST_USER_ID'] = test_user_id
    os.environ['TEST_COURSE_ID'] = test_course_id
    os.environ['TEST_LECTURE_ID'] = test_lecture_id

    try:
        with conn.cursor() as cursor:
            # Insert test school
            cursor.execute("""
            INSERT INTO app.schools (id, name)
            VALUES (%s, %s)
            ON CONFLICT (id) DO NOTHING
            """, (test_school_id, 'Test University'))

            # Insert test user
            cursor.execute("""
            INSERT INTO app.users (id, name, email, auth_type, school_id)
            VALUES (%s, %s, %s, %s, %s)
            ON CONFLICT (id) DO NOTHING
            """, (test_user_id, 'Test User', 'test@example.com', 'email', test_school_id))

            # Insert test semester
            cursor.execute("""
            INSERT INTO app.semesters (id, user_id, name, year, season)
            VALUES (%s, %s, %s, %s, %s)
            ON CONFLICT (id) DO NOTHING
            """, (test_semester_id, test_user_id, 'Test Semester', 2025, 'spring'))

            # Insert test course
            cursor.execute("""
            INSERT INTO app.courses (id, semester_id, user_id, name)
            VALUES (%s, %s, %s, %s)
            ON CONFLICT (id) DO NOTHING
            """, (test_course_id, test_semester_id, test_user_id, 'Test Course'))

            # Insert test lecture
            cursor.execute("""
            INSERT INTO app.lectures (id, course_id, user_id, title, material_path, material_type, display_order_lex, summary_status)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (id) DO NOTHING
            """, (test_lecture_id, test_course_id, test_user_id, 'Test Lecture', 'sample_lecture.pdf', 'pdf', 'a',
                  'not_started'))

        conn.commit()
        print("Inserted test data into database")
    except Exception as e:
        conn.rollback()
        print(f"Error inserting test data: {e}")


def main():
    # Connect to database
    print("Connecting to database...")
    conn = get_db_connection()

    # Create schema
    create_schema(conn)

    # Set up tables
    setup_test_tables(conn)

    # Insert test data
    insert_test_data(conn)

    # Close connection
    conn.close()

    print("Database setup completed!")


if __name__ == "__main__":
    main()
