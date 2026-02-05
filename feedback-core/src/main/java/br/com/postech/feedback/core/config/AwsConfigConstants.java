package br.com.postech.feedback.core.config;

import lombok.experimental.UtilityClass;

/**
 * Constantes para recursos AWS compartilhados entre os serviços.
 * 
 * IMPORTANTE: Para uso com AWS Lambda, configure as seguintes variáveis de ambiente:
 * - SQS_QUEUE_NAME: Nome da fila SQS (ex: feedback-analysis-queue)
 * - SQS_QUEUE_URL: URL completa da fila SQS
 * - SNS_TOPIC_ARN: ARN completo do tópico SNS
 * - S3_BUCKET_NAME: Nome do bucket S3
 * - AWS_REGION: Região AWS (ex: us-east-2, us-east-1, etc)
 * - SES_FROM_EMAIL: Email remetente (deve estar verificado no SES)
 * - SES_RECIPIENT_EMAIL: Email destinatário
 */
@UtilityClass
public class AwsConfigConstants {

    // Nomes dos recursos AWS - Usar valores de ambiente em produção
    public static final String QUEUE_INGESTION_ANALYSIS = "feedback-analysis-queue";
    public static final String TOPIC_NOTIFICATION = "feedback-notification-topic";
    public static final String BUCKET_REPORTS = "postech-feedback-reports";

    // Padrão de data para JSON (ISO-8601)
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
}