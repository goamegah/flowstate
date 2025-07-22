CREATE MATERIALIZED VIEW IF NOT EXISTS top_congested_roads AS
SELECT
    segment_id,
    denomination,
    COUNT(*) AS congestion_count
FROM road_traffic_stats_minute
WHERE avg_speed < 15
GROUP BY segment_id, denomination
ORDER BY congestion_count DESC
LIMIT 10;
