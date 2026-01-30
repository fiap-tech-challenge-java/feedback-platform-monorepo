package br.com.postech.feedback.core.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

@Entity
@Table(name = "feedbacks")
@Data // Gera Getters, Setters, toString, equals, hashCode
@NoArgsConstructor // Obrigatório para o JPA
@AllArgsConstructor
public class Feedback {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    @NotNull(message = "Rating é obrigatório")
    @Min(value = 0, message = "Rating deve ser no mínimo 0")
    @Max(value = 10, message = "Rating deve ser no máximo 10")
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFeedback status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Construtor Customizado para criar novos Feedbacks
    // Aqui aplicamos a Regra de Negócio: Rating entre 0-10 e Nota < 5 é Crítico
    public Feedback(String description, Integer rating) {
        validarRating(rating);
        this.description = description;
        this.rating = rating;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // Regra de Negócio: Calcula automaticamente a urgência
        this.status = calcularStatus(rating);
    }

    private void validarRating(Integer rating) {
        if (rating == null || rating < 0 || rating > 10) {
            throw new IllegalArgumentException("Rating deve estar entre 0 e 10");
        }
    }

    private StatusFeedback calcularStatus(Integer rating) {
        // Rating já validado (0-10), calcula criticidade
        // Se nota for 0, 1, 2, 3 ou 4 -> CRITICO
        return (rating < 5) ? StatusFeedback.CRITICAL : StatusFeedback.NORMAL;
    }

    // Método para atualizar antes de salvar (Audit)
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}