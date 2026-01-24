# ===========================================
# Script de Deploy Lambda - AWS SAM (PowerShell)
# ===========================================
# Este script automatiza o build e deploy das Lambdas
# Requer: AWS SAM CLI, Maven, Java 21
# Execute: .\deploy.ps1 -Environment dev
# ===========================================

param(
    [string]$Environment = "dev",
    [switch]$SkipBuild = $false,
    [switch]$Guided = $false,
    [string]$DatabaseUrl = "",
    [string]$DatabaseUser = "admin",
    [string]$DatabasePassword = ""
)

# Cores para output
function Write-Success { param($msg) Write-Host "✓ $msg" -ForegroundColor Green }
function Write-Info { param($msg) Write-Host "ℹ $msg" -ForegroundColor Cyan }
function Write-Warning { param($msg) Write-Host "⚠ $msg" -ForegroundColor Yellow }
function Write-Error { param($msg) Write-Host "✗ $msg" -ForegroundColor Red }

# Banner
Write-Host ""
Write-Host "=========================================" -ForegroundColor Magenta
Write-Host "   Feedback Platform - Lambda Deploy    " -ForegroundColor Magenta
Write-Host "   Ambiente: $Environment               " -ForegroundColor Magenta
Write-Host "=========================================" -ForegroundColor Magenta
Write-Host ""

# Verificar pré-requisitos
Write-Info "Verificando pré-requisitos..."

$samVersion = sam --version 2>$null
if (-not $samVersion) {
    Write-Error "AWS SAM CLI não encontrado. Instale: https://docs.aws.amazon.com/serverless-application-model/latest/developerguide/install-sam-cli.html"
    exit 1
}
Write-Success "SAM CLI: $samVersion"

$mavenVersion = mvn --version 2>$null | Select-Object -First 1
if (-not $mavenVersion) {
    Write-Error "Maven não encontrado. Instale o Maven."
    exit 1
}
Write-Success "Maven: $mavenVersion"

$javaVersion = java --version 2>$null | Select-Object -First 1
if (-not $javaVersion) {
    Write-Error "Java não encontrado. Instale o Java 21."
    exit 1
}
Write-Success "Java: $javaVersion"

Write-Host ""

# Diretório raiz
$rootDir = $PSScriptRoot
if ([string]::IsNullOrEmpty($rootDir)) {
    $rootDir = Get-Location
}

Write-Info "Diretório raiz: $rootDir"
Write-Host ""

# ==========================================
# ETAPA 1: Build Maven
# ==========================================
if (-not $SkipBuild) {
    Write-Host "=========================================" -ForegroundColor Yellow
    Write-Info "ETAPA 1: Build Maven"
    Write-Host "=========================================" -ForegroundColor Yellow
    Write-Host ""

    # Build do módulo core primeiro
    Write-Info "Buildando feedback-core..."
    Set-Location "$rootDir\feedback-core"
    mvn clean install -DskipTests
    if ($LASTEXITCODE -ne 0) {
        Write-Error "Falha no build do feedback-core!"
        Set-Location $rootDir
        exit 1
    }
    Write-Success "feedback-core buildado!"

    # Build dos módulos Lambda
    $lambdas = @("feedback-ingestion", "feedback-analysis", "feedback-notification", "feedback-reporting")
    
    foreach ($lambda in $lambdas) {
        Write-Info "Buildando $lambda..."
        Set-Location "$rootDir\$lambda"
        mvn clean package -DskipTests
        if ($LASTEXITCODE -ne 0) {
            Write-Error "Falha no build do $lambda!"
            Set-Location $rootDir
            exit 1
        }
        
        # Verificar se o JAR aws foi gerado
        $awsJar = Get-ChildItem -Path "target" -Filter "*-aws.jar" -ErrorAction SilentlyContinue
        if ($awsJar) {
            Write-Success "$lambda buildado: $($awsJar.Name)"
        } else {
            Write-Warning "$lambda: JAR -aws.jar não encontrado, usando JAR padrão"
        }
    }
    
    Set-Location $rootDir
    Write-Host ""
}

# ==========================================
# ETAPA 2: SAM Build
# ==========================================
Write-Host "=========================================" -ForegroundColor Yellow
Write-Info "ETAPA 2: SAM Build"
Write-Host "=========================================" -ForegroundColor Yellow
Write-Host ""

Set-Location $rootDir
sam build
if ($LASTEXITCODE -ne 0) {
    Write-Error "Falha no SAM build!"
    exit 1
}
Write-Success "SAM build concluído!"
Write-Host ""

# ==========================================
# ETAPA 3: SAM Deploy
# ==========================================
Write-Host "=========================================" -ForegroundColor Yellow
Write-Info "ETAPA 3: SAM Deploy"
Write-Host "=========================================" -ForegroundColor Yellow
Write-Host ""

# Construir parâmetros
$deployParams = @()

if ($Guided) {
    $deployParams += "--guided"
} else {
    $deployParams += "--config-env"
    $deployParams += $Environment
    
    # Adicionar parâmetros de banco se fornecidos
    if ($DatabaseUrl -and $DatabasePassword) {
        $paramOverrides = "Environment=`"$Environment`" DatabaseUrl=`"$DatabaseUrl`" DatabaseUsername=`"$DatabaseUser`" DatabasePassword=`"$DatabasePassword`""
        $deployParams += "--parameter-overrides"
        $deployParams += $paramOverrides
    }
}

Write-Info "Executando: sam deploy $($deployParams -join ' ')"
Write-Host ""

sam deploy @deployParams

if ($LASTEXITCODE -ne 0) {
    Write-Error "Falha no SAM deploy!"
    exit 1
}

Write-Host ""
Write-Host "=========================================" -ForegroundColor Green
Write-Success "Deploy concluído com sucesso!"
Write-Host "=========================================" -ForegroundColor Green
Write-Host ""

# Mostrar outputs
Write-Info "Obtendo outputs do stack..."
$stackName = "feedback-platform-$Environment"
aws cloudformation describe-stacks --stack-name $stackName --query 'Stacks[0].Outputs' --output table

Write-Host ""
Write-Info "Para testar a API:"
Write-Host "  curl -X POST <API_URL>/feedbacks -H 'Content-Type: application/json' -d '{...}'"
Write-Host ""
