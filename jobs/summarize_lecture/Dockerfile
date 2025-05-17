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
RUN mkdir -p /var/task/jobs/summarize_lecture

# Copy function code and prompts from repository root context first
COPY jobs/summarize_lecture/ /var/task/jobs/summarize_lecture/
COPY prompts/ /var/task/prompts/

# Install Python dependencies
RUN pip install --no-cache-dir -r /var/task/jobs/summarize_lecture/requirements.txt

# Create the lambda_handler wrapper
RUN echo 'import sys\nsys.path.insert(0, ".")\nfrom jobs.summarize_lecture.handler import handler\n\ndef lambda_handler(event, context):\n    return handler(event, context)' > /var/task/lambda_handler.py

# Install Lambda runtime interface client
RUN pip install --no-cache-dir awslambdaric

# Set environment variables
ENV PYTHONPATH=/var/task

# Set the entrypoint
ENTRYPOINT [ "/usr/local/bin/python", "-m", "awslambdaric" ]

# Set the Lambda handler as CMD
CMD [ "lambda_handler.lambda_handler" ]
