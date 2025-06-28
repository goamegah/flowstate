import time
from fetch_data import fetch_latest_data
from llm.predict_model.predict_model import SpeedPredictor

model = SpeedPredictor("saved_model/speed_prediction_model_30min.joblib")

while True:
    # retrieve recent data from db
    latest_data = fetch_latest_data()
    print(latest_data)

    if latest_data is None:
        print("Aucune donnée, on attend...")
        time.sleep(60)
        continue

    # prediction using latest_data
    prediction = model.predict(latest_data)

    print(f"Prévision vitesse dans 30 min : {prediction[0]:.2f} km/h")

    time.sleep(60)
