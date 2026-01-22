package br.com.postech.feedback.core.config;

import lombok.experimental.UtilityClass;

/**
 * Constantes para recursos AWS compartilhados entre os serviços.
 */
@UtilityClass
public class AwsConfigConstants {

    // Nomes dos recursos AWS
    public static final String QUEUE_INGESTION_ANALYSIS = "feedback-analysis-queue";
    public static final String TOPIC_NOTIFICATION = "feedback-notification-topic";
    public static final String BUCKET_REPORTS = "postech-feedback-reports";

    // Padrão de data para JSON (ISO-8601)
    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
}