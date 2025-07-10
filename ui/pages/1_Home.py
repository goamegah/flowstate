import streamlit as st
import pandas as pd
import altair as alt
from streamlit_autorefresh import st_autorefresh

from dataloader.data_loader import get_db_engine, run_query

st.set_page_config(page_title="🏠 Home - Traffic Overview", layout="wide")
st.title("🏠 Traffic Monitoring Dashboard")

# Rafraîchissement automatique toutes les 60 secondes
st_autorefresh(interval=60 * 1000, key="home_refresh")

engine = get_db_engine()

# --- Chargement des données principales ---
@st.cache_data(ttl=30)
def load_home_data() -> pd.DataFrame:
    sql = """
        SELECT
          period,
          road_name,
          traffic_status,
          road_category
        FROM road_traffic_feats_map
        WHERE period = (SELECT MAX(period) FROM road_traffic_feats_map)
    """
    return run_query(engine, sql)

df = load_home_data()
if df.empty:
    st.warning("⚠️ Aucune donnée disponible pour l'instant.")
    st.stop()

# --- KPI globaux ---
nb_segments     = len(df)
nb_routes       = df["road_name"].nunique()
status_dominant = df["traffic_status"].mode()[0]

st.markdown("## 📈 Statistiques globales")
c1, c2, c3 = st.columns(3)
c1.metric("🧩 Tronçons total",     nb_segments)
c2.metric("🛣️ Routes différentes", nb_routes)
c3.metric("🚦 Statut dominant",    status_dominant)

# --- Répartition des statuts de trafic ---
st.markdown("## 🚦 Répartition des statuts de trafic")
status_counts = (
    df["traffic_status"]
    .value_counts()
    .rename_axis("traffic_status")
    .reset_index(name="count")
)

chart = (
    alt.Chart(status_counts)
    .mark_bar()
    .encode(
        x=alt.X("traffic_status", sort="-y", title="Statut de trafic"),
        y=alt.Y("count", title="Nombre de tronçons"),
        color=alt.Color("traffic_status", legend=None),
    )
    .properties(width="container", height=400,
                title="🚦 Nombre de tronçons par statut de trafic")
)

st.altair_chart(chart, use_container_width=True)

# --- Chargement des données de vitesse pour boxplots ---
@st.cache_data(ttl=30)
def load_speed_data() -> pd.DataFrame:
    sql = """
        SELECT
          traffic_status,
          avg_speed
        FROM traffic_status_avg_speed
    """
    return run_query(engine, sql)

try:
    speed_df = load_speed_data()
    has_speed_data = not speed_df.empty
except Exception as e:
    st.error(f"Erreur lors du chargement des données de vitesse : {e}")
    speed_df = pd.DataFrame()
    has_speed_data = False

# --- Boxplot des vitesses moyennes par statut de trafic ---
if has_speed_data:
    st.markdown("## 🚗 Distributions des vitesses moyennes par statut de trafic")
    boxplot = (
        alt.Chart(speed_df)
        .mark_boxplot()
        .encode(
            x=alt.X("traffic_status:N", title="Statut de trafic"),
            y=alt.Y("avg_speed:Q", title="Vitesse moyenne (km/h)"),
            color=alt.Color("traffic_status", legend=None),
        )
        .properties(width="container", height=400,
                    title="🚗 Distribution des vitesses par statut de trafic")
    )
    st.altair_chart(boxplot, use_container_width=True)

# --- Données brutes ---
with st.expander("🔍 Voir les données brutes"):
    st.dataframe(df)
    if has_speed_data:
        st.dataframe(speed_df)
