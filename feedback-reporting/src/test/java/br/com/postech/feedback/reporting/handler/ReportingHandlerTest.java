package br.com.postech.feedback.reporting.handler;

import br.com.postech.feedback.reporting.dto.ReportMetrics;
import br.com.postech.feedback.reporting.service.DatabaseQueryService;
import br.com.postech.feedback.reporting.service.ReportGeneratorService;
import br.com.postech.feedback.reporting.service.S3UploadService;
import br.com.postech.feedback.reporting.service.SnsPublishService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReportingHandlerTest {

    @Mock
    private DatabaseQueryService databaseQueryService;

    @Mock
    private ReportGeneratorService reportGeneratorService;

    @Mock
    private S3UploadService s3UploadService;

    @Mock
    private SnsPublishService snsPublishService;

    @InjectMocks
    private ReportingHandler reportingHandler;

    private ReportMetrics mockMetrics;

    @BeforeEach
    void setUp() {
        mockMetrics = ReportMetrics.builder()
                .totalFeedbacks(19L)
                .averageScore(7.5)
                .feedbacksByDay(new HashMap<>())
                .feedbacksByUrgency(new HashMap<>())
                .feedbacks(new ArrayList<>())
                .build();
    }

    @Test
    @DisplayName("Deve executar o fluxo completo de geração de relatório semanal com sucesso")
    void shouldExecuteCompleteWeeklyReportGenerationFlow() {
        // Arrange
        when(databaseQueryService.fetchMetrics()).thenReturn(mockMetrics);
        when(reportGeneratorService.generateReportAsBytes(any(ReportMetrics.class), any(LocalDateTime.class)))
                .thenReturn("CSV content".getBytes());
        when(reportGeneratorService.generateS3Key(any(LocalDateTime.class)))
                .thenReturn("reports/2026/01/relatorio-semanal-2026-01-15.csv");
        when(reportGeneratorService.getContentType()).thenReturn("text/csv; charset=UTF-8");
        when(s3UploadService.uploadReport(any(byte[].class), anyString(), anyString()))
                .thenReturn("https://bucket.s3.amazonaws.com/reports/2026/01/relatorio-semanal-2026-01-15.csv");

        Map<String, Object> event = new HashMap<>();
        event.put("source", "aws.events");

        // Act
        Function<Map<String, Object>, Map<String, Object>> function = reportingHandler.generateReport();
        Map<String, Object> result = function.apply(event);

        // Assert
        assertThat(result.get("statusCode")).isEqualTo(200);
        assertThat(result.get("message")).isEqualTo("Weekly report generated successfully");
        assertThat(result.get("reportUrl")).isNotNull();
        assertThat(result.get("totalFeedbacks")).isEqualTo(19L);
        assertThat(result.get("averageScore")).isEqualTo(7.5);

        // Verify all services were called in correct order
        verify(databaseQueryService, times(1)).fetchMetrics();
        verify(reportGeneratorService, times(1)).generateReportAsBytes(any(), any());
        verify(s3UploadService, times(1)).uploadReport(any(byte[].class), anyString(), anyString());
        verify(snsPublishService, times(1)).publishReportReadyEvent(anyString(), anyString(), any(), anyLong(), anyDouble());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o banco de dados falhar")
    void shouldThrowExceptionWhenDatabaseFails() {
        // Arrange
        when(databaseQueryService.fetchMetrics())
                .thenThrow(new RuntimeException("Database connection failed"));

        Map<String, Object> event = new HashMap<>();

        // Act & Assert
        Function<Map<String, Object>, Map<String, Object>> function = reportingHandler.generateReport();
        
        assertThatThrownBy(() -> function.apply(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Report generation failed");

        // Verify S3 and SNS were not called
        verify(s3UploadService, never()).uploadReport(any(byte[].class), anyString(), anyString());
        verify(snsPublishService, never()).publishReportReadyEvent(anyString(), anyString(), any(), anyLong(), anyDouble());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o upload S3 falhar")
    void shouldThrowExceptionWhenS3UploadFails() {
        // Arrange
        when(databaseQueryService.fetchMetrics()).thenReturn(mockMetrics);
        when(reportGeneratorService.generateReportAsBytes(any(), any())).thenReturn("CSV".getBytes());
        when(reportGeneratorService.generateS3Key(any())).thenReturn("key");
        when(reportGeneratorService.getContentType()).thenReturn("text/csv; charset=UTF-8");
        when(s3UploadService.uploadReport(any(byte[].class), anyString(), anyString()))
                .thenThrow(new RuntimeException("S3 upload failed"));

        Map<String, Object> event = new HashMap<>();

        // Act & Assert
        Function<Map<String, Object>, Map<String, Object>> function = reportingHandler.generateReport();
        
        assertThatThrownBy(() -> function.apply(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Report generation failed");

        // Verify SNS was not called
        verify(snsPublishService, never()).publishReportReadyEvent(anyString(), anyString(), any(), anyLong(), anyDouble());
    }

    @Test
    @DisplayName("Deve lançar exceção quando a publicação no SNS falhar")
    void shouldThrowExceptionWhenSnsPublishFails() {
        // Arrange
        when(databaseQueryService.fetchMetrics()).thenReturn(mockMetrics);
        when(reportGeneratorService.generateReportAsBytes(any(), any())).thenReturn("CSV".getBytes());
        when(reportGeneratorService.generateS3Key(any())).thenReturn("key");
        when(reportGeneratorService.getContentType()).thenReturn("text/csv; charset=UTF-8");
        when(s3UploadService.uploadReport(any(byte[].class), anyString(), anyString()))
                .thenReturn("https://bucket.s3.amazonaws.com/key");
        doThrow(new RuntimeException("SNS publish failed"))
                .when(snsPublishService).publishReportReadyEvent(anyString(), anyString(), any(), anyLong(), anyDouble());

        Map<String, Object> event = new HashMap<>();

        // Act & Assert
        Function<Map<String, Object>, Map<String, Object>> function = reportingHandler.generateReport();
        
        assertThatThrownBy(() -> function.apply(event))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Report generation failed");
    }
}
