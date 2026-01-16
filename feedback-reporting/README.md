# 🧪 Guia de Teste - Feedback Reporting

Este guia mostra como testar o módulo `feedback-reporting` com toda a infraestrutura local (PostgreSQL + LocalStack).

## 📋 Pré-requisitos

- Docker e Docker Compose instalados
- Java 21+
- Maven 3.8+
- jq (opcional, para formatação JSON)

## 🚀 Teste Rápido (Script Automatizado)

Execute o script de teste de integração:

```bash
chmod +x test-integration.sh
./test-integration.sh
```

O script vai:
1. Parar containers antigos
2. Subir PostgreSQL + LocalStack
3. Criar bucket S3, tópico SNS e fila SQS
4. Inserir 10 feedbacks de teste no banco
5. Compilar o projeto

---

## 🔧 Teste Manual (Passo a Passo)

### 1️⃣ Subir a Infraestrutura

```bash
cd /home/tet/Documents/feedback-platform-monorepo
docker compose down -v
docker compose up -d
sleep 12
docker compose ps
```

**Resultado esperado:**
```
NAME           STATUS    PORTS
feedback-aws   running   4566->4566, 4510-4559->4510-4559
feedback-db    running   5432->5432
```

### 2️⃣ Criar Recursos no LocalStack

```bash
# Criar bucket S3
docker exec feedback-aws awslocal s3 mb s3://postech-feedback-reports

# Criar tópico SNS
docker exec feedback-aws awslocal sns create-topic --name feedback-notification-topic

# Criar fila SQS
docker exec feedback-aws awslocal sqs create-queue --queue-name report-notifications

# Inscrever fila no tópico SNS
docker exec feedback-aws awslocal sns subscribe \
  --topic-arn arn:aws:sns:us-east-1:000000000000:feedback-notification-topic \
  --protocol sqs \
  --notification-endpoint arn:aws:sqs:us-east-1:000000000000:report-notifications
```

### 3️⃣ Verificar Recursos Criados

```bash
# Listar buckets S3
docker exec feedback-aws awslocal s3 ls

# Listar tópicos SNS
docker exec feedback-aws awslocal sns list-topics

# Listar filas SQS
docker exec feedback-aws awslocal sqs list-queues
```

### 4️⃣ Inserir Dados de Teste no PostgreSQL

```bash
docker exec -i feedback-db psql -U user_feedback -d feedback_db <<'SQL'
DROP TABLE IF EXISTS feedbacks;
CREATE TABLE feedbacks (
    id SERIAL PRIMARY KEY,
    description TEXT NOT NULL,
    rating INTEGER NOT NULL,
    status VARCHAR(20) NOT NULL,
    user_id VARCHAR(100),
    product_id VARCHAR(100),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO feedbacks (description, rating, status, created_at) VALUES
('Excelente aula sobre AWS Lambda!', 5, 'NORMAL', '2026-01-10 10:30:00'),
('Muito boa explicação sobre S3', 5, 'NORMAL', '2026-01-10 14:00:00'),
('Aula confusa, precisa melhorar', 2, 'CRITICAL', '2026-01-11 09:00:00'),
('Bom conteúdo, mas muito rápido', 3, 'CRITICAL', '2026-01-11 11:30:00'),
('Perfeito! Aprendi muito', 5, 'NORMAL', '2026-01-12 08:00:00'),
('Material desatualizado', 1, 'CRITICAL', '2026-01-12 15:00:00'),
('Ótimo professor!', 4, 'NORMAL', '2026-01-13 10:00:00'),
('Precisa de mais exemplos práticos', 3, 'CRITICAL', '2026-01-13 16:00:00'),
('Muito bom o hands-on', 5, 'NORMAL', '2026-01-14 09:30:00'),
('Aula excelente sobre EventBridge', 5, 'NORMAL', '2026-01-14 14:00:00');
SQL
```

### 5️⃣ Verificar Dados Inseridos

```bash
docker exec -i feedback-db psql -U user_feedback -d feedback_db -c "SELECT COUNT(*) FROM feedbacks;"
docker exec -i feedback-db psql -U user_feedback -d feedback_db -c "SELECT id, description, rating, status FROM feedbacks;"
```

**Resultado esperado:**
```
 count 
-------
    10
```

