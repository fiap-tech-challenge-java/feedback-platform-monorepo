#!/bin/bash
set -e

echo "🧪 TESTE END-TO-END - FEEDBACK REPORTING"
echo "========================================="

cd /home/tet/Documents/feedback-platform-monorepo

# 1. Infraestrutura
echo ""
echo "🐳 [1/6] Subindo infraestrutura..."
docker compose up -d
sleep 8
docker compose ps

# 2. Inserir dados de teste
echo ""
echo "📊 [2/6] Inserindo dados de teste..."
docker exec -i feedback-db psql -U user_feedback -d feedback_db <<'SQL'
DROP TABLE IF EXISTS feedbacks CASCADE;
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
('Excelente aula!', 5, 'NORMAL', NOW() - INTERVAL '1 day'),
('Muito bom', 5, 'NORMAL', NOW() - INTERVAL '2 days'),
('Precisa melhorar', 2, 'CRITICAL', NOW() - INTERVAL '3 days'),
('Bom conteúdo', 4, 'NORMAL', NOW() - INTERVAL '4 days'),
('Ótimo!', 5, 'NORMAL', NOW() - INTERVAL '5 days');
SELECT COUNT(*) as total FROM feedbacks;
SQL

# 3. Compilar
echo ""
echo "⚙️  [3/6] Compilando..."
mvn clean install -pl feedback-core -DskipTests -q
mvn clean package -pl feedback-reporting -am -DskipTests -q
echo "✅ Compilação concluída!"

# 4. Iniciar aplicação
echo ""
echo "🚀 [4/6] Iniciando aplicação..."
cd feedback-reporting
mvn spring-boot:run > /tmp/app.log 2>&1 &
APP_PID=$!
echo "PID: $APP_PID"
sleep 18

# 5. Verificar logs
echo ""
echo "📋 [5/6] Logs de inicialização:"
grep -E "LocalStack|bucket|topic|✓|✅|Started" /tmp/app.log | head -10

# 6. Testar endpoint
echo ""
echo "🔥 [6/6] Testando geração de relatório..."
RESPONSE=$(curl -s -X POST http://localhost:8080/generateReport \
    -H "Content-Type: application/json" \
    -d '{"source": "test"}')
echo "$RESPONSE" | jq '.' 2>/dev/null || echo "$RESPONSE"

# Verificar S3
echo ""
echo "📂 Arquivos no S3:"
docker exec feedback-aws awslocal s3 ls s3://postech-feedback-reports/reports/ --recursive 2>/dev/null || echo "Bucket vazio ou não existe"

echo ""
echo "✅ TESTE CONCLUÍDO!"
echo "Para parar: kill $APP_PID"

