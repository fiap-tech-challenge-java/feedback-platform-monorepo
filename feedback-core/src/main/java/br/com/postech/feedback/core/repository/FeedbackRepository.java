package br.com.postech.feedback.core.repository;

import br.com.postech.feedback.core.domain.Feedback;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface FeedbackRepository extends JpaRepository<Feedback, Long> {

    @Query("SELECT COUNT(f) FROM Feedback f")
    Long countTotalFeedbacks();

    @Query("SELECT COUNT(f) FROM Feedback f WHERE f.status = 'CRITICAL'")
    Long countCriticalFeedbacks();

    @Query("SELECT COALESCE(AVG(f.rating), 0.0) FROM Feedback f")
    Double calculateAverageScore();

    @Query("SELECT f FROM Feedback f ORDER BY f.createdAt DESC")
    List<Feedback> findAllFeedbacksForReport();

    @Query("SELECT f FROM Feedback f WHERE f.createdAt >= :startDate ORDER BY f.createdAt DESC")
    List<Feedback> findFeedbacksSince(@Param("startDate") LocalDateTime startDate);
}