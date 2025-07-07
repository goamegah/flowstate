# streamlit_app.py

import streamlit as st

st.set_page_config(
    page_title="FlowTrack - Real-Time Traffic Monitoring",
    layout="wide"
)

# 🎨 Mise en page principale
st.title("🚦 Welcome to FlowTrack")
st.subheader("Real-Time Urban Traffic Intelligence")

# 📷 Image d'accueil
# st.image("assets/flowtrack_banner.png", use_column_width=True)

# ✨ Introduction
st.markdown("""
---
### 🌟 Pourquoi FlowTrack ?
**FlowTrack** est une plateforme de surveillance et d'analyse du trafic en temps réel.  
Grâce à une visualisation intuitive et des indicateurs clés, nous vous aidons à :
- Suivre l'état du trafic urbain
- Visualiser les tronçons de route critiques
- Anticiper la congestion
- Analyser les performances de circulation
- Améliorer la prise de décision en mobilité

---
""")

# 🔥 Petit rappel pour utiliser le dashboard
st.info("👉 Utilisez le **menu latéral** pour naviguer entre les différentes fonctionnalités : historique, carte, indicateurs clés, et plus à venir ! 🚀")

# (Facultatif) Un bouton simple pour aller directement vers l'historique
if st.button("📈 Voir l'évolution du trafic"):
    st.switch_page("pages/2_History.py")
