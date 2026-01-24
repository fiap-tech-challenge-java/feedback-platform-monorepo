#!/bin/bash
# ===========================================
# Script para criar recursos AWS via CLI
# ===========================================
# Este script cria os recursos necessários na AWS
# Requer: AWS CLI configurado com credenciais
# ===========================================

set -e

# Variáveis - ALTERE CONFORME NECESSÁRIO
AWS_REGION="us-east-2"
PROJECT_NAME="feedback-platform"
DB_USERNAME="admin"
DB_PASSWORD="SuaSenhaSegura123!"  # ALTERE ESTA SENHA!

echo "========================================="
echo "   Criando recursos AWS                 "
echo "========================================="
echo ""

# 1. Criar repositórios ECR
echo "Criando repositórios ECR..."
SERVICES=("feedback-ingestion" "feedback-analysis" "feedback-notification" "feedback-reporting")

for SERVICE in "${SERVICES[@]}"; do
    echo "  Criando: feedback-platform/$SERVICE"
    aws ecr create-repository \
        --repository-name "feedback-platform/$SERVICE" \
        --region $AWS_REGION \
        --image-scanning-configuration scanOnPush=false \
        2>/dev/null || echo "    (já existe)"
done

echo ""

# 2. Criar fila SQS
echo "Criando fila SQS..."
aws sqs create-queue \
    --queue-name "${PROJECT_NAME}-queue" \
    --region $AWS_REGION \
    --attributes '{
        "VisibilityTimeout": "60",
        "MessageRetentionPeriod": "345600",
        "ReceiveMessageWaitTimeSeconds": "10"
    }' 2>/dev/null || echo "  (já existe)"

SQS_URL=$(aws sqs get-queue-url --queue-name "${PROJECT_NAME}-queue" --region $AWS_REGION --query 'QueueUrl' --output text)
echo "  SQS URL: $SQS_URL"

echo ""

# 3. Criar tópico SNS
echo "Criando tópico SNS..."
SNS_ARN=$(aws sns create-topic \
    --name "${PROJECT_NAME}-notifications" \
    --region $AWS_REGION \
    --query 'TopicArn' \
    --output text 2>/dev/null || aws sns list-topics --region $AWS_REGION --query "Topics[?contains(TopicArn, '${PROJECT_NAME}-notifications')].TopicArn" --output text)
echo "  SNS ARN: $SNS_ARN"

echo ""

# 4. Criar bucket S3
echo "Criando bucket S3..."
S3_BUCKET="${PROJECT_NAME}-reports-$(aws sts get-caller-identity --query Account --output text)"
aws s3api create-bucket \
    --bucket $S3_BUCKET \
    --region $AWS_REGION \
    --create-bucket-configuration LocationConstraint=$AWS_REGION \
    2>/dev/null || echo "  (já existe)"
echo "  S3 Bucket: $S3_BUCKET"

echo ""

# 5. Criar VPC (simplificado)
echo "Criando VPC..."
VPC_ID=$(aws ec2 create-vpc \
    --cidr-block 10.0.0.0/16 \
    --tag-specifications "ResourceType=vpc,Tags=[{Key=Name,Value=${PROJECT_NAME}-vpc}]" \
    --region $AWS_REGION \
    --query 'Vpc.VpcId' \
    --output text 2>/dev/null || aws ec2 describe-vpcs --filters "Name=tag:Name,Values=${PROJECT_NAME}-vpc" --query 'Vpcs[0].VpcId' --output text)
echo "  VPC ID: $VPC_ID"

# Habilitar DNS
aws ec2 modify-vpc-attribute --vpc-id $VPC_ID --enable-dns-hostnames "{\"Value\":true}" 2>/dev/null || true
aws ec2 modify-vpc-attribute --vpc-id $VPC_ID --enable-dns-support "{\"Value\":true}" 2>/dev/null || true

echo ""

# 6. Criar Subnet pública
echo "Criando Subnet..."
SUBNET_ID=$(aws ec2 create-subnet \
    --vpc-id $VPC_ID \
    --cidr-block 10.0.1.0/24 \
    --availability-zone "${AWS_REGION}a" \
    --tag-specifications "ResourceType=subnet,Tags=[{Key=Name,Value=${PROJECT_NAME}-public-subnet}]" \
    --region $AWS_REGION \
    --query 'Subnet.SubnetId' \
    --output text 2>/dev/null || aws ec2 describe-subnets --filters "Name=tag:Name,Values=${PROJECT_NAME}-public-subnet" --query 'Subnets[0].SubnetId' --output text)
