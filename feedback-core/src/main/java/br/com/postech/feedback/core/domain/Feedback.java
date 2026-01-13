package br.com.postech.feedback.core.domain;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import java.time.LocalDateTime;

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
    private Integer rating;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private StatusFeedback status;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Campos opcionais (mas boas práticas para futuro)
    private String userId;
    private String productId;

    // Construtor Customizado para criar novos Feedbacks
    // Aqui aplicamos a Regra de Negócio: Nota < 5 é Crítico
    public Feedback(String description, Integer rating, String userId, String productId) {
        this.description = description;
        this.rating = rating;
        this.userId = userId;
        this.productId = productId;
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();

        // Regra de Negócio: Calcula automaticamente a urgência
        this.status = calcularStatus(rating);
    }

    private StatusFeedback calcularStatus(Integer rating) {
        if (rating == null) return StatusFeedback.NORMAL;
        // Se nota for 0, 1, 2, 3 ou 4 -> CRITICO
        return (rating < 5) ? StatusFeedback.CRITICAL : StatusFeedback.NORMAL;
    }

    // Método para atualizar antes de salvar (Audit)
    @PreUpdate
    public void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}