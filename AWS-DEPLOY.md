# Deploy AWS Lambda - Feedback Platform

Este documento descreve como fazer deploy da aplicação Feedback Platform usando **AWS Lambda** com **SAM (Serverless Application Model)**.

## 📐 Arquitetura Serverless

```
                                    ┌─────────────────┐
                                    │   API Gateway   │
                                    └────────┬────────┘
                                             │
                    ┌────────────────────────┼────────────────────────┐
                    │                        │                        │
                    ▼                        ▼                        ▼
           ┌───────────────┐        ┌───────────────┐        ┌───────────────┐
           │   Lambda 1    │        │   Lambda 4    │        │   Lambda 4    │
           │   Ingestion   │        │   Reporting   │        │   (Schedule)  │
           └───────┬───────┘        └───────────────┘        └───────────────┘
                   │                        │
                   ▼                        ▼
           ┌───────────────┐        ┌───────────────┐
           │   SQS Queue   │        │   S3 Bucket   │
           │   (Feedbacks) │        │   (Reports)   │
           └───────┬───────┘        └───────────────┘
                   │
                   ▼
           ┌───────────────┐
           │   Lambda 2    │
           │   Analysis    │
           └───────┬───────┘
                   │
                   ▼
           ┌───────────────┐
           │   SNS Topic   │
           │ (Notifications)│
           └───────┬───────┘
                   │
                   ▼
           ┌───────────────┐
           │   Lambda 3    │
           │  Notification │
           └───────┬───────┘
                   │
                   ▼
           ┌───────────────┐
           │    AWS SES    │
           │   (Emails)    │
           └───────────────┘
```

## 📁 Arquivos do Projeto

```
feedback-platform-monorepo/
├── template.yaml           # Template SAM (CloudFormation)
├── samconfig.toml          # Configuração do SAM CLI
├── deploy.ps1              # Script de deploy (Windows)
├── deploy.sh               # Script de deploy (Linux/Mac)
├── events/                 # Eventos de teste para Lambdas
│   ├── api-gateway-post.json
│   ├── sqs-event.json
│   ├── sns-event.json
│   └── scheduled-event.json
├── feedback-core/          # Módulo compartilhado
├── feedback-ingestion/     # Lambda 1: API REST → SQS
├── feedback-analysis/      # Lambda 2: SQS → SNS
├── feedback-notification/  # Lambda 3: SNS → SES
└── feedback-reporting/     # Lambda 4: Schedule → S3
```

## 🛠️ Pré-requisitos

### 1. Instalar AWS SAM CLI

**Windows (Chocolatey):**
```powershell
choco install aws-sam-cli
```

**Windows (MSI):**
Download: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html

**Linux/Mac:**
```bash
brew install aws-sam-cli
# ou
pip install aws-sam-cli
```

### 2. Verificar instalações

```powershell
sam --version       # AWS SAM CLI, version 1.x.x
mvn --version       # Apache Maven 3.x.x
java --version      # openjdk 21.x.x
aws --version       # aws-cli/2.x.x
```

### 3. Configurar AWS CLI

```powershell
aws configure
# AWS Access Key ID: <sua-access-key>
# AWS Secret Access Key: <sua-secret-key>
# Default region name: us-east-2
# Default output format: json
```

## 🚀 Passos para Deploy

### Opção 1: Script automatizado

**Windows:**
```powershell
.\deploy.ps1 -Environment dev -Guided
```

**Linux/Mac:**
```bash
chmod +x deploy.sh
./deploy.sh dev
```

### Opção 2: Comandos manuais

```powershell
# 1. Build do módulo core
cd feedback-core
mvn clean install -DskipTests

# 2. Build de cada Lambda
cd ../feedback-ingestion && mvn clean package -DskipTests
cd ../feedback-analysis && mvn clean package -DskipTests
cd ../feedback-notification && mvn clean package -DskipTests
cd ../feedback-reporting && mvn clean package -DskipTests

# 3. Voltar para raiz
cd ..

# 4. SAM Build
sam build

# 5. SAM Deploy (primeira vez - guiado)
sam deploy --guided
```

## 📝 Parâmetros do Deploy

Durante o deploy guiado (`--guided`), você será perguntado:

