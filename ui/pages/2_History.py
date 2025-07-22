import streamlit as st
import pandas as pd
import altair as alt
from dataloader.data_loader import get_db_engine, run_query
from streamlit_autorefresh import st_autorefresh
from datetime import datetime

st.set_page_config(page_title="📈 History - Traffic Trends", layout="wide")
st.title("📈 Historique du trafic urbain")

# Rafraîchissement auto (toutes les 60 secondes)
st_autorefresh(interval=60 * 1000, key="refresh-history")

# --- Résolution temporelle ---
if "resolution" not in st.session_state:
    st.session_state["resolution"] = "minute"

resolution = st.radio(
    "⏱️ Résolution :",
    options=["minute", "hour"],
    horizontal=True,
    index=["minute", "hour"].index(st.session_state["resolution"])
)
st.session_state["resolution"] = resolution

# --- Connexion ---
engine = get_db_engine()

# --- Chargement des données ---
@st.cache_data(ttl=60)
def load_history_data(res: str) -> pd.DataFrame:
    table = f"road_traffic_stats_{res}"
    query = f"""
        SELECT
            period,
            segment_id,
            denomination,
            trafficstatus,
            avg_speed,
            avg_travel_time,
            avg_reliability,
            count
        FROM {table}
        ORDER BY period DESC
    """
    return run_query(engine, query)

df = load_history_data(resolution)

if df.empty:
    st.warning("❌ Aucune donnée disponible.")
    st.stop()

# --- Dernière actualisation ---
latest_ts = pd.to_datetime(df["period"].max())
st.markdown(f"🕒 Dernière actualisation : **{latest_ts.strftime('%d/%m/%Y %H:%M:%S')}**")

# --- Comparaison avec période précédente ---
periods = df["period"].dropna().sort_values().unique()
if len(periods) >= 2:
    current_period = periods[-1]
    previous_period = periods[-2]

    df_current = df[df["period"] == current_period]
    df_previous = df[df["period"] == previous_period]

    delta_speed = round(df_current["avg_speed"].mean() - df_previous["avg_speed"].mean(), 2)
    delta_travel = round(df_current["avg_travel_time"].mean() - df_previous["avg_travel_time"].mean(), 2)
    delta_reliab = round(df_current["avg_reliability"].mean() - df_previous["avg_reliability"].mean(), 2)
else:
    delta_speed = delta_travel = delta_reliab = None

# --- KPIs globaux ---
avg_speed = round(df["avg_speed"].mean(), 2)
max_speed = round(df["avg_speed"].max(), 2)
avg_travel = round(df["avg_travel_time"].mean(), 2)
avg_reliab = round(df["avg_reliability"].mean(), 2)
nb_rows = len(df)

st.markdown("### 📊 Indicateurs globaux")
k1, k2, k3, k4 = st.columns(4)
k1.metric("🚗 Vitesse moyenne", f"{avg_speed} km/h", delta=f"{delta_speed} km/h" if delta_speed is not None else None)
k2.metric("⏱️ Temps de trajet moyen", f"{avg_travel} min", delta=f"{delta_travel} min" if delta_travel is not None else None)
k3.metric("📶 Fiabilité moyenne", f"{avg_reliab}", delta=f"{delta_reliab}" if delta_reliab is not None else None)
k4.metric("📈 Nombres de mesures", nb_rows)

# --- Filtres interactifs ---
st.markdown("### 🎯 Filtres")
col1, col2, col3 = st.columns([3, 3, 2])

route_opts = ["Toutes"] + sorted(df["denomination"].dropna().unique())
status_opts = ["Tous"] + sorted(df["trafficstatus"].dropna().unique())

# --- Initialisation session_state ---
for key, default in [("selected_route", "Toutes"), ("selected_status", "Tous"), ("ignore_status", False)]:
    if key not in st.session_state:
        st.session_state[key] = default

with col1:
    selected_route = st.selectbox("🛣️ Choisir une route :", route_opts, index=route_opts.index(st.session_state["selected_route"]))
    st.session_state["selected_route"] = selected_route
with col2:
    selected_status = st.selectbox("🚦 Choisir un statut :", status_opts, index=status_opts.index(st.session_state["selected_status"]))
    st.session_state["selected_status"] = selected_status
with col3:
    ignore_status = st.checkbox("🧮 Agréger tous les statuts", value=st.session_state["ignore_status"])
    st.session_state["ignore_status"] = ignore_status

# --- Application des filtres ---
filtered_df = df.copy()
if selected_route != "Toutes":
    filtered_df = filtered_df[filtered_df["denomination"] == selected_route]
if not ignore_status and selected_status != "Tous":
    filtered_df = filtered_df[filtered_df["trafficstatus"] == selected_status]

if filtered_df.empty:
    st.info("Aucune donnée disponible avec ces filtres.")
    st.stop()

# --- Graphiques ---
def make_line_chart(df, y_column, label):
    return (
        alt.Chart(df)
        .mark_line(point=True)
        .encode(
            x=alt.X("period:T", title="Période"),
            y=alt.Y(f"{y_column}:Q", title=label),
            color=alt.Color("denomination:N" if ignore_status else "trafficstatus:N", title="Légende"),
            tooltip=["period:T", "denomination", "trafficstatus", y_column]
        )
        .properties(height=300)
    )

st.markdown("### 📉 Visualisation des indicateurs")

tab1, tab2, tab3 = st.tabs(["🚗 Vitesse", "⏱️ Temps de trajet", "📶 Fiabilité"])

with tab1:
    st.altair_chart(make_line_chart(filtered_df, "avg_speed", "Vitesse (km/h)"), use_container_width=True)
with tab2:
    st.altair_chart(make_line_chart(filtered_df, "avg_travel_time", "Temps de trajet (min)"), use_container_width=True)
with tab3:
    st.altair_chart(make_line_chart(filtered_df, "avg_reliability", "Fiabilité"), use_container_width=True)

# --- Export CSV ---
st.markdown("### 📥 Export CSV")
st.download_button(
    label="💾 Télécharger les données",
    data=filtered_df.to_csv(index=False).encode("utf-8"),
    file_name=f"traffic_history_{resolution}.csv",
    mime="text/csv"
)

# --- Données brutes ---
with st.expander("🔎 Voir les données filtrées"):
    st.dataframe(filtered_df, use_container_width=True)
