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

# Copy necessary modules directly to root to avoid import issues
RUN cp /var/task/jobs/summarize_lecture/openai_client.py /var/task/
RUN cp /var/task/jobs/summarize_lecture/summary_models.py /var/task/
RUN cp /var/task/jobs/summarize_lecture/handler.py /var/task/summarize_handler.py

# Create the lambda_handler wrapper with direct imports
RUN echo 'import sys\nimport os\n\n# Add current directory to path\nsys.path.insert(0, os.path.dirname(os.path.realpath(__file__)))\n\n# Import the handler directly\nfrom summarize_handler import handler\n\n# Lambda handler function\ndef lambda_handler(event, context):\n    return handler(event, context)' > /var/task/lambda_handler.py

# Install Lambda runtime interface client
RUN pip install --no-cache-dir awslambdaric

# Set environment variables
ENV PYTHONPATH=/var/task

# Set the entrypoint
ENTRYPOINT [ "/usr/local/bin/python", "-m", "awslambdaric" ]

# Set the Lambda handler as CMD
CMD [ "lambda_handler.lambda_handler" ]
