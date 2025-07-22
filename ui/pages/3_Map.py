import streamlit as st
import pandas as pd
import folium
import json
from datetime import datetime
from streamlit_folium import st_folium
from streamlit_autorefresh import st_autorefresh
from dataloader.data_loader import get_db_engine, run_query

st.set_page_config(page_title="🗺️ Traffic Map", layout="wide")
st.title("🗺️ Carte du trafic en temps réel")

# --- Rafraîchissement automatique toutes les 60 secondes
st_autorefresh(interval=60 * 1000, key="refresh-map")

# --- Initialisation filtre session_state ---
if "selected_status_map" not in st.session_state:
    st.session_state["selected_status_map"] = "Tous"

# --- Chargement des données ---
@st.cache_data(ttl=60)
def load_latest_map_data():
    sql = """
          SELECT *
          FROM road_traffic_feats_map
          WHERE timestamp = (SELECT MAX(timestamp) FROM road_traffic_feats_map) \
          """
    engine = get_db_engine()
    return run_query(engine, sql)

df = load_latest_map_data()

if df.empty:
    st.warning("❌ Aucune donnée cartographique disponible.")
    st.stop()

# --- Filtrage par statut ---
status_options = sorted(df["trafficstatus"].dropna().unique())
selected_status = st.selectbox(
    "🎯 Filtrer par statut de trafic :",
    options=["Tous"] + status_options,
    index=(["Tous"] + status_options).index(st.session_state["selected_status_map"])
)
st.session_state["selected_status_map"] = selected_status

if selected_status != "Tous":
    df = df[df["trafficstatus"] == selected_status]

# --- Infos globales ---
last_update = pd.to_datetime(df["timestamp"].max())
nb_segments = len(df)

st.markdown(f"🕒 **Dernière mise à jour :** `{last_update.strftime('%d/%m/%Y %H:%M:%S')}`")
st.markdown(f"🧩 **Nombre de tronçons affichés :** `{nb_segments}`")

# --- Couleurs selon statut ---
status_colors = {
    "freeFlow": "green",
    "heavy": "orange",
    "congested": "red",
    "unknown": "gray"
}

# --- Création carte Folium ---
traffic_map = folium.Map(location=[48.111, -1.68], zoom_start=13)

for _, row in df.iterrows():
    try:
        coords_raw = json.loads(row["coordinates"])
        coords_latlon = [(lat, lon) for lon, lat in coords_raw]
        color = status_colors.get(row["trafficstatus"], "gray")
        tooltip = f"{row['denomination']} ({row['trafficstatus']}) – {row['averagevehiclespeed']} km/h"
        folium.PolyLine(
            locations=coords_latlon,
            color=color,
            weight=4,
            opacity=0.8,
            tooltip=tooltip
        ).add_to(traffic_map)
    except Exception as e:
        st.error(f"Erreur avec le segment {row['segment_id']}: {e}")

# --- Légende intuitive ---
with st.expander("🗺️ Légende des statuts", expanded=True):
    st.markdown("""
    <ul style="list-style: none;">
        <li>🟩 <b>freeFlow</b> : Circulation fluide</li>
        <li>🟧 <b>heavy</b> : Circulation dense</li>
        <li>🟥 <b>congested</b> : Embouteillage</li>
        <li>⬜ <b>unknown</b> : Statut inconnu</li>
    </ul>
    """, unsafe_allow_html=True)

# --- Affichage carte ---
st_folium(traffic_map, width="100%", height=600)

# --- Données brutes ---
with st.expander("📄 Voir les données brutes"):
    st.dataframe(df.reset_index(drop=True))
