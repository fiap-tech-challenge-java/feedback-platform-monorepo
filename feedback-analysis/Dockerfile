# Dockerfile para feedback-analysis (AWS Lambda compatível ou local)
FROM eclipse-temurin:21-jre-alpine

WORKDIR /app

# Copiar o JAR gerado
COPY target/*.jar app.jar

# Variáveis de ambiente padrão
ENV JAVA_OPTS="-Xmx512m -Xms256m"
ENV SPRING_PROFILES_ACTIVE=prod

# Porta da aplicação
EXPOSE 8082

# Health check
HEALTHCHECK --interval=30s --timeout=10s --start-period=60s --retries=3 \
    CMD wget --quiet --tries=1 --spider http://localhost:8082/actuator/health || exit 1

# Executar a aplicação
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
