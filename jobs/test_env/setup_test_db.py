#!/usr/bin/env python3
import os
import sys
import time
import json
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
    # Get all migration files from the flyway/migrations directory
    migrations_dir = Path("flyway/migrations").resolve()

    if not migrations_dir.exists():
        print(f"Warning: Migrations directory not found at {migrations_dir}")
        return

    # Find all SQL migration files and sort them by version number
    migration_files = sorted([
        f for f in migrations_dir.glob("V*.sql")
        if f.is_file()
    ], key=lambda x: int(x.stem.split('__')[0][1:]))  # Extract version number from V{number}__

    if not migration_files:
        print("No migration files found")
        return

    print(f"Found {len(migration_files)} migration files")

    for migration_file in migration_files:
        print(f"Applying migration from {migration_file.name}")
        with open(migration_file, 'r') as f:
            migration_sql = f.read()

        with conn.cursor() as cursor:
            try:
                cursor.execute(migration_sql)
                conn.commit()
                print(f"Successfully applied {migration_file.name}")
            except Exception as e:
                conn.rollback()
                # Get details of the error
                error_msg = str(e)
                if "already exists" in error_msg:
                    print(f"⚠Schema objects in {migration_file.name} already exist, continuing...")
                else:
                    print(f"Error applying migration {migration_file.name}: {e}")
                # Continue execution even if there are errors, as tables might already exist


def insert_test_data(conn):
    """Insert test data into the tables"""
    test_user_id = "3da59f94-0b80-48cc-a891-7d1ab58dd308"
    test_school_id = "d993e3bc-f563-457e-a8b4-8f8d83a889cd"
    test_semester_id = "d993e3bc-f563-457e-a8b4-8f8d83a889cd"
    test_course_id = "d993e3bc-f563-457e-a8b4-8f8d83a889cd"
    test_lecture_id = "743b2baa-16a5-4982-aa21-010ba83ca283"
    test_quiz_id = "a82f7c3e-5a9d-4e2a-b48f-32fc4539d6d0"

    # Sample parsed_text data with 5 pages
    sample_parsed_text = json.dumps({
        "total_pages": 5,
        "pages": [
            {
                "page_number": 1,
                "text": "# Introduction to Computer Science\n\nComputer science is the study of computation, automation, and information. Computer science spans theoretical disciplines to practical disciplines. Unlike physical systems, abstract computation has no natural laws that provide fundamental limitations, and this provides for a field of study that is both deep and broad.\n\nComputer science is generally considered an academic discipline and distinct from computer programming and software engineering, although the three are often considered interchangeable in colloquial speech. The field of computer science has its roots in mathematics, electrical engineering, physics, and linguistics. As an academic discipline, it began in the 1950s and 1960s, with the advent of digital electronic computers."
            },
            {
                "page_number": 2,
                "text": "## Major Areas of Computer Science\n\n### 1. Theoretical Computer Science\n\nTheoretical computer science includes several core areas:\n\n- **Automata Theory**: The study of abstract machines and their computational problems.\n- **Computability Theory**: What problems can be solved algorithmically.\n- **Computational Complexity Theory**: How efficiently a problem can be solved.\n- **Cryptography**: Secure communications and the mathematics of encryption.\n- **Formal Methods**: Using mathematical techniques to prove correctness of computer systems.\n\nTheoretical computer science provides the foundation upon which all other areas of computer science are built. The famous P vs NP problem, one of the Millennium Prize Problems, belongs to this area."
            },
            {
                "page_number": 3,
                "text": "### 2. Computer Systems and Architecture\n\nComputer systems and architecture encompasses the design and implementation of computer hardware and systems software:\n\n- **Computer Architecture**: How computers are designed and organized, including CPU design, memory hierarchies, and input/output systems.\n- **Operating Systems**: Software that manages computer hardware and provides services for computer programs.\n- **Computer Networks**: The study of communication between computers and the protocols that enable it.\n- **Distributed Systems**: Systems of multiple computers working together to achieve a common goal.\n- **High-Performance Computing**: Design and analysis of supercomputers and parallel processing systems.\n\nMoore's Law, which predicted that the number of transistors on a microchip would double approximately every two years, has been a driving force in this field for decades, though its pace has slowed in recent years."
            },
            {
                "page_number": 4,
                "text": "### 3. Artificial Intelligence and Machine Learning\n\nArtificial intelligence (AI) is the field concerned with creating systems that can mimic human cognitive functions:\n\n- **Machine Learning**: Systems that can learn from data without being explicitly programmed.\n- **Deep Learning**: Neural network models with many layers that can learn complex patterns.\n- **Natural Language Processing**: Enabling computers to understand and generate human language.\n- **Computer Vision**: Systems that can interpret and understand visual information.\n- **Robotics**: Creating autonomous machines that can sense and interact with their environment.\n\nThe recent surge in AI capabilities has been fueled by advances in deep learning and the availability of large datasets and computational resources. Models like transformers have revolutionized fields like natural language processing and computer vision."
            },
            {
                "page_number": 5,
                "text": "### 4. Software Engineering and Programming Languages\n\nSoftware engineering focuses on the systematic development of software systems:\n\n- **Programming Languages**: The design, implementation, and theory of languages used to write computer programs.\n- **Compilers and Interpreters**: Tools that translate programming languages into machine code.\n- **Software Development Methodologies**: Approaches to organizing the process of software creation, like Agile, Waterfall, and DevOps.\n- **Formal Verification**: Mathematical techniques to prove program correctness.\n- **Human-Computer Interaction**: Designing systems with usability in mind.\n\nThe field of software engineering has evolved significantly since the term was coined in the 1960s. Modern techniques emphasize collaboration, continuous integration, and automated testing. Popular programming paradigms include object-oriented, functional, and reactive programming.\n\n## Conclusion\n\nComputer science continues to evolve rapidly, with new areas emerging as technology advances. The interdisciplinary nature of the field means that computer scientists often collaborate with experts in other domains to solve complex problems. As computing becomes increasingly ubiquitous, understanding the fundamentals of computer science becomes increasingly valuable across all fields of human endeavor."
            }
        ]
    })

    # Use consistent UUIDs for referencing from tests
    os.environ['TEST_USER_ID'] = test_user_id
    os.environ['TEST_COURSE_ID'] = test_course_id
    os.environ['TEST_LECTURE_ID'] = test_lecture_id
    os.environ['TEST_QUIZ_ID'] = test_quiz_id

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

            # Insert test lecture with parsed_text field
            cursor.execute("""
            INSERT INTO app.lectures (id, course_id, user_id, title, material_path, material_type, display_order_lex, summary_status, parsed_text)
            VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
            ON CONFLICT (id) DO UPDATE SET
                parsed_text = EXCLUDED.parsed_text
            """, (test_lecture_id, test_course_id, test_user_id, 'Test Lecture', 'sample_lecture.pdf', 'pdf', 'a',
                  'not_started', sample_parsed_text))
                  
            # Insert test quiz
            cursor.execute("""
            INSERT INTO app.quizzes (id, lecture_id, user_id, title, status, created_at, updated_at)
            VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
            ON CONFLICT (id) DO NOTHING
            """, (test_quiz_id, test_lecture_id, test_user_id, 'Test Quiz', 'not_started'))

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
