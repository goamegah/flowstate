# Dockerfile.txt (ui/)
FROM python:3.11-slim

# 1) Crée le dossier de travail
WORKDIR /app

# 2) Copie uniquement le requirements pour profiter du cache Docker
COPY requirements.txt ./

# 3) Augmente le timeout pip et installe les dépendances
ENV PIP_DEFAULT_TIMEOUT=100
RUN pip install --no-cache-dir --upgrade pip \
 && pip install --no-cache-dir -r requirements.txt

# 4) Copie le reste de l’application
COPY . .

# 5) Expose le port et commande de lancement
EXPOSE 8501
CMD ["streamlit", "run", "streamlit_app.py", "--server.port=8501", "--server.address=0.0.0.0"]