.PHONY: help build run logs logs-api down clean ps redis-cli pgsql-cli test test_win test-coverage test-coverage_win open-test-report open-coverage-report migrate migration-info test-env-start test-env-setup test-job-summarize test-job-quiz test-job-exam test-job-shell test-env-stop test-env-clean

# Default target
help:
	@echo "Available commands:"
	@echo "  make build                - Build Docker images"
	@echo "  make run                  - Start all services"
	@echo "  make logs                 - Follow logs from all containers"
	@echo "  make logs-api             - Follow logs from API service"
	@echo "  make down                 - Stop all services"
	@echo "  make clean                - Remove all containers, volumes, and images"
	@echo "  make ps                   - List running containers"
	@echo "  make pgsql-cli            - Connect to PostgreSQL CLI"
	@echo "  make redis-cli            - Connect to Redis CLI"
	@echo "  make test                 - Run tests"
	@echo "  make test-win             - Run tests (Windows)"
	@echo "  make test-coverage        - Run tests with coverage report"
	@echo "  make test-coverage-win    - Run tests with coverage report (Windows)"
	@echo "  make open-test-report     - Open test report in browser"
	@echo "  make open-coverage-report - Open coverage report in browser"
	@echo "  make migrate              - Run Flyway migration"
	@echo "  make migration-info       - Check migrated schema versions and status"
	@echo ""
	@echo "Lambda Job Test Commands:"
	@echo "  make test-env-start       - Start test environment (PostgreSQL, LocalStack)"
	@echo "  make test-env-setup       - Setup test database with sample data"
	@echo "  make test-job-summarize   - Test summarize_lecture job"
	@echo "  make test-job-quiz        - Test generate_quiz job"
	@echo "  make test-job-shell       - Start a shell in test container for debugging"
	@echo "  make test-env-stop        - Stop test environment containers"
	@echo "  make test-env-clean       - Remove test environment including volumes"


# Build Docker images
build:
	docker compose build

# Start all services
run:
	docker compose up -d
	@echo "API Server is running at http://localhost:8080/api"
	@echo "Swagger UI is available at http://localhost:8080/api/swagger-ui.html"

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

# -------------------------
# Lambda 테스트 관련 명령어
# -------------------------

# 테스트 환경 시작 (PostgreSQL, LocalStack)
test-env-start:
	@echo "Starting test environment (PostgreSQL, LocalStack)..."
	cd jobs/test_env && docker-compose -f docker-compose.test.yml up -d postgres-test localstack-test setup-s3
	@echo "Test environment started successfully!"
	@echo "Database: PostgreSQL on localhost:5433"
	@echo "S3: LocalStack on localhost:4566"

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
