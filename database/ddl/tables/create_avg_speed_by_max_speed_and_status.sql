CREATE TABLE IF NOT EXISTS avg_speed_by_max_speed_and_status(
    max_speed INTEGER,
    traffic_status CHAR(20),
    avg_speed DOUBLE PRECISION,
    record_count INTEGER,
    min_avg_speed INTEGER,
    max_avg_speed INTEGER
);