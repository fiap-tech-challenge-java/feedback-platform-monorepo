#!/bin/bash
set -e

echo "=============================================="
echo "🚀 TESTE DE INTEGRAÇÃO - FEEDBACK REPORTING"
echo "=============================================="

cd /home/tet/Documents/feedback-platform-monorepo

echo ""
echo "1️⃣  Parando containers antigos..."
docker compose down -v 2>/dev/null || true

echo ""
echo "2️⃣  Subindo infraestrutura (Postgres + LocalStack)..."
docker compose up -d
sleep 12

echo ""
echo "3️⃣  Verificando containers..."
docker compose ps

echo ""
echo "4️⃣  Criando recursos no LocalStack..."
docker exec feedback-aws awslocal s3 mb s3://postech-feedback-reports 2>/dev/null || echo "Bucket já existe"
docker exec feedback-aws awslocal sns create-topic --name feedback-notification-topic 2>/dev/null || echo "Topic já existe"
docker exec feedback-aws awslocal sqs create-queue --queue-name report-notifications 2>/dev/null || echo "Queue já existe"
docker exec feedback-aws awslocal sns subscribe \
  --topic-arn arn:aws:sns:us-east-2:000000000000:feedback-notification-topic \
  --protocol sqs \
  --notification-endpoint arn:aws:sqs:us-east-2:000000000000:report-notifications 2>/dev/null || true

echo ""
echo "5️⃣  Listando recursos criados..."
echo "--- S3 Buckets ---"
docker exec feedback-aws awslocal s3 ls
echo "--- SNS Topics ---"
docker exec feedback-aws awslocal sns list-topics
echo "--- SQS Queues ---"
docker exec feedback-aws awslocal sqs list-queues

echo ""
echo "6️⃣  Inserindo dados de teste no PostgreSQL..."
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

echo ""
echo "7️⃣  Verificando dados inseridos..."
docker exec -i feedback-db psql -U user_feedback -d feedback_db -c "SELECT COUNT(*) as total FROM feedbacks;"

echo ""
echo "8️⃣  Compilando o projeto..."
mvn clean package -pl feedback-reporting -am -DskipTests -q

echo ""
echo "=============================================="
echo "✅ INFRAESTRUTURA PRONTA!"
echo "=============================================="
echo ""
echo "Agora execute em terminais separados:"
echo ""
echo "TERMINAL 1 - Iniciar a aplicação:"
echo "  mvn spring-boot:run -pl feedback-reporting"
echo ""
echo "TERMINAL 2 - Testar o endpoint (após app iniciar):"
echo "  curl -X POST http://localhost:8080/generateReport -H 'Content-Type: application/json' -d '{\"source\": \"test\"}' | jq ."
echo ""
echo "TERMINAL 2 - Verificar relatório no S3:"
echo "  docker exec feedback-aws awslocal s3 ls s3://postech-feedback-reports/reports/ --recursive"
echo ""
echo "TERMINAL 2 - Ver conteúdo do relatório:"
echo "  docker exec feedback-aws awslocal s3 cp s3://postech-feedback-reports/reports/2026/01/report-2026-01-16.json - | jq ."
echo ""
