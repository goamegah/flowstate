# Data Warehouse

The data warehouse component of the FlowState project is implemented using PostgreSQL, a powerful open-source relational database management system. The data warehouse stores the processed traffic data from the Spark Structured Streaming transformation pipeline, making it available for analysis and visualization.

## Database Schema

The data warehouse schema is designed to efficiently store and query traffic data at different levels of aggregation. The main tables in the schema include:

### Road Traffic Features Map

This table stores the latest traffic data for each road segment, including geographical information for map visualization:

```sql
CREATE TABLE road_traffic_feats_map (
    timestamp TIMESTAMP,
    segment_id INTEGER,
    denomination VARCHAR(255),
    averagevehiclespeed INTEGER,
    trafficstatus VARCHAR(50),
    traveltime INTEGER,
    traveltimereliability INTEGER,
    coordinates TEXT,
    traffic_speed_category VARCHAR(20),
    PRIMARY KEY (timestamp, segment_id)
);
```

### Road Traffic Stats (Minute)

This table stores traffic statistics aggregated by minute, allowing for detailed temporal analysis:

```sql
CREATE TABLE road_traffic_stats_minute (
    period TIMESTAMP,
    segment_id INTEGER,
    denomination VARCHAR(255),
    trafficstatus VARCHAR(50),
    avg_speed FLOAT,
    avg_travel_time FLOAT,
    avg_reliability FLOAT,
    count INTEGER,
    PRIMARY KEY (period, segment_id, trafficstatus)
);
```

### Road Traffic Stats (Hour)

This table stores traffic statistics aggregated by hour, providing a higher-level view of traffic patterns:

```sql
CREATE TABLE road_traffic_stats_hour (
    period TIMESTAMP,
    segment_id INTEGER,
    denomination VARCHAR(255),
    trafficstatus VARCHAR(50),
    avg_speed FLOAT,
    avg_travel_time FLOAT,
    avg_reliability FLOAT,
    count INTEGER,
    PRIMARY KEY (period, segment_id, trafficstatus)
);
```

### Traffic Status Average Speed

This view provides the average speed for each traffic status, useful for understanding the relationship between traffic status and vehicle speed:

```sql
CREATE VIEW traffic_status_avg_speed AS
SELECT 
    trafficstatus,
    AVG(averagevehiclespeed) AS avg_speed
FROM road_traffic_feats_map
GROUP BY trafficstatus;
```

## Data Loading

The data is loaded into the data warehouse using the PostgresLoader component of the FlowState project. This component:

1. Establishes a connection to the PostgreSQL database
2. Creates the necessary tables if they don't exist
3. Writes the transformed data from the Spark Structured Streaming pipeline to the appropriate tables
4. Handles data updates and insertions

The PostgresLoader is implemented in the `com.goamegah.flowstate.load.PostgresLoader` class and is configured through the `AppConfig` class.

## Database Management

The database schema is managed through the `DBSchemaManager` component, which:

1. Initializes the database schema
2. Creates tables and views
3. Manages schema migrations
4. Ensures data integrity

The DDL (Data Definition Language) scripts for creating the tables and views are stored in the `database/ddl` directory and are executed by the `DBSchemaManager` during application startup.

## Data Access

The data in the data warehouse is accessed by the visualization component of the FlowState project, which uses SQL queries to retrieve the data for display in the Streamlit application. The data access is handled by the `data_loader.py` module, which provides functions for:

1. Establishing a connection to the database
2. Running SQL queries to fetch data
3. Caching data to improve performance

## Configuration

The data warehouse is configured through the `AppConfig` class, which provides various parameters for the PostgreSQL connection:

- `DWH_POSTGRES_HOST`: Hostname for the Postgres database
- `DWH_POSTGRES_PORT`: Port for the Postgres database
- `DWH_POSTGRES_DB`: Database name
- `DWH_POSTGRES_USR`: Username for database authentication
- `DWH_POSTGRES_PWD`: Password for database authentication
- `DWH_POSTGRES_DDL_DIR`: Directory containing DDL scripts

These configuration parameters allow for flexible deployment of the data warehouse in different environments.

[Transformation](transformation.md) <- -> [Visualization](visualization.md)