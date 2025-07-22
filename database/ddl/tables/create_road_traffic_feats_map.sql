DROP TABLE IF EXISTS road_traffic_feats_map CASCADE;

CREATE TABLE road_traffic_feats_map (
                                        segment_id INT,
                                        denomination TEXT,
                                        lat DOUBLE PRECISION,
                                        lon DOUBLE PRECISION,
                                        coordinates TEXT,
                                        shape_type TEXT,
                                        trafficstatus TEXT,
                                        averagevehiclespeed INT,
                                        traveltime INT,
                                        timestamp TIMESTAMP,
                                        traffic_speed_category TEXT
);
