package br.com.postech.feedback.core.repository;

import br.com.postech.feedback.core.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {
    // Exemplo: Método para o Relatório Semanal filtrar por data
    // List<Feedback> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}