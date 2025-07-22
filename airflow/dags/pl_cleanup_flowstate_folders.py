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
        dag_id='pl_cleanup_flowstate_folders_dag',
        default_args=default_args,
        description='Nettoyage des répertoires de données FlowState',
        schedule_interval=None,  # Ou mets None pour le lancer manuellement
        catchup=False
) as dag:

    # Suppression des fichiers RAW
    clean_raw = BashOperator(
        task_id='clean_raw_dir',
        bash_command='rm -rf /opt/airflow/data/raw/*'
    )

    # Suppression des fichiers TRANSIENT
    clean_transient = BashOperator(
        task_id='clean_transient_dir',
        bash_command='rm -rf /opt/airflow/data/transient/*'
    )

    # Suppression des checkpoints Spark
    clean_checkpoints = BashOperator(
        task_id='clean_spark_checkpoints',
        bash_command='rm -rf /opt/airflow/checkpoint/*'
    )

    clean_raw >> clean_transient >> clean_checkpoints
