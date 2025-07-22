# ui/dataloader/data_loader.py
import os
import pandas as pd
from sqlalchemy import create_engine, text
from dotenv import load_dotenv

# Charger les variables d'environnement
load_dotenv()

def get_db_engine():
    url = (
        f"postgresql+psycopg2://{os.getenv('DWH_POSTGRES_USR')}:"
        f"{os.getenv('DWH_POSTGRES_PWD')}@"
        f"{os.getenv('DWH_POSTGRES_HOST')}:{os.getenv('DWH_POSTGRES_PORT')}/"
        f"{os.getenv('DWH_POSTGRES_DB')}"
    )
    return create_engine(url)


# Exécute une requête SQL et retourne un DataFrame
def run_query(engine, sql: str) -> pd.DataFrame:
    with engine.connect() as connection:
        return pd.read_sql(text(sql), connection)

# Nombre de tronçons fermés (status = "fermé" ou "closed")
def get_closed_segments_count(engine) -> int:
    sql = """
          SELECT COUNT(*) FROM road_traffic_feats_map
          WHERE traffic_status ILIKE 'fermé'
            AND period = (SELECT MAX(period) FROM road_traffic_feats_map) \
          """
    return run_query(engine, sql).iloc[0, 0]

# Pourcentage de tronçons congestionnés
def get_congested_percentage(engine) -> float:
    sql_total = """
                SELECT COUNT(*) FROM road_traffic_feats_map
                WHERE period = (SELECT MAX(period) FROM road_traffic_feats_map) \
                """
    sql_congested = """
                    SELECT COUNT(*) FROM road_traffic_feats_map
                    WHERE traffic_status ILIKE 'congestionné'
                      AND period = (SELECT MAX(period) FROM road_traffic_feats_map) \
                    """
    total = run_query(engine, sql_total).iloc[0, 0]
    congested = run_query(engine, sql_congested).iloc[0, 0]
    return (congested / total) * 100 if total > 0 else 0.0

# Catégorie de route dominante
def get_dominant_road_category(engine) -> str:
    sql = """
          SELECT road_category
          FROM road_traffic_feats_map
          WHERE period = (SELECT MAX(period) FROM road_traffic_feats_map)
          GROUP BY road_category
          ORDER BY COUNT(*) DESC
          LIMIT 1 \
          """
    df = run_query(engine, sql)
    return df.iloc[0, 0] if not df.empty else "Inconnu"