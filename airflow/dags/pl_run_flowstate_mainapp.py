from airflow import DAG
from airflow.operators.bash import BashOperator
from datetime import datetime, timedelta

default_args = {
    'owner': 'airflow',
    'depends_on_past': False,
    'start_date': datetime(2025, 3, 9),
    'email_on_failure': False,
    'email_on_retry': False,
    'retries': 0,
    'retry_delay': timedelta(minutes=1),
}

with DAG(
        dag_id='pl_run_flowstate_mainapp_dag',
        default_args=default_args,
        description='Start the MainApp Scala Spark Streaming job',
        schedule_interval=None,  # manual trigger recommended
        catchup=False
) as dag:

    run_streaming_job = BashOperator(
        task_id='launch_mainapp_streaming',
        bash_command="""
        echo "=== Starting Spark streaming ==="
        spark-submit \
            --class com.goamegah.flowstate.MainApp \
            --master local[*] \
            --verbose \
            /opt/airflow/jars/app.jar
        """,
    )
