package br.com.postech.feedback.core;

/**
 * Módulo core do sistema de feedbacks.
 *
 * Este é um módulo de biblioteca compartilhado entre os serviços.
 * Contém: domain, repository, DTOs, configurações e utilitários.
 *
 * NÃO é uma aplicação standalone - não deve ser executado diretamente.
 */
public final class FeedbackCoreModule {

    private FeedbackCoreModule() {
        // Classe utilitária - não instanciar
    }

    public static final String BASE_PACKAGE = "br.com.postech.feedback.core";
}
