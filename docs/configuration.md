# Application Configuration
The configuration of the FlowState project is managed through `application.conf` and `local.conf` files, which allow for easy customization and management of various parameters. These files are crucial for setting up the environment, defining data sources, and configuring the Spark application.

## Configuration Structure

The configuration is managed by the `AppConfig.scala` class, which loads the configuration from the resources directory. The configuration is organized into several sections:

### API Configuration
```
api {
  API_ENDPOINT = "..."
  API_URL = "..."
}
```
- `API_ENDPOINT`: The endpoint for the Rennes Metropole API
- `API_URL`: The base URL for the API

### AWS Configuration
```
aws {
  iam {
    IAM_ACCESS_KEY_ID = "..."
    IAM_SECRET_ACCESS_KEY = "..."
  }
  REGION = "..."
  s3 {
    S3_RAW_BUCKET_URI = "..."
  }
}
```
- `IAM_ACCESS_KEY_ID`: AWS access key ID for authentication
- `IAM_SECRET_ACCESS_KEY`: AWS secret access key for authentication
- `REGION`: AWS region
- `S3_RAW_BUCKET_URI`: URI for the S3 bucket where raw data is stored

### Postgres Configuration
```
container.postgres {
  DWH_POSTGRES_HOST = "dwh-postgres-db"
  DWH_POSTGRES_PORT = 5432
  DWH_POSTGRES_DB = "dwh_postgres_db"
  DWH_POSTGRES_USR = "dwh_postgres_user"
  DWH_POSTGRES_PWD = "dwh_postgres_password"
  DWH_POSTGRES_DDL_DIR = "../database/ddl"
}
```
- `DWH_POSTGRES_HOST`: Hostname for the Postgres database
- `DWH_POSTGRES_PORT`: Port for the Postgres database
- `DWH_POSTGRES_DB`: Database name
- `DWH_POSTGRES_USR`: Username for database authentication
- `DWH_POSTGRES_PWD`: Password for database authentication
- `DWH_POSTGRES_DDL_DIR`: Directory containing DDL scripts

### Airflow Configuration
```
container.airflow {
  AIRFLOW_DATA_DIR = "/opt/airflow/data"
  AIRFLOW_RAW_DIR = "/opt/airflow/data/raw"
  AIRFLOW_TRANSIENT_DIR = "/opt/airflow/data/transient"
}
```
- `AIRFLOW_DATA_DIR`: Directory for Airflow data
- `AIRFLOW_RAW_DIR`: Directory for raw data
- `AIRFLOW_TRANSIENT_DIR`: Directory for transient data

### Spark Configuration
```
container.spark {
  SPARK_CHECKPOINT_DIR = "output/checkpoint"
  SPARK_RAW_DIR = "output/data/raw"
  SPARK_SINK_DIR = "output/data/sink"
  SPARK_DATA_DIR = "output/data"
  SPARK_APP_NAME = "RennesMetropoleTrafficData"
  SPARK_MASTER = "local[*]"
  SPARK_SLIDING_WINDOW_DURATION = "5 minutes"
  SPARK_SLIDING_WINDOW_SLIDE = "1 minute"
  SPARK_TRIGGER_INTERVAL = "60 seconds"
  SPARK_ENABLE_SLIDING_WINDOW = false
  SPARK_ENABLE_HOURLY_AGGREGATION = true
  SPARK_ENABLE_MINUTE_AGGREGATION = true
  SPARK_WATERMARK = "1 minutes"
}
```
- `SPARK_CHECKPOINT_DIR`: Directory for Spark checkpoints
- `SPARK_RAW_DIR`: Directory for raw data
- `SPARK_SINK_DIR`: Directory for sink data
- `SPARK_DATA_DIR`: Directory for data
- `SPARK_APP_NAME`: Name of the Spark application
- `SPARK_MASTER`: Spark master URL
- `SPARK_SLIDING_WINDOW_DURATION`: Duration of the sliding window
- `SPARK_SLIDING_WINDOW_SLIDE`: Slide interval for the sliding window
- `SPARK_TRIGGER_INTERVAL`: Trigger interval for the Spark streaming job
- `SPARK_ENABLE_SLIDING_WINDOW`: Whether to enable sliding window aggregation
- `SPARK_ENABLE_HOURLY_AGGREGATION`: Whether to enable hourly aggregation
- `SPARK_ENABLE_MINUTE_AGGREGATION`: Whether to enable minute aggregation
- `SPARK_WATERMARK`: Watermark duration for late data handling

## Configuration Loading

The configuration is loaded using the following logic:
1. First, it attempts to load `local.conf` from the resources directory
2. If `local.conf` is found, it is used with fallback to `application.conf`
3. If `local.conf` is not found, only `application.conf` is used
4. For certain configuration parameters, default values are provided if the configuration is not found

This approach allows for environment-specific configuration overrides while maintaining sensible defaults.

[Project Structure](structure.md) <- -> [Data Ingestion](ingestion.md)