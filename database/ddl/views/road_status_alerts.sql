CREATE OR REPLACE VIEW road_status_alerts AS
SELECT
    segment_id,
    denomination,
    period,
    trafficstatus,
    LAG(trafficstatus) OVER (PARTITION BY segment_id ORDER BY period) AS previous_status,
    CASE
        WHEN LAG(trafficstatus) OVER (PARTITION BY segment_id ORDER BY period) IS DISTINCT FROM trafficstatus THEN true
        ELSE false
        END AS status_changed
FROM road_traffic_stats_minute
ORDER BY segment_id, period;
