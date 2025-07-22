import streamlit as st
import pandas as pd
import altair as alt
from streamlit_autorefresh import st_autorefresh

from dataloader.data_loader import get_db_engine, run_query

st.set_page_config(page_title="🏠 Home - Traffic Overview", layout="wide")
st.title("🏠 Aperçu du trafic urbain")

# 🔁 Rafraîchissement automatique toutes les 60 secondes
st_autorefresh(interval=60 * 1000, key="refresh-home")

# --- Connexion à la base ---
engine = get_db_engine()

# --- Chargement des données principales ---
@st.cache_data(ttl=60)
def load_latest_snapshot() -> pd.DataFrame:
    query = """
            SELECT *
            FROM road_traffic_feats_map
            WHERE timestamp = (SELECT MAX(timestamp) FROM road_traffic_feats_map) \
            """
    return run_query(engine, query)

df = load_latest_snapshot()
if df.empty:
    st.warning("⚠️ Aucune donnée disponible actuellement.")
    st.stop()

# --- Timestamp de mise à jour ---
last_ts = pd.to_datetime(df["timestamp"].max())
st.caption(f"🕒 Dernière mise à jour : `{last_ts.strftime('%d/%m/%Y %H:%M:%S')}`")

# --- KPIs globaux ---
st.markdown("### 📊 Indicateurs clés")

nb_segments = len(df)
nb_routes = df["denomination"].nunique()
status_dominant = df["trafficstatus"].mode()[0]

c1, c2, c3 = st.columns(3)
c1.metric("🧩 Tronçons actifs", nb_segments)
c2.metric("🛣️ Routes uniques", nb_routes)
c3.metric("🚦 Statut dominant", status_dominant.capitalize())

# --- Répartition des statuts ---
st.markdown("### 🚦 Répartition des statuts de trafic")

status_count = (
    df["trafficstatus"]
    .value_counts()
    .rename_axis("trafficstatus")
    .reset_index(name="count")
)

bar_chart = (
    alt.Chart(status_count)
    .mark_bar()
    .encode(
        x=alt.X("trafficstatus:N", title="Statut", sort="-y"),
        y=alt.Y("count:Q", title="Nombre de tronçons"),
        color=alt.Color("trafficstatus:N", legend=None),
        tooltip=["trafficstatus", "count"]
    )
    .properties(title="Nombre de tronçons par statut", height=400)
)

st.altair_chart(bar_chart, use_container_width=True)

# --- Vitesse moyenne par statut (boxplot) ---
@st.cache_data(ttl=60)
def load_speed_stats() -> pd.DataFrame:
    query = "SELECT * FROM traffic_status_avg_speed"
    return run_query(engine, query)

try:
    speed_df = load_speed_stats()
    has_speed_data = not speed_df.empty
except Exception as e:
    st.error(f"❌ Erreur lors du chargement des vitesses : {e}")
    has_speed_data = False

if has_speed_data:
    st.markdown("### 🚗 Distribution des vitesses moyennes par statut")
    box = (
        alt.Chart(speed_df)
        .mark_boxplot()
        .encode(
            x=alt.X("trafficstatus:N", title="Statut"),
            y=alt.Y("avg_speed:Q", title="Vitesse moyenne (km/h)"),
            color=alt.Color("trafficstatus:N", legend=None),
        )
        .properties(height=400)
    )
    st.altair_chart(box, use_container_width=True)

# --- Données brutes ---
with st.expander("📄 Voir les données brutes"):
    st.dataframe(df.reset_index(drop=True))
    if has_speed_data:
        st.markdown("#### ➕ Moyennes par statut")
        st.dataframe(speed_df)
