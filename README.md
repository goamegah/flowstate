<h1 align="center">End-To-End Near Real-time Road Traffic Monitoring Solution</h1>

FlowState is a near real-time road traffic monitoring solution that leverages Apache Spark, Apache Airflow, and Docker to process and analyze traffic data. The project is designed to handle large volumes of data efficiently, providing insights into traffic patterns and conditions.
This solution is built to be scalable and robust, making it suitable for real-world applications in traffic management and urban planning.

Here's reference architecture of the project:
![Reference Architecture](./assets/arch.png)

## Table of Contents

- [Getting Started](#getting-started)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Usage](#usage)
- [Project Structure](#project-structure)
- [Building and Running](#building-and-running)
- [Testing](#testing)
- [Contributing](#contributing)
- [License](#license)

## Getting Started

These instructions will help you get a copy of the project up and running on your local machine for development and testing purposes.

### Prerequisites

- **Java**: JDK 17
- **Scala**: 2.13.16
- **sbt**: 1.11.3

### Developer Setup

1. Clone the repository:
```bash
git clone git@github.com:goamegah/flowtrack.git
cd flowtrack
```

2. Install dependencies:
```bash
docker compose up -d
```

3. Go to airflow web UI:
```bash
http://localhost:8080
```

You well need to create a connection to the API with the following parameters:
- **Conn Id**: traffic_api
- **Conn Type**: HTTP
- **Host**: https://data.rennesmetropole.fr/

This connection is used to check the API availability

After setting up the connection, you can see following 3 DAGs:
- **pl_load_flowstate_raw_files**: DAG that performs the ETL process.
![alt text](assets/pl_load_raw_file.jpeg)
- **pl_run_flowstate_mainapp_dag**: DAG that runs the main application logic.
![alt text](assets/pl_run_main_app.jpeg)
- **pl_clean_up_flowstate_folders_dag**: DAG that cleans up the data from raw, transient and checkpoint folders.
![clean up pipeline](assets/pl_clean_up.jpeg)