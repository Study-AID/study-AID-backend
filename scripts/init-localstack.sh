#!/bin/bash

# Wait for LocalStack to be ready
echo "Waiting for LocalStack to be ready..."
max_retries=30
retry_count=0

while [ $retry_count -lt $max_retries ]; do
  # Simply check if LocalStack is responding
  if curl -sf http://localhost:4566/_localstack/health > /dev/null; then
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

echo "LocalStack is up - creating SQS queue"

# Create SQS queue using AWS CLI command via curl
QUEUE_NAME="local-study-aid-summarize-lecture"

# Create queue
echo "Creating queue: $QUEUE_NAME"
curl -X POST "http://localhost:4566/" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "Action=CreateQueue&QueueName=$QUEUE_NAME&Version=2012-11-05"

echo -e "\n\nListing queues..."
curl -X POST "http://localhost:4566/" \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "Action=ListQueues&Version=2012-11-05"

echo -e "\n\nSetup completed!"