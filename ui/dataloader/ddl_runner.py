import os
from sqlalchemy import text
from data_loader import get_db_engine

DDL_DIR = os.path.join(os.path.dirname(__file__), 'ddl')

def run_ddl():
    engine = get_db_engine()
    conn = engine.connect()

    for filename in sorted(os.listdir(DDL_DIR)):
        if filename.endswith(".sql"):
            path = os.path.join(DDL_DIR, filename)
            with open(path, 'r') as f:
                sql = f.read()
                print(f"[DDL] Executing {filename}...")
                conn.execute(text(sql))

    conn.close()

if __name__ == "__main__":
    run_ddl()