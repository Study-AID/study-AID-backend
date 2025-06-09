.PHONY: help build run logs logs-api down clean ps redis-cli pgsql-cli test test_win test-coverage test-coverage_win open-test-report open-coverage-report migrate migration-info test-env-start test-env-setup test-job-summarize test-job-quiz test-job-exam test-job-shell test-env-stop test-env-clean

# Default target
help:
	@echo "Available commands:"
	@echo "  make build                     - Build Docker images"
	@echo "  make run                       - Start all services"
	@echo "  make logs                      - Follow logs from all containers"
	@echo "  make logs-api                  - Follow logs from API service"
	@echo "  make down                      - Stop all services"
	@echo "  make clean                     - Remove all containers, volumes, and images"
	@echo "  make ps                        - List running containers"
	@echo "  make pgsql-cli                 - Connect to PostgreSQL CLI"
	@echo "  make redis-cli                 - Connect to Redis CLI"
	@echo "  make test                      - Run tests"
	@echo "  make test-win                  - Run tests (Windows)"
	@echo "  make test-coverage             - Run tests with coverage report"
	@echo "  make test-coverage-win         - Run tests with coverage report (Windows)"
	@echo "  make open-test-report          - Open test report in browser"
	@echo "  make open-coverage-report      - Open coverage report in browser"
	@echo "  make migrate                   - Run Flyway migration"
	@echo "  make migration-info            - Check migrated schema versions and status"
	@echo "  make migration-repair          - Repair migration checksum mismatches"
	@echo ""
	@echo "Lambda Job Test Commands:"
	@echo "  make test-env-start            - Start test environment (PostgreSQL, LocalStack)"
	@echo "  make test-env-setup            - Setup test database with sample data"
	@echo "  make test-job-summarize        - Test summarize_lecture job"
	@echo "  make test-job-quiz             - Test generate_quiz job"
	@echo "  make test-job-exam             - Test generate_exam job"
	@echo "  make test-job-grade-quiz-essay - Test grade_quiz_essay job"
	@echo "  make test-job-shell            - Start a shell in test container for debugging"
	@echo "  make test-env-stop             - Stop test environment containers"
	@echo "  make test-env-clean            - Remove test environment including volumes"
	@echo ""
	@echo "QnA Chat Langchain Commands:"
	@echo "  make langchain-build           - Build LangChain Docker image"
	@echo "  make langchain-up              - Run LangChain QnA Chat Context Generating Server"
	@echo "  make langchain-rebuild         - Rebuild LangChain Docker image"
	@echo "  make langchain-restart         - Restart LangChain server to apply code changes"
	@echo "  make langchain-stop            - Stop LangChain QnA Chat Context Generating Server"
	@echo "  make langchain-down            - Stop and remove LangChain QnA Chat Context Generating Server"

# Build Docker images
build:
	docker compose build

# Start all services
run:
	docker compose up -d
	@echo "Waiting for services to be ready..."
	@sleep 5
	@echo "Initializing LocalStack..."
	@bash scripts/init-localstack.sh
	@echo "API Server is running at http://localhost:8080/api"
	@echo "Swagger UI is available at http://localhost:8080/api/swagger-ui.html"
	@echo "MinIO Console is available at http://localhost:9001"
	@echo "  - Username: minioadmin"
	@echo "  - Password: minioadmin"

# Follow logs
logs:
	docker compose logs -f

# Follow API logs
logs-api:
	docker compose logs -f api

# Stop all services
down:
	docker compose down

# Remove all containers, volumes, and images
clean:
	docker compose down -v --rmi all

# List running containers
ps:
	docker compose ps

# Connect to PostgreSQL CLI
pgsql-cli:
	docker exec -it postgres psql -U postgres -d studyaid

# Connect to Redis CLI
redis-cli:
	docker exec -it redis redis-cli

# Run tests
test:
	./gradlew test --info

test_win:
	gradlew.bat test --info

# Run tests with coverage report
test-coverage:
	./gradlew test jacocoTestReport --info

# Run tests with coverage report
test-coverage_win:
	gradlew.bat test jacocoTestReport --info

# Open coverage report in browser
open-coverage-report:
	open build/reports/jacoco/test/html/index.html

# Open test report in browser
open-test-report:
	open build/reports/tests/test/index.html

