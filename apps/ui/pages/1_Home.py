import streamlit as st
import pandas as pd
import altair as alt
from streamlit_autorefresh import st_autorefresh

from dataloader.data_loader import get_db_engine, run_query

st.set_page_config(page_title="🏠 Home - Traffic Overview", layout="wide")
st.title("🏠 Traffic Monitoring Dashboard")

# Change from 60s to 5 minutes
st_autorefresh(interval=5 * 60 * 1000, key="home_refresh")

engine = get_db_engine()


# --- Chargement des données de vitesse pour visualisations ---
@st.cache_data(ttl=30)
def load_speed_data() -> pd.DataFrame:
    sql = """
        SELECT
          max_speed,
          traffic_status,
          avg_speed,
          record_count,
          min_avg_speed,
          max_avg_speed
        FROM avg_speed_by_max_speed_and_status
    """
    return run_query(engine, sql)

# Add numpy import for percentile calculations
import numpy as np

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

# --- Dynamic Visualization des vitesses moyennes par statut de trafic ---
if has_speed_data:
    st.markdown("## 🚗 Analyse des vitesses moyennes par statut de trafic")

    # Check if we have the required columns
    required_cols = ['max_speed', 'traffic_status', 'avg_speed', 'record_count']
    if all(col in speed_df.columns for col in required_cols):
        # Remove any null values
        clean_speed_df = speed_df.dropna(subset=required_cols)

        if not clean_speed_df.empty:
            # Create max_speed selector
            available_max_speeds = sorted(clean_speed_df['max_speed'].unique())

            # Create columns for better layout
            col1, col2 = st.columns([2, 1])

            with col1:
                selected_max_speed = st.selectbox(
                    "🎯 Sélectionnez la vitesse maximale authorisée à analyser:",
                    options=available_max_speeds,
                    index=0,
                    help="Choisissez une vitesse maximale authorisée pour voir les vitesses moyennes des véhicules par statut de trafic"
                )

            with col2:
                # Display data count for selected max_speed
                filtered_df = clean_speed_df[clean_speed_df['max_speed'] == selected_max_speed]
                total_records = filtered_df['record_count'].sum() if not filtered_df.empty else 0
                st.metric(
                    label="📊 Total des enregistrements",
                    value=f"{total_records:,}",
                    help="Nombre total d'enregistrements pour cette vitesse maximale"
                )

            # Filter data based on selected max_speed
            if not filtered_df.empty:


                # If min/max data is available, show range visualization
                if 'min_avg_speed' in filtered_df.columns and 'max_avg_speed' in filtered_df.columns:
                    st.markdown("### 📈 Plages de vitesses moyennes")

                    # Add average points
                    avg_points = (
                        alt.Chart(filtered_df)
                        .mark_circle(
                            size=100,
                            stroke='black',
                            strokeWidth=2
                        )
                        .encode(
                            x=alt.X("traffic_status:N"),
                            y=alt.Y("avg_speed:Q"),
                            color=alt.Color("traffic_status:N",
                                            scale=alt.Scale(scheme='category10')),
                            tooltip=[
                                alt.Tooltip("traffic_status:N", title="Statut de trafic"),
                                alt.Tooltip("avg_speed:Q", title="Vitesse moyenne", format=".1f")
                            ]
                        )
                    )

                    st.altair_chart(avg_points, use_container_width=True)

                # Compute boxplot statistics across all batches for each traffic status
                st.markdown("### 📊 Statistiques de type boxplot par statut de trafic")
                st.info("📈 Ces statistiques sont calculées sur l'ensemble des données historiques pour chaque statut de trafic à cette vitesse maximale")

                # Get all historical data for the selected max_speed to compute true boxplot stats
                historical_data = clean_speed_df[clean_speed_df['max_speed'] == selected_max_speed]

                boxplot_stats = []
                for traffic_status in historical_data['traffic_status'].unique():
                    # Get all avg_speed values for this traffic_status across all batches
                    traffic_data = historical_data[historical_data['traffic_status'] == traffic_status]
                    avg_speeds = traffic_data['avg_speed'].values

                    # Calculate true boxplot statistics across batches
                    if len(avg_speeds) > 0:
                        q1 = np.percentile(avg_speeds, 25)
                        median = np.percentile(avg_speeds, 50)
                        q3 = np.percentile(avg_speeds, 75)
                        min_val = np.min(avg_speeds)
                        max_val = np.max(avg_speeds)
                        iqr = q3 - q1
                        total_records = traffic_data['record_count'].sum()

                        boxplot_stats.append({
                            'Statut de trafic': traffic_status,
                            'Min (km/h)': f"{min_val:.1f}",
                            'Q1 (km/h)': f"{q1:.1f}",
                            'Médiane (km/h)': f"{median:.1f}",
                            'Q3 (km/h)': f"{q3:.1f}",
                            'Max (km/h)': f"{max_val:.1f}",
                            'IQR (km/h)': f"{iqr:.1f}",
                            'Total enregistrements': f"{total_records:,}"
                        })

                if boxplot_stats:
                    boxplot_df = pd.DataFrame(boxplot_stats)
                    st.dataframe(boxplot_df, use_container_width=True, hide_index=True)

                    # Add explanation
                    with st.expander("ℹ️ Explication des statistiques boxplot"):
                        st.markdown("""
                        Ces statistiques sont calculées en analysant **tous les batchs de données** pour chaque statut de trafic :
                        
                        - **Min/Max** : Valeurs minimales et maximales d'avg_speed
                        - **Q1/Q3** : Premier et troisième quartiles des avg_speed
                        - **Médiane** : Valeur médiane des avg_speed
                        - **IQR** : Écart interquartile (Q3 - Q1) indiquant l'étendue de la variable
                        - **Total enregistrements** : Somme de tous les record_count
                        """)
                else:
                    st.warning("Aucune donnée disponible pour calculer les statistiques boxplot")

                # Display summary statistics for selected max_speed
                st.markdown(f"### 📊 Statistiques sur la vitesse moyenne pour vitesse max = {selected_max_speed} km/h sur tous les batchs")

                # Create summary table
                summary_data = []
                for _, row in filtered_df.iterrows():
                    summary_data.append({
                        'Statut de trafic': row['traffic_status'],
                        'Vitesse moyenne (km/h)': f"{row['avg_speed']:.1f}",
                        'Taille échantillon': f"{row['record_count']:,}",
                        'Min (km/h)': f"{row.get('min_avg_speed', 'N/A'):.1f}" if pd.notna(row.get('min_avg_speed')) else 'N/A',
                        'Max (km/h)': f"{row.get('max_avg_speed', 'N/A'):.1f}" if pd.notna(row.get('max_avg_speed')) else 'N/A'
                    })

                summary_df = pd.DataFrame(summary_data)
                st.dataframe(summary_df, use_container_width=True, hide_index=True)

            else:
                st.warning(f"Aucune donnée disponible pour la vitesse maximale {selected_max_speed} km/h")
        else:
            st.warning("Aucune donnée valide trouvée après nettoyage")
    else:
        missing_cols = [col for col in required_cols if col not in speed_df.columns]
        st.error(f"Colonnes manquantes dans les données : {missing_cols}")
else:
    st.warning("Aucune donnée de vitesse disponible")