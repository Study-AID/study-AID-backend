#!/usr/bin/env python3
import json
import os
import psycopg2
import sys
import time
from pathlib import Path


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

    # Quiz related IDs
    test_quiz_item1_id = "b1234567-8901-2345-6789-012345678901"
    test_quiz_item2_id = "c2345678-9012-3456-7890-123456789012"
    test_quiz_response1_id = "d3456789-0123-4567-8901-234567890123"
    test_quiz_response2_id = "e4567890-1234-5678-9012-345678901234"
    
    # Exam related IDs
    test_exam_id = "b93e4f3a-6c8d-4f1a-a59f-43d65639e7e1"
    test_exam_item1_id = "f5678901-2345-6789-0123-456789012345"
    test_exam_item2_id = "b6789012-3456-4789-9234-567890123456"
    test_exam_response1_id = "c7890123-4567-4789-8234-678901234567"
    test_exam_response2_id = "d8901234-5678-4789-9234-789012345678"

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
    os.environ['TEST_EXAM_ID'] = test_exam_id

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
                           INSERT INTO app.lectures (id, course_id, user_id, title, material_path, material_type,
                                                     display_order_lex, summary_status, parsed_text)
                           VALUES (%s, %s, %s, %s, %s, %s, %s, %s, %s)
                           ON CONFLICT (id) DO UPDATE SET parsed_text = EXCLUDED.parsed_text
                           """,
                           (test_lecture_id, test_course_id, test_user_id, 'Test Lecture', 'sample_lecture.pdf', 'pdf',
                            'a',
                            'not_started', sample_parsed_text))

            # Insert test quiz
            cursor.execute("""
                           INSERT INTO app.quizzes (id, lecture_id, user_id, title, status, created_at, updated_at)
                           VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
                           ON CONFLICT (id) DO NOTHING
                           """, (test_quiz_id, test_lecture_id, test_user_id, 'Test Quiz', 'not_started'))

            # Insert test quiz results
            # 초기 데이터 생성
            cursor.execute("""
                INSERT INTO app.quiz_results (id, quiz_id, user_id, score, max_score, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
                ON CONFLICT (id) DO NOTHING
                """, (
                test_user_id, test_quiz_id, test_user_id, 0.0, 25.0
            ))

            # Insert test quiz items
            # 서술형 문제 1
            cursor.execute("""
                INSERT INTO app.quiz_items (id, quiz_id, user_id, question, question_type, 
                                           explanation, text_answer, points)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (id) DO NOTHING
                """, (
                test_quiz_item1_id, test_quiz_id, test_user_id,
                "정규화의 개념과 장단점에 대해 설명하시오.",
                "essay",
                "이 문제는 학생이 데이터베이스 설계의 핵심 개념인 정규화에 대한 이해도를 평가하기 위한 문제입니다. 정규화의 정의, 목적, 장점과 단점을 균형있게 설명할 수 있는지 확인합니다.",
                "정규화는 관계형 데이터베이스에서 중복 데이터를 최소화하여 데이터 일관성을 보장하는 설계 기법입니다. 장점으로는 데이터 중복 제거, 저장 공간 절약, 데이터 일관성 보장이 있습니다. 단점으로는 조인 연산 증가로 인한 성능 저하가 있을 수 있습니다.",
                10.0
            ))

            # 서술형 문제 2
            cursor.execute("""
                INSERT INTO app.quiz_items (id, quiz_id, user_id, question, question_type, 
                                           explanation, text_answer, points)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (id) DO NOTHING
                """, (
                test_quiz_item2_id, test_quiz_id, test_user_id,
                "객체지향 프로그래밍의 4가지 특징을 설명하시오.",
                "essay",
                "이 문제는 객체지향 프로그래밍의 핵심 개념들에 대한 학생의 이해도를 종합적으로 평가합니다. 4가지 특징(캡슐화, 상속, 다형성, 추상화)을 모두 정확히 설명하고 각각의 개념과 역할을 명확히 구분할 수 있는지 확인합니다.",
                "객체지향 프로그래밍의 4가지 특징은 다음과 같습니다: 1) 캡슐화: 데이터와 메서드를 하나로 묶어 정보 은닉 2) 상속: 기존 클래스의 특성을 새로운 클래스가 물려받음 3) 다형성: 같은 인터페이스로 다른 구현을 제공 4) 추상화: 복잡한 시스템을 단순화하여 표현",
                10.0
            ))

            # Insert test quiz responses
            # 사용자 답변 1
            cursor.execute("""
                INSERT INTO app.quiz_responses (id, quiz_id, question_id, user_id, text_answer, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
                ON CONFLICT (id) DO NOTHING
                """, (
                test_quiz_response1_id, test_quiz_id, test_quiz_item1_id, test_user_id,
                "정규화는 데이터베이스에서 중복을 없애는 것입니다. 장점은 저장공간을 절약할 수 있다는 것입니다."
            ))

            # 사용자 답변 2
            cursor.execute("""
                INSERT INTO app.quiz_responses (id, quiz_id, question_id, user_id, text_answer, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
                ON CONFLICT (id) DO NOTHING
                """, (
                test_quiz_response2_id, test_quiz_id, test_quiz_item2_id, test_user_id,
                "캡슐화, 상속, 다형성이 있습니다. 캡슐화는 데이터를 숨기는 것이고, 상속은 부모 클래스의 기능을 받는 것이고, 다형성은 여러 형태를 가질 수 있다는 것입니다."
            ))

            # ========================
            # EXAM 관련 테스트 데이터
            # ========================
            
            # Insert test exam
            cursor.execute("""
                INSERT INTO app.exams (id, course_id, user_id, title, status, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
                ON CONFLICT (id) DO NOTHING
                """, (test_exam_id, test_course_id, test_user_id, 'Test Exam', 'not_started'))

            # Insert test exam results
            cursor.execute("""
                INSERT INTO app.exam_results (id, exam_id, user_id, score, max_score, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
                ON CONFLICT (id) DO NOTHING
                """, (
                test_user_id, test_exam_id, test_user_id, 0.0, 30.0
            ))

            # Insert test exam items
            # 시험 서술형 문제 1 (더 복잡하고 포괄적)
            cursor.execute("""
                INSERT INTO app.exam_items (id, exam_id, user_id, question, question_type, 
                                           explanation, text_answer, points)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (id) DO NOTHING
                """, (
                test_exam_item1_id, test_exam_id, test_user_id,
                "데이터베이스 정규화 과정에서 1NF, 2NF, 3NF의 개념과 특징을 설명하고, 각 단계별 정규화의 목적과 장단점을 비교 분석하시오. 실제 사례를 들어 정규화 전후의 테이블 구조 변화를 보여주시오.",
                "essay",
                "이 문제는 데이터베이스 설계의 핵심인 정규화에 대한 심화 이해를 평가합니다. 단순한 정의를 넘어서 각 정규형의 특징, 목적, 장단점을 비교분석하고 실제 적용 사례를 제시할 수 있는지 종합적으로 평가합니다.",
                "1NF(제1정규형)는 각 속성이 원자값을 가져야 하며 반복 그룹이 없어야 합니다. 2NF(제2정규형)는 1NF를 만족하면서 부분적 함수 종속이 제거되어야 하며, 모든 비키 속성이 기본키에 완전 함수 종속되어야 합니다. 3NF(제3정규형)는 2NF를 만족하면서 이행적 함수 종속이 제거되어야 합니다. 정규화의 장점은 데이터 중복 최소화, 일관성 유지, 저장공간 절약이며, 단점은 조인 연산 증가로 인한 성능 저하입니다. 예를 들어, 학생-과목-성적 테이블에서 학생정보와 과목정보가 중복되는 경우, 정규화를 통해 학생 테이블, 과목 테이블, 수강 테이블로 분리할 수 있습니다.",
                15.0
            ))

            # 시험 서술형 문제 2 (더 복잡하고 포괄적)
            cursor.execute("""
                INSERT INTO app.exam_items (id, exam_id, user_id, question, question_type, 
                                           explanation, text_answer, points)
                VALUES (%s, %s, %s, %s, %s, %s, %s, %s)
                ON CONFLICT (id) DO NOTHING
                """, (
                test_exam_item2_id, test_exam_id, test_user_id,
                "객체지향 프로그래밍의 SOLID 원칙 5가지를 설명하고, 각 원칙이 소프트웨어 설계에 미치는 영향과 위반 시 발생할 수 있는 문제점을 구체적인 예시와 함께 논하시오. 또한 이러한 원칙들이 현대 소프트웨어 개발에서 어떻게 적용되는지 설명하시오.",
                "essay",
                "이 문제는 객체지향 설계의 핵심 원칙인 SOLID에 대한 깊이 있는 이해를 평가합니다. 각 원칙의 정의를 넘어서 실제 소프트웨어 설계에 미치는 영향, 위반 시 문제점, 현대 개발에서의 적용 사례까지 종합적으로 분석할 수 있는지 확인합니다.",
                "SOLID 원칙은 다음과 같습니다: 1) SRP(단일책임원칙): 클래스는 하나의 책임만 가져야 하며, 변경 이유도 하나여야 합니다. 2) OCP(개방폐쇄원칙): 확장에는 열려있고 수정에는 닫혀있어야 합니다. 3) LSP(리스코프치환원칙): 상위 타입의 객체를 하위 타입의 객체로 치환 가능해야 합니다. 4) ISP(인터페이스분리원칙): 클라이언트는 사용하지 않는 인터페이스에 의존하면 안 됩니다. 5) DIP(의존성역전원칙): 고수준 모듈은 저수준 모듈에 의존하면 안 되며, 추상화에 의존해야 합니다. 이러한 원칙들은 코드의 유지보수성, 확장성, 재사용성을 높이며, 현대의 프레임워크와 디자인 패턴에서 광범위하게 적용됩니다.",
                15.0
            ))

            # Insert test exam responses
            # 시험 사용자 답변 1 (퀴즈보다 더 상세하지만 불완전)
            cursor.execute("""
                INSERT INTO app.exam_responses (id, exam_id, question_id, user_id, text_answer, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
                ON CONFLICT (id) DO NOTHING
                """, (
                test_exam_response1_id, test_exam_id, test_exam_item1_id, test_user_id,
                "1NF는 원자값을 가져야 하고 반복그룹이 없어야 합니다. 2NF는 1NF를 만족하면서 부분 함수 종속이 제거되어야 합니다. 3NF는 2NF를 만족하면서 이행적 함수 종속이 제거되어야 합니다. 정규화의 장점은 중복 제거와 일관성 유지입니다. 단점은 조인이 많아져서 성능이 저하될 수 있습니다. 학생 테이블에서 학번, 이름, 과목명, 성적이 모두 들어있다면 과목별로 학생 정보가 중복됩니다. 이를 학생 테이블과 수강 테이블로 나누면 중복이 제거됩니다."
            ))

            # 시험 사용자 답변 2 (일부 원칙만 정확히 설명)
            cursor.execute("""
                INSERT INTO app.exam_responses (id, exam_id, question_id, user_id, text_answer, created_at, updated_at)
                VALUES (%s, %s, %s, %s, %s, NOW(), NOW())
                ON CONFLICT (id) DO NOTHING
                """, (
                test_exam_response2_id, test_exam_id, test_exam_item2_id, test_user_id,
                "SOLID 원칙 중에서 SRP는 단일 책임 원칙으로 하나의 클래스는 하나의 책임만 가져야 한다는 것입니다. OCP는 개방 폐쇄 원칙으로 확장에는 열려있고 수정에는 닫혀있어야 합니다. LSP는 상위 클래스를 하위 클래스로 바꿔도 동작해야 한다는 것입니다. 나머지 두 원칙은 잘 기억나지 않습니다. 이런 원칙들을 지키면 코드가 더 좋아진다고 배웠습니다."
            ))

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