# Run Flyway migration
migrate:
	@echo "Running DB migration with Flyway..."
	docker compose --profile migration run --rm flyway -configFiles=/flyway/conf/flyway.conf migrate
	@echo "Migration completed (Flyway container auto-removed)"

# Check migrated schema versions and status
migration-info:
	docker compose --profile migration run --rm flyway -configFiles=/flyway/conf/flyway.conf info

# Repair migration checksum mismatches
migration-repair:
	@echo "Running DB migration repair with Flyway..."
	docker compose --profile migration run --rm flyway -configFiles=/flyway/conf/flyway.conf repair
	@echo "Migration repair completed (Flyway container auto-removed)"

# -------------------------
# Lambda 테스트 관련 명령어
# -------------------------

# 테스트 환경 시작 (PostgreSQL, LocalStack)
test-env-start:
	@echo "Starting test environment (PostgreSQL, LocalStack)..."
	cd jobs/test_env && docker-compose -f docker-compose.test.yml up -d postgres-test localstack-test setup-s3
	@echo "Test environment started successfully!"
	@echo "Database: PostgreSQL on localhost:5433"
	@echo "S3: LocalStack on localhost:4567"

# 테스트 환경 초기화 및 데이터 세팅
test-env-setup:
	@echo "Setting up test environment..."
	cd jobs/test_env && docker-compose -f docker-compose.test.yml up setup-db
	@echo "Test environment setup completed!"

# 요약 기능 테스트 (summarize_lecture)
test-job-summarize:
	@echo "Testing summarize_lecture job..."
	cd jobs/test_env && docker-compose -f docker-compose.test.yml --profile summarize up --build

# 퀴즈 생성 기능 테스트 (generate_quiz)
test-job-quiz:
	@echo "Testing generate_quiz job..."
	cd jobs/test_env && docker-compose -f docker-compose.test.yml --profile quiz up --build

# 시험 생성 기능 테스트 (generate_exam)
test-job-exam:
	@echo "Testing generate_exam job..."
	cd jobs/test_env && docker-compose -f docker-compose.test.yml --profile exam up --build

# 서술형 채점 기능 테스트
test-job-grade-quiz-essay:
	@echo "Testing grade_quiz_essay job..."
	cd jobs/test_env && docker-compose -f docker-compose.test.yml --profile grade-quiz-essay up --build

# 테스트용 셸 실행 (디버깅용)
test-job-shell:
	@echo "Starting a shell in test container..."
	cd jobs/test_env && docker-compose -f docker-compose.test.yml --profile shell run --rm shell

# 테스트 환경 중지
test-env-stop:
	@echo "Stopping test environment..."
	cd jobs/test_env && docker-compose -f docker-compose.test.yml down
	@echo "Test environment stopped successfully!"

# 테스트 환경 정리 (볼륨 포함)
test-env-clean:
	@echo "Cleaning test environment (including volumes)..."
	cd jobs/test_env && docker-compose -f docker-compose.test.yml down -v
	@echo "Test environment cleaned successfully!"

# -------------------------
# QnA Chat Langchain server 연결 (문맥 관리) 관련 명령어
# -------------------------

# 이미지 빌드
langchain-build:
	docker compose --profile langchain build langchain

# 실행
langchain-up:
	@echo "Running LangChain QnA Chat Context Generating Server..."
	docker compose --profile langchain up -d langchain
	@echo "Langchain Server is running at http://localhost:5000"

# 코드 변경 후 재시작
langchain-restart:
	@echo "Restarting LangChain server to apply code changes..."
	docker compose --profile langchain restart langchain
	@echo "LangChain server restarted with updated code!"

# 새로운 이미지로 빌드 및 재실행
langchain-rebuild:
	@echo "Rebuild LangChain server image and run that server..."
	docker compose --profile langchain build --no-cache langchain
	docker compose --profile langchain up -d langchain
	@echo "LangChain server rebuilt and running at http://localhost:5000"

# 실행 중지 (리소스 사용량 절약)
langchain-stop:
	@echo "Stopping LangChain QnA Chat Context Generating Server to reduce resource usage..."
	docker compose --profile langchain stop langchain
	@echo "LangChain server stopped successfully!"

# 컨테이너 제거 (down)
langchain-down:
	@echo "Stopping and removing LangChain QnA Chat Context Generating Server..."
	docker compose --profile langchain down langchain
	@echo "LangChain server stopped and removed successfully!"
