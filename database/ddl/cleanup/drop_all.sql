-- Supprimer les vues si elles existent
DROP VIEW IF EXISTS traffic_evolution_by_road_name_hour CASCADE;
DROP VIEW IF EXISTS traffic_evolution_by_road_name CASCADE;

-- Supprimer la table principale
DROP TABLE IF EXISTS road_traffic_feats_map CASCADE;
DROP TABLE IF EXISTS road_traffic_stats_hour CASCADE;
DROP TABLE IF EXISTS road_traffic_stats_minute CASCADE;
DROP TABLE IF EXISTS road_traffic_stats_sliding_window CASCADE;
DROP TABLE IF EXISTS avg_speed_by_max_speed_and_status CASCADE;

