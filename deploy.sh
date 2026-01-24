#!/bin/bash
# ===========================================
# Script de Deploy Lambda - AWS SAM (Bash)
# ===========================================
# Este script automatiza o build e deploy das Lambdas
# Requer: AWS SAM CLI, Maven, Java 21
# Execute: chmod +x deploy.sh && ./deploy.sh dev
# ===========================================

set -e

# Cores
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
CYAN='\033[0;36m'
MAGENTA='\033[0;35m'
NC='\033[0m'

# Funções de log
log_success() { echo -e "${GREEN}✓ $1${NC}"; }
log_info() { echo -e "${CYAN}ℹ $1${NC}"; }
log_warning() { echo -e "${YELLOW}⚠ $1${NC}"; }
log_error() { echo -e "${RED}✗ $1${NC}"; }

# Parâmetros
ENVIRONMENT=${1:-"dev"}
SKIP_BUILD=${2:-"false"}
DATABASE_URL=${3:-""}
DATABASE_USER=${4:-"admin"}
DATABASE_PASSWORD=${5:-""}

# Banner
echo ""
echo -e "${MAGENTA}=========================================${NC}"
echo -e "${MAGENTA}   Feedback Platform - Lambda Deploy    ${NC}"
echo -e "${MAGENTA}   Ambiente: $ENVIRONMENT               ${NC}"
echo -e "${MAGENTA}=========================================${NC}"
echo ""

# Verificar pré-requisitos
log_info "Verificando pré-requisitos..."

if ! command -v sam &> /dev/null; then
    log_error "AWS SAM CLI não encontrado. Instale: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html"
    exit 1
fi
log_success "SAM CLI: $(sam --version)"

if ! command -v mvn &> /dev/null; then
    log_error "Maven não encontrado."
    exit 1
fi
log_success "Maven: $(mvn --version | head -1)"

if ! command -v java &> /dev/null; then
    log_error "Java não encontrado."
    exit 1
fi
log_success "Java: $(java --version | head -1)"

echo ""

# Diretório raiz
ROOT_DIR="$(cd "$(dirname "$0")" && pwd)"
log_info "Diretório raiz: $ROOT_DIR"
echo ""

# ==========================================
# ETAPA 1: Build Maven
# ==========================================
if [ "$SKIP_BUILD" != "true" ]; then
    echo -e "${YELLOW}=========================================${NC}"
    log_info "ETAPA 1: Build Maven"
    echo -e "${YELLOW}=========================================${NC}"
    echo ""

    # Build do módulo core
    log_info "Buildando feedback-core..."
    cd "$ROOT_DIR/feedback-core"
    mvn clean install -DskipTests -q
    log_success "feedback-core buildado!"

    # Build dos módulos Lambda
    LAMBDAS=("feedback-ingestion" "feedback-analysis" "feedback-notification" "feedback-reporting")
    
    for LAMBDA in "${LAMBDAS[@]}"; do
        log_info "Buildando $LAMBDA..."
        cd "$ROOT_DIR/$LAMBDA"
        mvn clean package -DskipTests -q
        
        if ls target/*-aws.jar 1> /dev/null 2>&1; then
            log_success "$LAMBDA buildado: $(ls target/*-aws.jar | xargs basename)"
        else
            log_warning "$LAMBDA: JAR -aws.jar não encontrado"
        fi
    done
    
    cd "$ROOT_DIR"
    echo ""
fi

# ==========================================
# ETAPA 2: SAM Build
# ==========================================
echo -e "${YELLOW}=========================================${NC}"
log_info "ETAPA 2: SAM Build"
echo -e "${YELLOW}=========================================${NC}"
echo ""

cd "$ROOT_DIR"
sam build
log_success "SAM build concluído!"
echo ""

# ==========================================
# ETAPA 3: SAM Deploy
# ==========================================
echo -e "${YELLOW}=========================================${NC}"
log_info "ETAPA 3: SAM Deploy"
echo -e "${YELLOW}=========================================${NC}"
echo ""

DEPLOY_CMD="sam deploy --config-env $ENVIRONMENT"

# Adicionar parâmetros de banco se fornecidos
if [ -n "$DATABASE_URL" ] && [ -n "$DATABASE_PASSWORD" ]; then
    DEPLOY_CMD="$DEPLOY_CMD --parameter-overrides Environment=\"$ENVIRONMENT\" DatabaseUrl=\"$DATABASE_URL\" DatabaseUsername=\"$DATABASE_USER\" DatabasePassword=\"$DATABASE_PASSWORD\""
fi

log_info "Executando: $DEPLOY_CMD"
echo ""

eval $DEPLOY_CMD

echo ""
echo -e "${GREEN}=========================================${NC}"
log_success "Deploy concluído com sucesso!"
echo -e "${GREEN}=========================================${NC}"
echo ""

# Mostrar outputs
log_info "Obtendo outputs do stack..."
STACK_NAME="feedback-platform-$ENVIRONMENT"
aws cloudformation describe-stacks --stack-name $STACK_NAME --query 'Stacks[0].Outputs' --output table

echo ""
log_info "Para testar a API:"
echo "  curl -X POST <API_URL>/feedbacks -H 'Content-Type: application/json' -d '{...}'"
echo ""
