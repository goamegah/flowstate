CREATE MATERIALIZED VIEW IF NOT EXISTS road_traffic_status_latest AS
SELECT DISTINCT ON (segment_id)
    segment_id,
    denomination,
    trafficstatus,
    averagevehiclespeed,
    traveltime,
    timestamp
FROM road_traffic_feats_map
ORDER BY segment_id, timestamp DESC;
