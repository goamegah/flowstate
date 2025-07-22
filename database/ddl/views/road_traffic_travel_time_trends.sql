CREATE MATERIALIZED VIEW IF NOT EXISTS road_traffic_travel_time_trends AS
SELECT
    period,
    segment_id,
    denomination,
    avg_travel_time
FROM road_traffic_stats_minute
ORDER BY period;
