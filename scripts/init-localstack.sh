#!/bin/bash

# Wait for LocalStack to be ready
echo "Waiting for LocalStack to be ready..."
max_retries=30
retry_count=0

while [ $retry_count -lt $max_retries ]; do
  if aws --endpoint-url=http://localhost:4566 sqs list-queues 2>/dev/null; then
    echo "LocalStack is ready"
    break
  fi
  echo "LocalStack is unavailable - sleeping"
  sleep 2
  retry_count=$((retry_count + 1))
done

if [ $retry_count -eq $max_retries ]; then
  echo "Failed to connect to LocalStack after $max_retries attempts"
  exit 1
fi

echo "LocalStack is up - executing command"

# Create SQS queue
echo "Creating SQS queue..."
aws --endpoint-url=http://localhost:4566 \
    sqs create-queue \
    --queue-name local-study-aid-summarize-lecture \
    --region ap-northeast-2

echo "SQS queue created successfully!"

# List queues to verify
echo "Listing queues..."
aws --endpoint-url=http://localhost:4566 \
    sqs list-queues \
    --region ap-northeast-2
