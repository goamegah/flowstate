# apps/ui/pages/3_Map.py

import streamlit as st
import pandas as pd
import folium
import ast
from folium import Element
from datetime import datetime
from streamlit_folium import st_folium
from streamlit_autorefresh import st_autorefresh

from dataloader.data_loader import get_db_engine, run_query

st.set_page_config(page_title="🗺️ Traffic Map", layout="wide")
st.title("🗺️ Real-Time Traffic Map")

# Rafraîchissement automatique toutes les 60 secondes
st_autorefresh(interval=60 * 1000, key="map_refresh")

engine = get_db_engine()

@st.cache_data(ttl=30)
def load_map_data() -> pd.DataFrame:
    sql = """
        SELECT
          period,
          location_id,
          road_name,
          road_category,
          traffic_status,
          geometry_linestring
        FROM road_traffic_feats_map
        WHERE geometry_linestring IS NOT NULL
          AND period = (SELECT MAX(period) FROM road_traffic_feats_map)
    """
    return run_query(engine, sql)

df = load_map_data()
if df.empty:
    st.warning("⚠️ Aucune donnée géographique disponible.")
    st.stop()

# Filtres
with st.sidebar:
    st.header("🔎 Filtres")
    road_names       = sorted(df["road_name"].dropna().unique())
    traffic_statuses = sorted(df["traffic_status"].dropna().unique())

    selected_road_names = st.multiselect("🛣️ Road(s)", road_names, default=road_names)
    selected_statuses   = st.multiselect("🚦 Statut(s)", traffic_statuses, default=traffic_statuses)

# Filtrage
filtered_df = df[
    df["road_name"].isin(selected_road_names) &
    df["traffic_status"].isin(selected_statuses)
]

# Résumé
latest_period = df["period"].max()
st.markdown(f"""
### 🛰️ Mise à jour : `{latest_period.strftime('%Y-%m-%d %H:%M')}`  
**🚗 Tronçons affichés :** {len(filtered_df)} / {len(df)}  
""")

# Couleurs par statut
STATUS_COLORS = {
    "freeFlow":   "green",
    "slow":       "orange",
    "heavy":      "red",
    "trafficJam": "darkred",
    "roadClosed": "black",
    "unknown":    "gray",
}
def get_color(status: str) -> str:
    return STATUS_COLORS.get(status, "blue")

# Création de la carte
m = folium.Map(location=[48.1147, -1.6794], zoom_start=12, control_scale=True)

for _, row in filtered_df.iterrows():
    try:
        geom   = ast.literal_eval(row["geometry_linestring"])
        coords = [(lat, lon) for lon, lat in geom["coordinates"]]
        folium.PolyLine(
            locations=coords,
            color=get_color(row["traffic_status"]),
            weight=5,
            tooltip=f"{row['road_name']} ({row['traffic_status']})"
        ).add_to(m)
    except Exception as e:
        st.error(f"Erreur sur location_id={row['location_id']}: {e}")

# Légende avec cercles Unicode
legend_html = """
<div style="
    position: fixed;
    bottom: 50px;
    left: 50px;
    width: 160px;
    background-color: white;
    border:2px solid gray;
    border-radius:5px;
    padding: 10px;
    font-size:14px;
    z-index:1000;
">
  <b>🚦 Légende</b><br>
  <span style="font-size:16px; color:green;">&#9679;</span> Free Flow<br>
  <span style="font-size:16px; color:orange;">&#9679;</span> Slow<br>
  <span style="font-size:16px; color:red;">&#9679;</span> Heavy<br>
  <span style="font-size:16px; color:darkred;">&#9679;</span> Traffic Jam<br>
  <span style="font-size:16px; color:black;">&#9679;</span> Road Closed<br>
  <span style="font-size:16px; color:gray;">&#9679;</span> Unknown
</div>
"""
m.get_root().html.add_child(Element(legend_html))

# Affichage
st.markdown("## 🧭 Carte des tronçons surveillés")
st_folium(m, width=1000, height=600)

# Données tabulaires
with st.expander("🔍 Voir les données géographiques"):
    st.dataframe(filtered_df)
