# Visualization

<h3 align="center">
    <a href="https://streamlit.io/"><img style="float: middle; padding: 10px 10px 10px 10px;" width="200" height="100" src="../assets/streamlit.png" /></a>
</h3>

The visualization component of the FlowState project is implemented using Streamlit, a Python framework for building data applications. The visualization provides an intuitive interface for exploring and analyzing the traffic data collected from the Rennes Metropole API.

## Application Structure

The Streamlit application is organized into several pages, each focusing on a different aspect of the traffic data:

1. **Main Page**: Provides an introduction to the FlowTrack platform and its key features
2. **Home Page**: Displays an overview of the current traffic situation with key metrics and visualizations
3. **History Page**: Allows for exploration of historical traffic data with interactive filters and visualizations
4. **Map Page**: Shows a real-time map of the traffic situation with color-coded road segments

## Main Page

The main page (`streamlit_app.py`) serves as the entry point to the application and provides:

- A title and introduction to the FlowTrack platform
- A description of the key features of the platform
- Navigation instructions for accessing the other pages
- A direct link to the History page for quick access to traffic evolution data

## Home Page

The Home page (`1_Home.py`) provides an overview of the current traffic situation with:

- Automatic refresh every 60 seconds to ensure up-to-date information
- Key performance indicators (KPIs) including:
  - Number of active road segments
  - Number of unique routes
  - Dominant traffic status
- Visualization of traffic status distribution using a bar chart
- Visualization of average speed distribution by traffic status using a box plot
- Access to raw data in an expandable section

```python
# --- KPIs globaux ---
st.markdown("### 📊 Indicateurs clés")

nb_segments = len(df)
nb_routes = df["denomination"].nunique()
status_dominant = df["trafficstatus"].mode()[0]

c1, c2, c3 = st.columns(3)
c1.metric("🧩 Tronçons actifs", nb_segments)
c2.metric("🛣️ Routes uniques", nb_routes)
c3.metric("🚦 Statut dominant", status_dominant.capitalize())
```

## History Page

The History page (`2_History.py`) allows for exploration of historical traffic data with:

- Automatic refresh every 60 seconds
- Temporal resolution selection (minute or hour)
- Global indicators with comparisons to previous periods
- Interactive filters for routes and traffic status
- Visualization of key metrics (speed, travel time, reliability) using line charts with tabs
- Data export functionality (CSV download)
- Access to raw filtered data in an expandable section

```python
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
```

## Map Page

The Map page (`3_Map.py`) shows a real-time map of the traffic situation with:

- Automatic refresh every 60 seconds
- Filtering by traffic status
- Color-coded road segments based on traffic status:
  - Green: Free flow
  - Orange: Heavy traffic
  - Red: Congested
  - Gray: Unknown
- Interactive map with tooltips showing road name, traffic status, and average speed
- Access to raw data in an expandable section

```python
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
```

## Data Loading

The visualization component connects to the PostgreSQL database to fetch the traffic data. The data loading is handled by the `data_loader.py` module, which provides functions for:

- Establishing a connection to the database
- Running SQL queries to fetch data
- Caching data to improve performance

```python
@st.cache_data(ttl=60)
def load_latest_snapshot() -> pd.DataFrame:
    query = """
            SELECT *
            FROM road_traffic_feats_map
            WHERE timestamp = (SELECT MAX(timestamp) FROM road_traffic_feats_map) \
            """
    return run_query(engine, query)
```

## Visualization Libraries

The visualization component uses several libraries to create interactive visualizations:

- **Altair**: For creating bar charts, line charts, and box plots
- **Folium**: For creating interactive maps
- **Streamlit Folium**: For integrating Folium maps with Streamlit
- **Streamlit Autorefresh**: For automatically refreshing the pages at regular intervals

These libraries provide a rich set of visualization capabilities that make the traffic data more accessible and understandable.

[Transformation](transformation.md) <- -> [Orchestration](orchestration.md)