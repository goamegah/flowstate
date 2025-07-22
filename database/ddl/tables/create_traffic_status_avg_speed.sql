DROP TABLE IF EXISTS traffic_status_avg_speed CASCADE;

CREATE TABLE traffic_status_avg_speed (
                                          trafficstatus TEXT,
                                          avg_speed DOUBLE PRECISION,
                                          count BIGINT
);
