# ================================
# Build stage
# ================================
FROM sbtscala/scala-sbt:eclipse-temurin-17.0.15_6_1.11.3_2.13.16 AS build

WORKDIR /app

# options mémoire pour SBT (2 à 4 Go)
ENV SBT_OPTS="-Xmx4G -XX:+UseG1GC"

COPY build.sbt ./
COPY project ./project
COPY src ./src

RUN sbt clean compile assembly

# Vérifie que le JAR a été créé
RUN ls -la /app/target/scala-2.13/ && \
    test -f /app/target/scala-2.13/flowstate-assembly.jar

# ================================
# Stage final - Copie du JAR vers le volume Airflow
# ================================
FROM alpine:latest

WORKDIR /app

# Copie le JAR compilé
COPY --from=build /app/target/scala-2.13/flowstate-assembly.jar ./app.jar

# Script amélioré pour garantir la persistance
RUN echo '#!/bin/sh' > /app/deploy_jar.sh && \
    echo 'set -e' >> /app/deploy_jar.sh && \
    echo 'echo "=== Déploiement du JAR pour Airflow ==="' >> /app/deploy_jar.sh && \
    echo '' >> /app/deploy_jar.sh && \
    echo '# Création du répertoire avec permissions appropriées' >> /app/deploy_jar.sh && \
    echo 'mkdir -p /opt/airflow/jars' >> /app/deploy_jar.sh && \
    echo 'chmod 755 /opt/airflow/jars' >> /app/deploy_jar.sh && \
    echo '' >> /app/deploy_jar.sh && \
    echo '# Copie du JAR avec vérification' >> /app/deploy_jar.sh && \
    echo 'echo "Copie du JAR..."' >> /app/deploy_jar.sh && \
    echo 'cp /app/app.jar /opt/airflow/jars/app.jar' >> /app/deploy_jar.sh && \
    echo '' >> /app/deploy_jar.sh && \
    echo '# Vérification de la copie' >> /app/deploy_jar.sh && \
    echo 'if [ -f "/opt/airflow/jars/app.jar" ]; then' >> /app/deploy_jar.sh && \
    echo '    echo "✓ JAR déployé avec succès : /opt/airflow/jars/app.jar"' >> /app/deploy_jar.sh && \
    echo '    ls -la /opt/airflow/jars/' >> /app/deploy_jar.sh && \
    echo '    echo "Taille du JAR: $(du -h /opt/airflow/jars/app.jar | cut -f1)"' >> /app/deploy_jar.sh && \
    echo '    chmod 644 /opt/airflow/jars/app.jar' >> /app/deploy_jar.sh && \
    echo 'else' >> /app/deploy_jar.sh && \
    echo '    echo "✗ ERREUR: Le JAR n'\''a pas pu être copié !"' >> /app/deploy_jar.sh && \
    echo '    exit 1' >> /app/deploy_jar.sh && \
    echo 'fi' >> /app/deploy_jar.sh && \
    echo '' >> /app/deploy_jar.sh && \
    echo 'echo "=== JAR prêt pour Airflow ==="' >> /app/deploy_jar.sh && \
    echo '' >> /app/deploy_jar.sh && \
    echo '# OPTIONNEL: Garde le conteneur en vie pour maintenir le volume' >> /app/deploy_jar.sh && \
    echo '# echo "Maintien du conteneur en vie pour la durée de vie du volume..."' >> /app/deploy_jar.sh && \
    echo '# tail -f /dev/null' >> /app/deploy_jar.sh && \
    chmod +x /app/deploy_jar.sh

# Le conteneur copie le JAR et se termine (le volume Docker persiste)
CMD ["/app/deploy_jar.sh"]