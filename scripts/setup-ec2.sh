#!/bin/bash
# ===========================================
# Script de Setup para EC2 (Amazon Linux 2023)
# ===========================================
# Execute este script na EC2 após conectar via SSH
# curl -sSL https://raw.githubusercontent.com/seu-repo/setup-ec2.sh | bash
# ou copie e execute localmente
# ===========================================

set -e

echo "========================================="
echo "   Setup EC2 - Feedback Platform        "
echo "========================================="
echo ""

# Atualizar sistema
echo "Atualizando sistema..."
sudo yum update -y

# Instalar Docker
echo "Instalando Docker..."
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -aG docker ec2-user

# Instalar Docker Compose
echo "Instalando Docker Compose..."
DOCKER_COMPOSE_VERSION=$(curl -s https://api.github.com/repos/docker/compose/releases/latest | grep -oP '"tag_name": "\K(.*)(?=")')
sudo curl -L "https://github.com/docker/compose/releases/download/${DOCKER_COMPOSE_VERSION}/docker-compose-$(uname -s)-$(uname -m)" -o /usr/local/bin/docker-compose
sudo chmod +x /usr/local/bin/docker-compose

# Instalar Git
echo "Instalando Git..."
sudo yum install -y git

# Criar diretório da aplicação
echo "Criando diretório da aplicação..."
mkdir -p ~/feedback-platform
cd ~/feedback-platform

# Verificar instalações
echo ""
echo "========================================="
echo "   Verificando instalações              "
echo "========================================="
echo "Docker version: $(docker --version)"
echo "Docker Compose version: $(docker-compose --version)"
echo "Git version: $(git --version)"

echo ""
echo "========================================="
echo "   Setup concluído!                     "
echo "========================================="
echo ""
echo "IMPORTANTE: Faça logout e login novamente para aplicar o grupo docker:"
echo "  exit"
echo ""
echo "Depois, copie os arquivos necessários:"
echo "  1. docker-compose.prod.yml"
echo "  2. .env (baseado no .env.example)"
echo "  3. nginx/nginx.conf"
echo ""
echo "E execute:"
echo "  cd ~/feedback-platform"
echo "  docker-compose -f docker-compose.prod.yml up -d"
echo ""
