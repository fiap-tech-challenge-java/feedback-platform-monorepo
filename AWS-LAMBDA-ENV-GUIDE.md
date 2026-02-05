# 🚀 AWS Lambda Deployment Guide

## 📌 Visão Geral

Este guia explica como configurar as Lambda Functions do Feedback Platform para usar variáveis de ambiente na AWS.

## 📋 Pré-requisitos

1. Conta AWS ativa
2. AWS CLI configurada
3. IAM Role com permissões para Lambda, SQS, SNS, S3, SES e RDS
4. RDS PostgreSQL disponível
5. Emails verificados no SES (para notificações)

## 🔧 Arquitetura

```
                    ┌──────────────────┐
                    │   API Gateway    │
                    └────────┬─────────┘
                             │
                    ┌────────▼─────────┐     ┌─────────────┐
                    │ Lambda Ingestion │────▶│  PostgreSQL │
                    └────────┬─────────┘     │    (RDS)    │
                             │               └─────────────┘
                    ┌────────▼─────────┐
                    │    SQS Queue     │
                    └────────┬─────────┘
                             │
                    ┌────────▼─────────┐
                    │ Lambda Analysis  │
                    └────────┬─────────┘
                             │
                    ┌────────▼─────────┐
                    │    SNS Topic     │
                    └────────┬─────────┘
                             │
    ┌────────────────────────┼────────────────────────┐
    │                        │                        │
┌───▼───────────────┐  ┌────▼───────────────┐  ┌─────▼─────────┐
│ Lambda Notification│  │ Lambda Reporting   │  │    EventBridge │
│  (Email via SES)  │  │  (S3 + SNS)        │  │    (Scheduled) │
└───────────────────┘  └────────────────────┘  └───────────────┘
```

## 📦 Variáveis de Ambiente por Lambda

### 1️⃣ Lambda: Feedback Ingestion

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://your-rds-endpoint:5432/feedback_db
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=your-secure-password

# AWS
AWS_REGION=us-east-2
SQS_QUEUE_NAME=feedback-analysis-queue
SQS_QUEUE_URL=https://sqs.us-east-2.amazonaws.com/123456789012/feedback-analysis-queue
```

### 2️⃣ Lambda: Feedback Analysis

```bash
# AWS
AWS_REGION=us-east-2
SQS_QUEUE_NAME=feedback-analysis-queue
SNS_TOPIC_ARN=arn:aws:sns:us-east-2:123456789012:feedback-notifications
```

### 3️⃣ Lambda: Feedback Notification

```bash
# AWS
AWS_REGION=us-east-2
SES_FROM_EMAIL=noreply@yourcompany.com
SES_RECIPIENT_EMAIL=admin@yourcompany.com
AWS_SES_ENABLED=true
```

### 4️⃣ Lambda: Feedback Reporting

```bash
# Database
SPRING_DATASOURCE_URL=jdbc:postgresql://your-rds-endpoint:5432/feedback_db
SPRING_DATASOURCE_USERNAME=admin
SPRING_DATASOURCE_PASSWORD=your-secure-password

# AWS
AWS_REGION=us-east-2
S3_BUCKET_NAME=postech-feedback-reports-123456789012
SNS_TOPIC_ARN=arn:aws:sns:us-east-2:123456789012:feedback-notifications
REPORT_FORMAT=csv
```

## 🛠 Configuração via AWS CLI

### Criar recursos

```bash
# Criar fila SQS
aws sqs create-queue \
  --queue-name feedback-analysis-queue \
  --region us-east-2

# Criar tópico SNS
aws sns create-topic \
  --name feedback-notifications \
  --region us-east-2

# Criar bucket S3
aws s3 mb s3://postech-feedback-reports-$(aws sts get-caller-identity --query Account --output text) \
  --region us-east-2

# Verificar email no SES
aws ses verify-email-identity \
  --email-address noreply@yourcompany.com \
  --region us-east-2
