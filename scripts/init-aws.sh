#!/bin/bash
echo "⏳ Iniciando criação de recursos no LocalStack..."

# 1. Cria a Fila SQS (Ingestion -> Analysis)
awslocal sqs create-queue --queue-name feedback-analysis-queue

# 2. Cria o Tópico SNS (Analysis -> Notification)
awslocal sns create-topic --name feedback-notification-topic

# 3. Cria o Bucket S3 (Reporting)
awslocal s3 mb s3://postech-feedback-reports

echo "✅ Recursos criados com sucesso! Infraestrutura pronta."