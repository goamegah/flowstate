<h1 align="center">FlowTrack: Real-Time Road Traffic Monitoring Solution </h1>

FlowTrack is a real-time road traffic monitoring platform that ingests, processes, and visualizes urban traffic data. It leverages modern streaming technologies, scalable data pipelines, and interactive dashboards to provide actionable insights for mobility and congestion management. 

Below the Reference Architecture:

![Reference Architecture](./assets/test-e2e-serverless-aws.drawio.png)


## Developer Setup


1. Clone the Repository

```shell
git clone git@github.com:goamegah/flowtrack.git
cd flowtrack
```

2. Set Up Docker Ensure Docker is installed and running.[ Docker installation guide](https://docs.docker.com/engine/install/)


3. Configure spark application

```shell
cd app/src/main/resources
```
Edit `application.conf` to set your AWS S3 bucket and other configurations.



4. Configure Environment Variables

```shell
cp dotenv.txt .env
```
Edit .env with your AWS and database credentials.


5. un Docker Containers

```shell
docker-compose up -d
```
6. Access the Dashboard

* Streamlit UI: http://localhost:8501
* Airflow: http://localhost:8080