import streamlit as st

def kpi_card(title, value, unit=None, delta=None, icon=None, color="#f5f5f5"):
    html = f"""
    <div style="background-color: {color}; padding: 1rem 1.5rem; border-radius: 1rem; box-shadow: 0 0 5px rgba(0,0,0,0.1);">
        <div style="font-size: 1.5rem;">{icon or ''} {title}</div>
        <div style="font-size: 2rem; font-weight: bold; margin-top: 0.5rem;">
            {value} {unit or ''}
        </div>
    </div>
    """
    st.markdown(html, unsafe_allow_html=True)
