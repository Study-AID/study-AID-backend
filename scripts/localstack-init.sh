#!/bin/bash

# Start LocalStack in the background
docker-entrypoint.sh &

# Wait for LocalStack to be ready
until aws --endpoint-url=http://localhost:4566 sqs list-queues 2>/dev/null; do
  echo "Waiting for LocalStack to be ready..."
  sleep 2
done

echo "LocalStack is ready - creating SQS queue"

# Create SQS queue
aws --endpoint-url=http://localhost:4566 \
    sqs create-queue \
    --queue-name local-study-aid-summarize-lecture \
    --region ap-northeast-2

echo "SQS queue created successfully!"

# Keep the container running
wait
