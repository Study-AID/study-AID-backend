FROM python:3.11-slim

# Install system dependencies
RUN apt-get update && apt-get install -y \
    curl \
    gcc \
    g++ \
    make \
    && rm -rf /var/lib/apt/lists/*

# Create app directory
WORKDIR /var/task

# Create jobs directory structure
RUN mkdir -p /var/task/jobs/generate_course_weakness_analysis

# Copy function code and prompts from repository root context first
COPY jobs/generate_course_weakness_analysis/ /var/task/jobs/generate_course_weakness_analysis/
COPY prompts/ /var/task/prompts/

# Install Python dependencies
RUN pip install --no-cache-dir -r /var/task/jobs/generate_course_weakness_analysis/requirements.txt

# Install Lambda runtime interface client
RUN pip install --no-cache-dir awslambdaric

# Set environment variables
ENV PYTHONPATH=/var/task

# 핸들러 실행 디렉토리를 jobs/generate_course_weakness_analysis로 변경
WORKDIR /var/task/jobs/generate_course_weakness_analysis

# Set the entrypoint
ENTRYPOINT [ "/usr/local/bin/python", "-m", "awslambdaric" ]

# Set the Lambda handler as CMD
CMD [ "handler.lambda_handler" ]