package br.com.postech.feedback.core.config;

import lombok.experimental.UtilityClass;

@UtilityClass
public class AwsConfigConstants {

    public static final String QUEUE_INGESTION_ANALYSIS = "feedback-analysis-queue";
    public static final String TOPIC_NOTIFICATION = "feedback-notification-topic";
    public static final String BUCKET_REPORTS = "postech-feedback-reports";

    public static final String DATE_PATTERN = "yyyy-MM-dd'T'HH:mm:ss";
}