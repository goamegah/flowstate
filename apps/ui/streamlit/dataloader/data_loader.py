import os
import pandas as pd
from sqlalchemy import create_engine, text
from dotenv import load_dotenv

# Charger les variables d'environnement
load_dotenv()

def get_db_engine():
    url = (
        f"postgresql+psycopg2://{os.getenv('POSTGRES_USER')}:"
        f"{os.getenv('POSTGRES_PASSWORD')}@"
        f"{os.getenv('POSTGRES_HOST')}:{os.getenv('POSTGRES_PORT')}/"
        f"{os.getenv('POSTGRES_DB')}"
    )
    return create_engine(url)

def run_query(engine, query: str, params: dict = None) -> pd.DataFrame:
    """
    Exécute une requête SQL via SQLAlchemy.  
    Utilise des placeholders :name et renvoie un DataFrame.
    """
    stmt = text(query)
    with engine.connect() as conn:
        result = conn.execute(stmt, params or {})
        df = pd.DataFrame(result.fetchall(), columns=result.keys())
    return df

def get_available_road_names(engine, resolution: str) -> list:
    sql = f"""
        SELECT DISTINCT road_name
        FROM road_traffic_stats_{resolution}
        WHERE road_name IS NOT NULL
        ORDER BY road_name
    """
    df = run_query(engine, sql)
    return df["road_name"].tolist()

def get_period_bounds_query(resolution: str) -> str:
    return f"""
        SELECT
          MIN(period) AS min_period,
          MAX(period) AS max_period
        FROM road_traffic_stats_{resolution}
        WHERE road_name = :road_name
    """

def get_traffic_history_query(resolution: str) -> str:
    return f"""
        SELECT
          period,
          road_name,
          average_speed,
          average_travel_time
        FROM road_traffic_stats_{resolution}
        WHERE road_name = :road_name
          AND period BETWEEN :start AND :end
        ORDER BY period
    """
