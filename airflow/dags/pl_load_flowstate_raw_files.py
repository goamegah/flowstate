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
        dag_id='pl_load_flowstate_raw_files',
        default_args=default_args,
        description="Ingestion trafic via Spark/Scala",
        schedule_interval='*/1 * * * *',  # adapte à ce que tu veux
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

source_to_transient = BashOperator(
    task_id='source_to_transient',
    bash_command="""
    echo "=== DEBUG FLOWSTATE JAR ==="
    
    echo "1. Vérification du JAR:"
    ls -la /opt/airflow/jars/app.jar
    
    echo "2. Taille du JAR:"
    du -h /opt/airflow/jars/app.jar
    
    echo "3. Classes flowstate dans le JAR:"
    jar tf /opt/airflow/jars/app.jar | grep -i flowstate
    
    echo "4. Toutes les classes principales:"
    jar tf /opt/airflow/jars/app.jar | grep "\.class$" | grep -E "(extract|flowstate)" | head -20
    
    echo "5. Structure des packages:"
    jar tf /opt/airflow/jars/app.jar | grep "com/goamegah" | head -10
    
    echo "6. Test spark-submit:"
    spark-submit \
        --class com.goamegah.flowstate.elt.SourceToTransient \
        --master local[*] \
        --verbose \
        /opt/airflow/jars/app.jar
    """,
    dag=dag,
)

# 3. Envoie les données vers le sink
transient_to_raw = BashOperator(
    task_id='transient_to_raw',
    bash_command="""
    echo "=== Envoi des données vers le sink ==="
    spark-submit \
        --class com.goamegah.flowstate.elt.TransientToRaw \
        --master local[*] \
        --verbose \
        /opt/airflow/jars/app.jar
    """,
    dag=dag,
)


check_api >> source_to_transient >> transient_to_raw
