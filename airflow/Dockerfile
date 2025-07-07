FROM apache/airflow:2.6.0

COPY dags/ /opt/airflow/dags/
COPY requirements.txt /requirements.txt
RUN pip install --no-cache-dir -r /requirements.txt