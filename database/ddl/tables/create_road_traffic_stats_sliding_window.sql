DROP TABLE IF EXISTS road_traffic_stats_sliding_window CASCADE;

CREATE TABLE road_traffic_stats_sliding_window (
                                                   period TIMESTAMP,
                                                   segment_id INT,
                                                   trafficstatus TEXT,
                                                   denomination TEXT,
                                                   avg_speed DOUBLE PRECISION,
                                                   min_speed DOUBLE PRECISION,
                                                   max_speed DOUBLE PRECISION,
                                                   stddev_speed DOUBLE PRECISION,
                                                   avg_travel_time DOUBLE PRECISION,
                                                   avg_reliability DOUBLE PRECISION,
                                                   avg_latency_sec DOUBLE PRECISION,
                                                   count BIGINT
);
