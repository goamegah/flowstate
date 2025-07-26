<h1 align="center">End-To-End Near Real-time Road Traffic Monitoring Solution</h1>

FlowState is a near real-time road traffic monitoring solution that leverages Apache Spark, Apache Airflow, and Docker to process and analyze traffic data. The project is designed to handle large volumes of data efficiently, providing insights into traffic patterns and conditions.
This solution is built to be scalable and robust, making it suitable for real-world applications in traffic management and urban planning.

Data is collected from the [Rennes Metropole API](https://data.rennesmetropole.fr/explore/dataset/etat-du-trafic-en-temps-reel/information/), which provides real-time traffic data. The solution processes this data to extract meaningful insights, such as traffic flow and congestion levels, and stores the results in a structured format for further analysis.

Here's reference architecture of the project:
![Reference Architecture](./assets/arch.png)

Check out the [project structure](docs/structure.md) for more details on how the components are organized.


## Table of Contents
- [Technology Stack](#technology-stack)
- [Prerequisites](#prerequisites)
- [Setup](#setup)

### Technology Stack

- **Stream Processing**: Apache Spark 4.0.0
- **Orchestration**: Apache Airflow 2.6.0
- **Database**: PostgreSQL 13
- **UI Framework**: Streamlit
- **Build Tool**: SBT with Assembly plugin
- **Containerization**: Docker & Docker Compose
- **Language**: Scala 2.13.16

### Prerequisites
Before you begin, ensure you have the following software installed:

- **Docker**: [Install Docker](https://docs.docker.com/engine/install/)

### Setup

1. **Clone the repository**:
```bash
git clone git@github.com:goamegah/flowstate.git
cd flowstate
```

2. **Rename the `dotenv.txt` file to `.env`**:
```bash
mv dotenv.txt .env
```

2. **Create 3 folders**:
```bash
mkdir -p shared/data/transient # for intermediate data loading
mkdir -p shared/data/raw # for raw data loading from transient folder
mkdir -p shared/checkpoint # used by Spark for checkpointing
mkdir -p shared/jars # for Spark application JAR file
```

`shared` is a bind volume that is mounted to the docker container to display the raw files in your IDE.

2. **Run the Docker Compose**:
```bash
docker compose up -d
```

3. **Go to airflow web UI**:
```bash
http://localhost:8080
```

You well need to create a connection to the API with the following parameters:
- **Conn Id**: traffic_api
- **Conn Type**: HTTP
- **Host**: https://data.rennesmetropole.fr/

![alt text](assets/airflow_admin_connections.png)

![alt text](assets/airflow_admin_connections_api.png)

This connection is used to check the API availability

After setting up the connection, you can see following 3 DAGs that you can run one after another:
- **pl_load_flowstate_raw_files**

![alt text](assets/pl_load_raw_file.jpeg)
This DAG loads the raw data from the Rennes Metropole API into the raw folder. It is scheduled to run every 1 minutes.

- **pl_run_flowstate_mainapp_dag**

![alt text](assets/pl_run_main_app.jpeg)
This DAG run the main application that processes the raw data and stores the results in the data warehouse. It's not scheduled to run automatically, you can trigger it manually from the Airflow UI.

- **[Optional] pl_clean_up_flowstate_folders_dag**: 

![clean up pipeline](assets/pl_clean_up.jpeg)
DAG that cleans up the data from raw, transient and checkpoint folders.

4. **Check the results in the Streamlit app web UI**:
```bash
http://localhost:8501
```

![alt text](assets/flowtrack_history.jpeg)