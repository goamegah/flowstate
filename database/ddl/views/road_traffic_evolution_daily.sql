CREATE MATERIALIZED VIEW IF NOT EXISTS road_traffic_evolution_daily AS
SELECT
    DATE_TRUNC('day', period) AS period_day,
    segment_id,
    denomination,
    trafficstatus,
    AVG(avg_speed) AS avg_speed,
    AVG(avg_travel_time) AS avg_travel_time,
    AVG(avg_reliability) AS avg_reliability,
    SUM(count) AS total_records
FROM road_traffic_stats_hour
GROUP BY period_day, segment_id, denomination, trafficstatus;
