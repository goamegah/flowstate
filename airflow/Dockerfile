# airflow/Dockerfile.txt

FROM apache/airflow:2.6.0

# Étape 1 : Installer Java (Spark en a besoin)
USER root
# RUN apt-get update && apt-get install -y curl openjdk-11-jdk && apt-get clean
RUN apt-get update && apt-get install -y curl openjdk-17-jdk && apt-get clean


# Étape 2 : Installer Spark avec gestion d'erreur
ENV SPARK_VERSION=4.0.0
ENV HADOOP_VERSION=3
ENV SPARK_HOME=/opt/spark
RUN mkdir -p /opt && \
    curl -L -o /tmp/spark.tgz https://archive.apache.org/dist/spark/spark-${SPARK_VERSION}/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION}.tgz && \
    tar -xzf /tmp/spark.tgz -C /opt && \
    mv /opt/spark-${SPARK_VERSION}-bin-hadoop${HADOOP_VERSION} ${SPARK_HOME} && \
    rm /tmp/spark.tgz
ENV PATH="${SPARK_HOME}/bin:$PATH"

# Étape 3 : Passer à l'utilisateur airflow AVANT d'installer les packages Python
USER airflow

# Étape 4 : Installer Python requirements
COPY requirements.txt /requirements.txt
RUN pip install --no-cache-dir -r /requirements.txt

# Étape 5 : Copier les DAGs
COPY dags/ /opt/airflow/dags/