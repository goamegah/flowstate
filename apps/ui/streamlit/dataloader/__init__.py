import os
from sqlalchemy import text

BASE_DIR = os.path.dirname(__file__)

def load_query(filename: str) -> str:
    """Charge un fichier SQL en tant que chaîne de caractères"""
    with open(os.path.join(BASE_DIR, filename), "r") as file:
        return file.read()

def get_available_road_names(engine, resolution):
    query = text(f"""
        SELECT DISTINCT road_name
        FROM road_traffic_stats_{resolution}
        WHERE road_name IS NOT NULL
        ORDER BY road_name
    """)
    with engine.connect() as conn:
        result = conn.execute(query)
        return [row[0] for row in result.fetchall()]

def get_period_bounds_query(resolution: str):
    """Retourne la requête SQL pour récupérer la période min/max d'une route"""
    table = "road_traffic_stats_minute" if resolution == "minute" else "road_traffic_stats_hour"
    return text(f"""
        SELECT MIN(period) AS min_period, MAX(period) AS max_period
        FROM {table}
        WHERE road_name = :road_name
    """)

def get_traffic_history_query(resolution: str):
    """Retourne la requête SQL pour l’évolution temporelle d’une route"""
    table = "road_traffic_stats_minute" if resolution == "minute" else "road_traffic_stats_hour"
    return text(f"""
        SELECT period, road_name, average_speed, average_travel_time
        FROM {table}
        WHERE road_name = :road_name
        AND period BETWEEN :start AND :end
        ORDER BY period
    """)
