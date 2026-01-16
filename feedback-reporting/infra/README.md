# Lambda-4: Reporting - Configuração de Infraestrutura

## Variáveis de Ambiente

Configure as seguintes variáveis de ambiente na Lambda:

```bash
# Database (RDS PostgreSQL)
DB_HOST=your-rds-endpoint.region.rds.amazonaws.com
DB_PORT=5432
DB_NAME=feedback_db
DB_USER=feedback_user
DB_PASSWORD=your-secure-password

# AWS S3
REPORT_BUCKET_NAME=postech-feedback-reports

# AWS SNS
SNS_TOPIC_ARN=arn:aws:sns:us-east-1:123456789012:feedback-notification-topic

# Report Configuration
REPORT_FORMAT=json  # ou csv

# AWS Region
AWS_REGION=us-east-1
```

## EventBridge - Cron Semanal

### Opção 1: Via AWS CLI

```bash
# Criar a regra de agendamento (toda segunda-feira às 08:00 UTC)
aws events put-rule \
    --name "weekly-report-schedule" \
    --schedule-expression "cron(0 8 ? * MON *)" \
    --state ENABLED \
    --description "Executa a Lambda de Reporting toda segunda-feira às 08:00 UTC"

# Adicionar a Lambda como target
aws events put-targets \
    --rule "weekly-report-schedule" \
    --targets "Id"="ReportingLambdaTarget","Arn"="arn:aws:lambda:us-east-1:123456789012:function:feedback-reporting"

# Dar permissão ao EventBridge para invocar a Lambda
aws lambda add-permission \
    --function-name feedback-reporting \
    --statement-id EventBridgeInvoke \
    --action lambda:InvokeFunction \
    --principal events.amazonaws.com \
    --source-arn arn:aws:events:us-east-1:123456789012:rule/weekly-report-schedule
```

### Opção 2: Via JSON (EventBridge Scheduler)

Use o arquivo `eventbridge-schedule.json` neste diretório para criar o agendamento.

### Expressões Cron Alternativas

```bash
# Toda segunda às 08:00 UTC
cron(0 8 ? * MON *)

# Todo dia às 00:00 UTC (relatório diário)
cron(0 0 * * ? *)

# Primeiro dia de cada mês às 06:00 UTC (relatório mensal)
cron(0 6 1 * ? *)

# A cada 6 horas
rate(6 hours)
```

## IAM Policy

Use o arquivo `lambda-iam-policy.json` para criar a policy:

```bash
# Criar a policy
aws iam create-policy \
    --policy-name FeedbackReportingLambdaPolicy \
    --policy-document file://lambda-iam-policy.json

# Criar a role
aws iam create-role \
    --role-name FeedbackReportingLambdaRole \
    --assume-role-policy-document '{
        "Version": "2012-10-17",
        "Statement": [{
            "Effect": "Allow",
            "Principal": {"Service": "lambda.amazonaws.com"},
            "Action": "sts:AssumeRole"
        }]
    }'

# Anexar a policy à role
aws iam attach-role-policy \
    --role-name FeedbackReportingLambdaRole \
    --policy-arn arn:aws:iam::123456789012:policy/FeedbackReportingLambdaPolicy
```

## VPC Configuration

Para acessar o RDS, a Lambda precisa estar na mesma VPC:

```bash
aws lambda update-function-configuration \
    --function-name feedback-reporting \
    --vpc-config SubnetIds=subnet-xxx,subnet-yyy,SecurityGroupIds=sg-zzz
```

## S3 Bucket

Criar o bucket para os relatórios:

```bash
aws s3 mb s3://postech-feedback-reports --region us-east-1

# Configurar lifecycle para arquivar relatórios antigos (opcional)
aws s3api put-bucket-lifecycle-configuration \
    --bucket postech-feedback-reports \
    --lifecycle-configuration '{
        "Rules": [{
            "ID": "ArchiveOldReports",
            "Status": "Enabled",
            "Filter": {"Prefix": "reports/"},
            "Transitions": [{
                "Days": 90,
                "StorageClass": "GLACIER"
            }]
        }]
    }'
```

## Deploy da Lambda

```bash
# Build do projeto
cd feedback-reporting
mvn clean package -DskipTests

# Deploy via AWS CLI
aws lambda create-function \
    --function-name feedback-reporting \
    --runtime java21 \
    --handler org.springframework.cloud.function.adapter.aws.FunctionInvoker::handleRequest \
    --role arn:aws:iam::123456789012:role/FeedbackReportingLambdaRole \
    --zip-file fileb://target/feedback-reporting-1.0.0-SNAPSHOT-aws.jar \
    --timeout 60 \
    --memory-size 512 \
    --environment "Variables={DB_HOST=xxx,DB_PORT=5432,DB_NAME=feedback_db,DB_USER=xxx,DB_PASSWORD=xxx,REPORT_BUCKET_NAME=postech-feedback-reports,SNS_TOPIC_ARN=arn:aws:sns:us-east-1:123456789012:feedback-notification-topic,REPORT_FORMAT=json}"
```

## Estrutura de Arquivos no S3

Os relatórios são salvos seguindo este padrão:

```
s3://postech-feedback-reports/
└── reports/
    └── 2026/
        └── 01/
            ├── report-2026-01-06.json
            ├── report-2026-01-13.json
            └── report-2026-01-20.json
```

## Testando Localmente

Para testar localmente com LocalStack:

```bash
# Iniciar LocalStack
docker-compose up -d

# Criar recursos locais
aws --endpoint-url=http://localhost:4566 s3 mb s3://postech-feedback-reports
aws --endpoint-url=http://localhost:4566 sns create-topic --name feedback-notification-topic

# Executar a aplicação
cd feedback-reporting
mvn spring-boot:run
```
