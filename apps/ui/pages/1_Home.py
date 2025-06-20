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


# Make sure you have these imports at the top of your file
# from your_database_module import run_query, engine  # Replace with your actual imports

# --- Chargement des données de vitesse pour boxplots ---
@st.cache_data(ttl=30)
def load_speed_data() -> pd.DataFrame:
    sql = """
        SELECT
          trafficstatus as traffic_status,
          avg_speed
        FROM avg_speed_by_max_speed_and_status
    """
    return run_query(engine, sql)

# --- Chargement des données complètes ---
@st.cache_data(ttl=30)
def load_full_data() -> pd.DataFrame:
    sql = """
        SELECT *
        FROM avg_speed_by_max_speed_and_status
    """
    return run_query(engine, sql)

# Load data with error handling
try:
    speed_df = load_speed_data()
    has_speed_data = not speed_df.empty

    # Load full data for raw data display
    full_df = load_full_data()

except Exception as e:
    st.error(f"Erreur lors du chargement des données de vitesse : {e}")
    speed_df = pd.DataFrame()
    full_df = pd.DataFrame()
    has_speed_data = False

# --- Boxplot des vitesses moyennes par statut de trafic ---
if has_speed_data:
    st.markdown("## 🚗 Distributions des vitesses moyennes par statut de trafic")

    # Check if we have the required columns
    if 'traffic_status' in speed_df.columns and 'avg_speed' in speed_df.columns:
        # Remove any null values
        clean_speed_df = speed_df.dropna(subset=['traffic_status', 'avg_speed'])

        if not clean_speed_df.empty:
            boxplot = (
                alt.Chart(clean_speed_df)
                .mark_boxplot(size=50)  # Added size parameter for better visibility
                .encode(
                    x=alt.X("traffic_status:N",
                            title="Statut de trafic",
                            sort=alt.SortField("avg_speed", "median")),  # Sort by median speed
                    y=alt.Y("avg_speed:Q",
                            title="Vitesse moyenne (km/h)",
                            scale=alt.Scale(zero=False)),  # Don't force zero baseline
                    color=alt.Color("traffic_status:N",
                                    legend=alt.Legend(title="Statut de trafic")),  # Show legend
                    tooltip=["traffic_status:N", "avg_speed:Q"]  # Add tooltip
                )
                .properties(
                    width="container",
                    height=400,
                    title=alt.TitleParams(
                        text="🚗 Distribution des vitesses par statut de trafic",
                        anchor="start"
                    )
                )
            )
            st.altair_chart(boxplot, use_container_width=True)

            # Display summary statistics
            st.markdown("### 📊 Statistiques par statut de trafic")
            summary_stats = clean_speed_df.groupby('traffic_status')['avg_speed'].agg([
                'count', 'mean', 'median', 'std', 'min', 'max'
            ]).round(2)
            st.dataframe(summary_stats, use_container_width=True)

        else:
            st.warning("Aucune donnée valide trouvée après nettoyage.")
    else:
        st.error("Colonnes manquantes dans les données. Colonnes trouvées: " + str(list(speed_df.columns)))
else:
    st.warning("Aucune donnée de vitesse disponible.")

# --- Données brutes ---
with st.expander("🔍 Voir les données brutes"):
    if not full_df.empty:
        st.markdown("**Données complètes:**")
        st.dataframe(full_df, use_container_width=True)
    else:
        st.info("Aucune donnée brute disponible.")

    if has_speed_data and not speed_df.empty:
        st.markdown("**Données de vitesse pour visualisation:**")
        st.dataframe(speed_df, use_container_width=True)

# --- Métriques additionnelles ---
if has_speed_data and not speed_df.empty:
    st.markdown("## 📈 Métriques générales")

    col1, col2, col3, col4 = st.columns(4)

    with col1:
        st.metric(
            label="Nombre total d'enregistrements",
            value=len(speed_df)
        )

    with col2:
        st.metric(
            label="Vitesse moyenne globale",
            value=f"{speed_df['avg_speed'].mean():.1f} km/h"
        )

    with col3:
        st.metric(
            label="Nombre de statuts différents",
            value=speed_df['traffic_status'].nunique()
        )

    with col4:
        st.metric(
            label="Écart-type global",
            value=f"{speed_df['avg_speed'].std():.1f} km/h"
        )