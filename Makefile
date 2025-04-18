.PHONY: help build run logs logs-api down clean ps redis-cli pgsql-cli test test_win test-coverage test-coverage_win clean-test clean-test-coverage open-test-report open-coverage-report migrate migration-info

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