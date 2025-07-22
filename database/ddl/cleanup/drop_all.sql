-- DÉSACTIVER LES CONFLITS DE DÉPENDANCES
DROP VIEW IF EXISTS road_traffic_stats_by_road_name CASCADE;
DROP VIEW IF EXISTS traffic_status_summary CASCADE;
DROP VIEW IF EXISTS latest_traffic_status CASCADE;
DROP VIEW IF EXISTS top_slow_segments CASCADE;
DROP VIEW IF EXISTS route_avg_speed_time CASCADE;

-- SUPPRESSION DES TABLES (dans l’ordre inverse de création pour éviter les dépendances)
DROP TABLE IF EXISTS traffic_status_avg_speed;
DROP TABLE IF EXISTS road_traffic_stats_sliding_window;
DROP TABLE IF EXISTS road_traffic_stats_hour;
DROP TABLE IF EXISTS road_traffic_stats_minute;
DROP TABLE IF EXISTS road_traffic_feats_map;
