import streamlit as st
import pandas as pd
import altair as alt
from datetime import datetime
from streamlit_autorefresh import st_autorefresh

from dataloader.data_loader import (
    get_db_engine,
    run_query,
    get_available_road_names,
    get_period_bounds_query,
    get_traffic_history_query,
)

st.set_page_config(page_title="📊 Traffic History", layout="wide")
st.title("📊 Traffic Evolution History")

# Rafraîchissement auto
st_autorefresh(interval=60 * 1000, key="history_refresh")

engine = get_db_engine()

resolution = st.radio("⏱️ Temporal Resolution", ["minute", "hour"], horizontal=True)

if "selected_road" not in st.session_state:
    st.session_state.selected_road = get_available_road_names(engine, resolution)[0]

road_name = st.selectbox(
    "🛣️ Road Name",
    get_available_road_names(engine, resolution),
    index=get_available_road_names(engine, resolution).index(st.session_state.selected_road),
    key="selected_road",
)

@st.cache_data(ttl=60)
def get_period_bounds(resolution: str, road_name: str):
    sql = get_period_bounds_query(resolution)
    df = run_query(engine, sql, params={"road_name": road_name})
    return (
        df["min_period"][0].to_pydatetime(),
        df["max_period"][0].to_pydatetime(),
    )

min_p, max_p = get_period_bounds(resolution, road_name)
if min_p == max_p:
    st.warning("⚠️ Pas assez de données pour cette route et cette résolution.")
    st.stop()

col1, col2 = st.columns(2)
with col1:
    start_date = st.slider(
        "📅 Start", min_value=min_p, max_value=max_p, value=min_p,
        format="YYYY-MM-DD HH:mm"
    )
with col2:
    end_date = st.slider(
        "📅 End", min_value=min_p, max_value=max_p, value=max_p,
        format="YYYY-MM-DD HH:mm"
    )

@st.cache_data(ttl=30)
def load_history(resolution: str, road_name: str,
                 start_date: datetime, end_date: datetime):
    sql = get_traffic_history_query(resolution)
    return run_query(
        engine,
        sql,
        params={"road_name": road_name, "start": start_date, "end": end_date},
    )

df = load_history(resolution, road_name, start_date, end_date)
if df.empty:
    st.warning("⚠️ Aucune donnée disponible pour les filtres choisis.")
    st.stop()

# Indicateurs clés
kpi_speed       = df["average_speed"].mean()
kpi_travel_time = df["average_travel_time"].mean()
max_speed       = df["average_speed"].max()
min_speed       = df["average_speed"].min()

st.markdown("## 📈 Indicateurs clés")
c1, c2, c3, c4 = st.columns(4)
c1.metric("🚀 Vitesse moyenne",    f"{kpi_speed:.2f} km/h")
c2.metric("⏱️ Temps moyen trajet", f"{kpi_travel_time:.2f} min")
c3.metric("📈 Vitesse max",       f"{max_speed:.2f} km/h",
         delta=f"{(max_speed - kpi_speed):+.2f}")
c4.metric("📉 Vitesse min",       f"{min_speed:.2f} km/h",
         delta=f"{(min_speed - kpi_speed):+.2f}")

st.markdown("## 📊 Évolution temporelle")
col_s, col_t = st.columns(2)

with col_s:
    st.altair_chart(
        alt.Chart(df).mark_line(point=True).encode(
            x=alt.X("period:T", title="Time"),
            y=alt.Y("average_speed:Q", title="Average Speed (km/h)"),
            tooltip=["period:T", "average_speed", "average_travel_time"],
        ).properties(width="container", height=400, title="🚗 Speed Evolution"),
        use_container_width=True,
    )

with col_t:
    st.altair_chart(
        alt.Chart(df).mark_line(point=True).encode(
            x=alt.X("period:T", title="Time"),
            y=alt.Y("average_travel_time:Q", title="Avg Travel Time (min)"),
            tooltip=["period:T", "average_speed", "average_travel_time"],
        ).properties(width="container", height=400,
                     title="⏱️ Travel Time Evolution"),
        use_container_width=True,
    )

with st.expander("🔍 Voir les données brutes"):
    st.dataframe(df)
