# ================================
# Build stage
# ================================
FROM hseeberger/scala-sbt:eclipse-temurin-11.0.14.1_1.6.2_3.1.1 AS build

WORKDIR /app
COPY build.sbt ./
COPY project ./project
COPY src ./src

RUN sbt clean assembly

# ================================
# Production stage (avec Spark)
# ================================
FROM bitnami/spark:3.5.5

# Définit un HOME absolu pour Ivy
ENV HOME=/opt/spark/app

WORKDIR /opt/spark/app
RUN mkdir -p $HOME/.ivy2

# Copie le fat-jar généré pour Scala 2.12
COPY --from=build /app/target/scala-2.12/*.jar app.jar

# Lance le job Spark avec spark-submit
ENTRYPOINT ["spark-submit","--class","com.goamegah.flowtrack.MainApp","--master","local[*]","--conf","spark.jars.ivy=/opt/spark/app/.ivy2","--conf","spark.hadoop.hadoop.security.authentication=simple","app.jar"]


