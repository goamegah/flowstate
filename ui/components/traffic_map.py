import folium
from branca.colormap import linear
from folium import CircleMarker

def create_traffic_map(df):
    if df.empty:
        return folium.Map(location=[48.115, -1.675], zoom_start=12)

    m = folium.Map(location=[df['lat'].mean(), df['lon'].mean()], zoom_start=13)

    colormap = linear.YlOrRd_09.scale(df['avg_speed'].min(), df['avg_speed'].max())
    colormap.caption = 'Vitesse moyenne (km/h)'
    colormap.add_to(m)

    for _, row in df.iterrows():
        CircleMarker(
            location=[row['lat'], row['lon']],
            radius=7,
            color=colormap(row['avg_speed']),
            fill=True,
            fill_color=colormap(row['avg_speed']),
            fill_opacity=0.8,
            popup=folium.Popup(f"""
                <b>Route :</b> {row['route_id']}<br>
                <b>Vitesse :</b> {round(row['avg_speed'], 1)} km/h<br>
                <b>Congestion :</b> {round(row['congestion_level'] * 100)}%
            """, max_width=250)
        ).add_to(m)

    return m
