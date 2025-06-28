import pandas as pd
from sqlalchemy import create_engine
from dotenv import load_dotenv
import os

load_dotenv()

DB_HOST = os.getenv("DWH_POSTGRES_HOST")
DB_PORT = os.getenv("DWH_POSTGRES_PORT")
DB_NAME = os.getenv("DWH_POSTGRES_DB")
DB_USER = os.getenv("DWH_POSTGRES_USR")
DB_PASS = os.getenv("DWH_POSTGRES_PWD")


def fetch_latest_data():
    connection_str = f"postgresql://{DB_USER}:{DB_PASS}@{DB_HOST}:{DB_PORT}/{DB_NAME}"
    engine = create_engine(connection_str)

    query = """
        SELECT * FROM traffic_data
        WHERE period = (SELECT MAX(period) FROM traffic_data)
    """
    df = pd.read_sql(query, engine)
    return df if not df.empty else None
