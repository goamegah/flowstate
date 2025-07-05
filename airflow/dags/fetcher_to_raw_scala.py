from airflow import DAG
from airflow.operators.bash import BashOperator
from airflow.providers.http.sensors.http import HttpSensor
from datetime import datetime, timedelta

default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': datetime(2025, 3, 9),
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 1,
    'retry_delay': timedelta(minutes=2),
}

with DAG(
    dag_id='scala_spark_traffic_ingestion',
    default_args=default_args,
    description="Ingestion trafic via Spark/Scala",
    schedule_interval='@hourly',  # adapte à ce que tu veux
    catchup=False
) as dag:

    # 1. Vérifie si l’API est accessible
    check_api = HttpSensor(
        task_id="check_api_availability",
        http_conn_id="traffic_api",  # Doit exister dans Airflow UI > Admin > Connections
        endpoint="api/explore/v2.1/catalog/datasets/etat-du-trafic-en-temps-reel/records?limit=1",
        poke_interval=30,
        timeout=60,
        mode="poke"
    )

    # 2. Lancer le job Spark/Scala
    run_spark_job = BashOperator(
        task_id='run_spark_ingestion',
        bash_command='spark-submit --class com.goamegah.flowtrack.extract.FetchAndStoreTrafficData /opt/airflow/jars/realtime-traffic-monitor-assembly-0.1.0-SNAPSHOT.jar'
    )

    check_api >> run_spark_job