echo "  Subnet ID: $SUBNET_ID"

# Habilitar IP público automático
aws ec2 modify-subnet-attribute --subnet-id $SUBNET_ID --map-public-ip-on-launch 2>/dev/null || true

echo ""

# 7. Criar Internet Gateway
echo "Criando Internet Gateway..."
IGW_ID=$(aws ec2 create-internet-gateway \
    --tag-specifications "ResourceType=internet-gateway,Tags=[{Key=Name,Value=${PROJECT_NAME}-igw}]" \
    --region $AWS_REGION \
    --query 'InternetGateway.InternetGatewayId' \
    --output text 2>/dev/null || aws ec2 describe-internet-gateways --filters "Name=tag:Name,Values=${PROJECT_NAME}-igw" --query 'InternetGateways[0].InternetGatewayId' --output text)
echo "  IGW ID: $IGW_ID"

# Attach ao VPC
aws ec2 attach-internet-gateway --internet-gateway-id $IGW_ID --vpc-id $VPC_ID 2>/dev/null || true

echo ""

# 8. Configurar Route Table
echo "Configurando Route Table..."
RTB_ID=$(aws ec2 describe-route-tables --filters "Name=vpc-id,Values=$VPC_ID" --query 'RouteTables[0].RouteTableId' --output text)
aws ec2 create-route --route-table-id $RTB_ID --destination-cidr-block 0.0.0.0/0 --gateway-id $IGW_ID 2>/dev/null || true
echo "  Route Table ID: $RTB_ID"

echo ""

# 9. Criar Security Groups
echo "Criando Security Groups..."

# SG para EC2
EC2_SG_ID=$(aws ec2 create-security-group \
    --group-name "${PROJECT_NAME}-ec2-sg" \
    --description "Security group for EC2" \
    --vpc-id $VPC_ID \
    --region $AWS_REGION \
    --query 'GroupId' \
    --output text 2>/dev/null || aws ec2 describe-security-groups --filters "Name=group-name,Values=${PROJECT_NAME}-ec2-sg" --query 'SecurityGroups[0].GroupId' --output text)
echo "  EC2 SG ID: $EC2_SG_ID"

# Regras do EC2 SG
aws ec2 authorize-security-group-ingress --group-id $EC2_SG_ID --protocol tcp --port 22 --cidr 0.0.0.0/0 2>/dev/null || true
aws ec2 authorize-security-group-ingress --group-id $EC2_SG_ID --protocol tcp --port 80 --cidr 0.0.0.0/0 2>/dev/null || true
aws ec2 authorize-security-group-ingress --group-id $EC2_SG_ID --protocol tcp --port 8080-8084 --cidr 0.0.0.0/0 2>/dev/null || true

# SG para RDS
RDS_SG_ID=$(aws ec2 create-security-group \
    --group-name "${PROJECT_NAME}-rds-sg" \
    --description "Security group for RDS" \
    --vpc-id $VPC_ID \
    --region $AWS_REGION \
    --query 'GroupId' \
    --output text 2>/dev/null || aws ec2 describe-security-groups --filters "Name=group-name,Values=${PROJECT_NAME}-rds-sg" --query 'SecurityGroups[0].GroupId' --output text)
echo "  RDS SG ID: $RDS_SG_ID"

# Regra RDS - permitir acesso do EC2 SG
aws ec2 authorize-security-group-ingress --group-id $RDS_SG_ID --protocol tcp --port 5432 --source-group $EC2_SG_ID 2>/dev/null || true

echo ""
echo "========================================="
echo "   Recursos criados com sucesso!        "
echo "========================================="
echo ""
echo "Resumo:"
echo "  VPC ID: $VPC_ID"
echo "  Subnet ID: $SUBNET_ID"
echo "  EC2 Security Group: $EC2_SG_ID"
echo "  RDS Security Group: $RDS_SG_ID"
echo "  SQS URL: $SQS_URL"
echo "  SNS ARN: $SNS_ARN"
echo "  S3 Bucket: $S3_BUCKET"
echo ""
echo "Próximos passos (via Console AWS):"
echo "  1. Criar instância EC2 t2.micro na subnet criada"
echo "  2. Criar RDS PostgreSQL db.t3.micro"
echo "  3. Alocar Elastic IP e associar à EC2"
echo ""
