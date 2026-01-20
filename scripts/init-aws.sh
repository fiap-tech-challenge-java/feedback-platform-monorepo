#!/bin/bash
echo "⏳ Iniciando criação de recursos no LocalStack..."

# 1. Cria a Fila SQS (Ingestion -> Analysis)
awslocal sqs create-queue --queue-name feedback-analysis-queue

# 2. Cria o Tópico SNS (Analysis -> Notification)
awslocal sns create-topic --name feedback-notification-topic

# 3. Verifica E-mails no SES (Notification)
echo "📧 Verificando e-mails no SES..."
awslocal ses verify-email-identity --email-address noreply@feedbackplatform.com
awslocal ses verify-email-identity --email-address admin@feedbackplatform.com

# 4. Cria o Bucket S3 (Reporting)
awslocal s3 mb s3://postech-feedback-reports

echo "✅ Recursos criados com sucesso! Infraestrutura pronta."