```

### Configurar variáveis de ambiente na Lambda

```bash
# Ingestion Lambda
aws lambda update-function-configuration \
  --function-name FeedbackIngestionFunction \
  --environment "Variables={
    SPRING_DATASOURCE_URL=jdbc:postgresql://your-rds:5432/feedbackdb,
    SPRING_DATASOURCE_USERNAME=admin,
    SPRING_DATASOURCE_PASSWORD=yourpassword,
    AWS_REGION=us-east-2,
    SQS_QUEUE_NAME=feedback-analysis-queue,
    SQS_QUEUE_URL=https://sqs.us-east-2.amazonaws.com/123456789012/feedback-analysis-queue
  }"

# Analysis Lambda
aws lambda update-function-configuration \
  --function-name FeedbackAnalysisFunction \
  --environment "Variables={
    AWS_REGION=us-east-2,
    SQS_QUEUE_NAME=feedback-analysis-queue,
    SNS_TOPIC_ARN=arn:aws:sns:us-east-2:123456789012:feedback-notifications
  }"

# Notification Lambda
aws lambda update-function-configuration \
  --function-name FeedbackNotificationFunction \
  --environment "Variables={
    AWS_REGION=us-east-2,
    SES_FROM_EMAIL=noreply@yourcompany.com,
    SES_RECIPIENT_EMAIL=admin@yourcompany.com,
    AWS_SES_ENABLED=true
  }"

# Reporting Lambda
aws lambda update-function-configuration \
  --function-name FeedbackReportingFunction \
  --environment "Variables={
    SPRING_DATASOURCE_URL=jdbc:postgresql://your-rds:5432/feedbackdb,
    SPRING_DATASOURCE_USERNAME=admin,
    SPRING_DATASOURCE_PASSWORD=yourpassword,
    AWS_REGION=us-east-2,
    S3_BUCKET_NAME=postech-feedback-reports-123456789012,
    SNS_TOPIC_ARN=arn:aws:sns:us-east-2:123456789012:feedback-notifications,
    REPORT_FORMAT=csv
  }"
```

## 📁 Deploy com SAM (AWS SAM Template)

O arquivo `template.yaml` já está configurado para usar variáveis de ambiente. Para fazer o deploy:

```bash
# Build
sam build

# Deploy
sam deploy --guided
```

Durante o deploy guiado, você pode fornecer os valores das variáveis.

## 🔐 Boas Práticas de Segurança

1. **Use AWS Secrets Manager** para senhas de banco de dados
2. **Habilite encryption at rest** no S3 e RDS
3. **Use VPC Endpoints** para comunicação segura
4. **Limite permissões IAM** ao mínimo necessário
5. **Nunca commite** arquivos `.env.production` com valores reais

## 🧪 Testando Localmente

Use LocalStack para testes locais:

```bash
# Inicie o LocalStack
docker-compose up -d

# Configure variáveis para LocalStack
export AWS_ENDPOINT=http://localhost:4566
export AWS_ACCESS_KEY_ID=localstack
export AWS_SECRET_ACCESS_KEY=localstack

# Execute os testes
./mvnw test
```

## ❓ Troubleshooting

### Erro: "Missing required property: SQS_QUEUE_URL"
- Verifique se a variável `SQS_QUEUE_URL` está configurada na Lambda
- A URL deve ser o endereço completo da fila

### Erro: "Email address is not verified"
- Verifique o email no SES: `aws ses verify-email-identity --email-address seu-email@dominio.com`
- Aguarde o email de confirmação e clique no link

### Erro: "Access Denied to S3 bucket"
- Verifique se a IAM Role da Lambda tem permissão `s3:PutObject`
- Verifique o nome do bucket e se ele existe

### Erro: "Unable to connect to database"
- Verifique se o Security Group do RDS permite conexões da Lambda
- Use VPC para Lambda se o RDS estiver em VPC privada

## 📞 Suporte

Para problemas ou dúvidas, abra uma issue no repositório.
