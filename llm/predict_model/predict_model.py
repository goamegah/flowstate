import pandas as pd
import joblib
from datetime import datetime

from llm.predict_model.traffic_processor import process_traffic_status


# real-time prediction function
class SpeedPredictor:
    def __init__(self, model_path):
        self.model = joblib.load(model_path)

    def prepare_input(self, input_data):
        """ prepare entry data for prediction """
        # convert into df if not done
        if not isinstance(input_data, pd.DataFrame):
            input_data = pd.DataFrame([input_data])

        if 'forecast_horizon_minutes' not in input_data:
            input_data['forecast_horizon_minutes'] = 0

        # ensure that all features are present
        required_features = [
            'num_troncon', 'total_vehicle_probe', 'average_travel_time',
            'average_travel_time_reliability', 'max_speed', 'hour',
            'day_of_week', 'is_weekend', 'freeFlow_ratio', 'heavy_ratio',
            'congested_ratio', 'unknown_ratio', 'status_count',
            'forecast_horizon_minutes'
        ]

        # add current time if not provided
        if 'hour' not in input_data:
            now = datetime.now()
            input_data['hour'] = now.hour
            input_data['day_of_week'] = now.weekday()
            input_data['is_weekend'] = 1 if now.weekday() in [5, 6] else 0

        # process traffic_status_list if provided
        if 'traffic_status_list' in input_data:
            traffic_features = input_data['traffic_status_list'].apply(process_traffic_status)
            input_data = pd.concat([input_data, traffic_features], axis=1)

        # ensure all necessary columns are present
        for feat in required_features:
            if feat not in input_data:
                input_data[feat] = 0

        return input_data[required_features]

    def predict(self, input_data):
        """ makes a prediction from the input data """
        prepared_data = self.prepare_input(input_data)
        return self.model.predict(prepared_data)