### 6️⃣ Compilar o Projeto

```bash
mvn clean install -pl feedback-core -DskipTests
mvn clean package -pl feedback-reporting -am -DskipTests
```

### 7️⃣ Iniciar a Aplicação

**Terminal 1:**
```bash
mvn spring-boot:run -pl feedback-reporting
```

Aguarde a mensagem:
```
Started FeedbackReportingApplication in X.XXX seconds
```

### 8️⃣ Testar Geração de Relatório

**Terminal 2:**
```bash
curl -X POST http://localhost:8080/generateReport \
  -H "Content-Type: application/json" \
  -d '{"source": "test"}' | jq .
```

**Resultado esperado:**
```json
{
  "generatedAt": "2026-01-16T02:21:03.107073861",
  "averageScore": 3.8,
  "statusCode": 200,
  "totalFeedbacks": 10,
  "reportUrl": "https://postech-feedback-reports.s3.us-east-1.amazonaws.com/reports/2026/01/report-2026-01-16.json",
  "message": "Weekly report generated successfully"
}
```

### 9️⃣ Verificar Relatório no S3

```bash
# Listar arquivos no bucket
docker exec feedback-aws awslocal s3 ls s3://postech-feedback-reports/reports/ --recursive

# Ver conteúdo do relatório (ajuste a data conforme necessário)
docker exec feedback-aws awslocal s3 cp s3://postech-feedback-reports/reports/2026/01/report-2026-01-16.json - | jq .
```

**Resultado esperado:**
```json
{
  "type": "WEEKLY_REPORT",
  "generatedAt": "2026-01-16T02:21:03Z",
  "period": "weekly",
  "summary": {
    "totalFeedbacks": 10,
    "averageScore": 3.8
  },
  "feedbacksByDay": {
    "2026-01-10": 2,
    "2026-01-11": 2,
    "2026-01-12": 2,
    "2026-01-13": 2,
    "2026-01-14": 2
  },
  "feedbacksByUrgency": {
    "LOW": 6,
    "MEDIUM": 0,
    "HIGH": 4
  },
  "feedbacks": [...]
}
```

### 🔟 Verificar Notificação SNS na Fila SQS

```bash
docker exec feedback-aws awslocal sqs receive-message \
  --queue-url http://localhost:4566/000000000000/report-notifications | jq .
```

**Resultado esperado:**
```json
{
  "Messages": [
    {
      "Body": "{\"eventType\":\"REPORT_READY\",\"message\":\"Weekly report generated\",\"reportLink\":\"https://...\",\"generatedAt\":\"2026-01-16T02:21:03Z\"}"
    }
  ]
}
```

---

## 🛑 Parar a Infraestrutura

```bash
docker compose down -v
```

---

## 📊 Resumo dos Endpoints

| Método | Endpoint | Descrição |
|--------|----------|-----------|
| POST | `/generateReport` | Gera relatório semanal e envia para S3 |

---

## 🔍 Troubleshooting

### Erro: `NoSuchMethodError: countTotalFeedbacks()`
O JAR do `feedback-core` está desatualizado. Execute:
```bash
mvn clean install -pl feedback-core -DskipTests
mvn clean package -pl feedback-reporting -am -DskipTests
```

### Erro: `Connection refused` no PostgreSQL
Verifique se o container está rodando:
```bash
docker compose ps
docker compose logs feedback-db
```

### Erro: `Bucket not found` no S3
Verifique se o bucket foi criado:
```bash
docker exec feedback-aws awslocal s3 ls
```

---

## 📁 Estrutura de Arquivos Gerados

```
s3://postech-feedback-reports/
└── reports/
    └── 2026/
        └── 01/
            └── report-2026-01-16.json
```

---

## ✅ Checklist de Validação

- [ ] Containers `feedback-db` e `feedback-aws` rodando
- [ ] Bucket `postech-feedback-reports` criado no S3
- [ ] Tópico `feedback-notification-topic` criado no SNS
- [ ] Fila `report-notifications` criada no SQS
- [ ] 10 feedbacks inseridos no PostgreSQL
- [ ] Aplicação `feedback-reporting` iniciada na porta 8080
- [ ] Endpoint `/generateReport` retorna status 200
- [ ] Relatório JSON salvo no S3
- [ ] Notificação enviada para fila SQS