| Parâmetro | Descrição | Exemplo |
|-----------|-----------|---------|
| Stack Name | Nome do stack CloudFormation | `feedback-platform-dev` |
| AWS Region | Região AWS | `us-east-2` |
| Environment | Ambiente (dev/staging/prod) | `dev` |
| DatabaseUrl | URL JDBC do RDS | `jdbc:postgresql://xxx.rds.amazonaws.com:5432/feedbackdb` |
| DatabaseUsername | Usuário do banco | `admin` |
| DatabasePassword | Senha do banco | `*****` |
| NotificationEmail | Email para envio | `noreply@example.com` |

## 🗄️ Criar RDS PostgreSQL (Free Tier)

Antes do deploy, crie o banco de dados:

1. **AWS Console** → **RDS** → **Create database**
2. Configure:
   - Engine: **PostgreSQL**
   - Template: **Free tier**
   - DB identifier: `feedback-db`
   - Master username: `admin`
   - Password: (defina uma senha)
   - Instance class: `db.t3.micro`
   - Storage: `20 GB`
   - Public access: **Yes** (para Lambdas sem VPC)
3. **Anote o Endpoint** após criação

## 🔧 Comandos Úteis

```powershell
# Ver logs de uma Lambda
sam logs -n FeedbackIngestionFunction --stack-name feedback-platform-dev --tail

# Invocar Lambda localmente
sam local invoke FeedbackIngestionFunction --event events/api-gateway-post.json

# Iniciar API localmente
sam local start-api

# Validar template
sam validate

# Deletar stack
sam delete --stack-name feedback-platform-dev
```

## 🧪 Testando a API

Após o deploy, você receberá a URL da API:

```bash
# Criar feedback
curl -X POST https://xxx.execute-api.us-east-2.amazonaws.com/dev/feedbacks \
  -H "Content-Type: application/json" \
  -d '{
    "studentId": 1,
    "teacherId": 1,
    "courseId": 1,
    "rating": 5,
    "comment": "Excelente aula!"
  }'

# Listar feedbacks
curl https://xxx.execute-api.us-east-2.amazonaws.com/dev/feedbacks

# Buscar por ID
curl https://xxx.execute-api.us-east-2.amazonaws.com/dev/feedbacks/1
```

## 💰 Custos (Free Tier)

| Recurso | Free Tier | Uso Típico |
|---------|-----------|------------|
| Lambda | 1M requisições/mês | ✅ Gratuito |
| Lambda | 400.000 GB-segundos | ✅ Gratuito |
| API Gateway | 1M chamadas/mês | ✅ Gratuito |
| SQS | 1M requisições/mês | ✅ Gratuito |
| SNS | 1M publicações/mês | ✅ Gratuito |
| S3 | 5 GB armazenamento | ✅ Gratuito |
| RDS | 750 hrs db.t3.micro | ✅ Gratuito |
| CloudWatch | 5 GB logs | ✅ Gratuito |

**Custo estimado dentro do Free Tier: $0/mês** 🎉

## 🔍 Monitoramento

### CloudWatch Logs

Cada Lambda cria um Log Group automaticamente:
- `/aws/lambda/feedback-ingestion-dev`
- `/aws/lambda/feedback-analysis-dev`
- `/aws/lambda/feedback-notification-dev`
- `/aws/lambda/feedback-reporting-dev`

### Métricas

**AWS Console** → **CloudWatch** → **Metrics** → **Lambda**

## 🐛 Troubleshooting

### Erro: "Unable to import module"
```bash
# Verificar se o JAR -aws.jar foi gerado
ls feedback-ingestion/target/*-aws.jar
```

### Erro: "Task timed out"
Aumente o timeout no `template.yaml`:
```yaml
Timeout: 60  # segundos
```

### Erro: "Out of memory"
Aumente a memória no `template.yaml`:
```yaml
MemorySize: 1024  # MB
```

### Cold Start lento
Adicione SnapStart para Java:
```yaml
SnapStart:
  ApplyOn: PublishedVersions
```

## 📚 Referências

- [AWS SAM Documentation](https://docs.aws.amazon.com/serverless-application-model/)
- [Spring Cloud Function](https://spring.io/projects/spring-cloud-function)
- [AWS Lambda Java](https://docs.aws.amazon.com/lambda/latest/dg/lambda-java.html)
