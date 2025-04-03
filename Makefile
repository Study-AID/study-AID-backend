.PHONY: help build run logs logs-api down clean ps redis-cli pgsql-cli test test-coverage clean-test clean-test-coverage open-test-report open-coverage-report

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
	@echo "  make test-coverage        - Run tests with coverage report"
	@echo "  make open-test-report     - Open test report in browser"
	@echo "  make open-coverage-report - Open coverage report in browser"

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

# Run tests with coverage report
test-coverage:
	./gradlew test jacocoTestReport --info

# Open coverage report in browser
open-coverage-report:
	open build/reports/jacoco/test/html/index.html

# Open test report in browser
open-test-report:
	open build/reports/tests/test/index.html
