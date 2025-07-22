import streamlit as st
from PIL import Image

st.set_page_config(
    page_title="🚦 FlowTrack - Real-Time Traffic Monitoring",
    layout="wide",
    page_icon="🚦"
)

st.title("🚦 Welcome to FlowTrack")
st.subheader("Real-Time Urban Traffic Intelligence")

# --- Présentation ---
st.markdown("---")
st.markdown("""
### 🌟 Pourquoi FlowTrack ?

**FlowTrack** est une plateforme de surveillance et d'analyse du trafic en temps réel.  
Elle vise à rendre le trafic urbain plus lisible et exploitable grâce à une interface moderne et intuitive.

🔍 Fonctionnalités clés :
- Visualisation **en direct** de l’état des routes
- Analyse de l’**évolution du trafic** (vitesse, statut, temps de trajet)
- Mise en évidence des **zones critiques** (bouchons, ralentissements)
- Suivi des **tronçons routiers** et comparaison des performances

---
""")

# Infos de navigation
st.info("👉 Utilisez le **menu à gauche** pour accéder aux pages : `Home`, `Map`, et `History`.")

# Raccourci vers l'historique
if st.button("📈 Voir l'évolution du trafic"):
    st.switch_page("pages/3_History.py")

# Footer
st.markdown("---")
st.markdown(
    "<small>💡 Plateforme développée avec Streamlit • Altair • PostgreSQL • Spark Streaming</small>",
    unsafe_allow_html=True
)
