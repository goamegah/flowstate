FROM apache/airflow:2.6.0

# Passer root pour installer des dépendances système
USER root

# Installer Java et outils nécessaires
RUN apt-get update && apt-get install -y openjdk-11-jdk curl && \
    apt-get clean

# Variables d'environnement pour Spark
ENV SPARK_VERSION=3.4.1
ENV HADOOP_VERSION=3
ENV SPARK_HOME=/opt/spark
ENV PATH=$PATH:$SPARK_HOME/bin

# Télécharger et installer Spark
RUN curl -fSL https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz \
    | tar -xz -C /opt && \
    mv /opt/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION} /opt/spark

# Revenir à l'utilisateur airflow
USER airflow

# Copier les DAGs dans l'image
COPY dags /opt/airflow/dags/

# Installer les dépendances Python si besoin
COPY requirements.txt /requirements.txt
RUN pip install --no-cache-dir -r /requirements.txt
