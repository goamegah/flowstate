import pandas as pd
from sklearn.linear_model import LinearRegression
from sklearn.model_selection import train_test_split
from sklearn.metrics import mean_squared_error, r2_score
import joblib
import os

from llm.predict_model.predict_model import SpeedPredictor


# load and treat the data
def load_and_preprocess(filepath, forecast_horizon_minutes):
    # load the data
    data_frame = pd.read_csv(filepath, sep=',', encoding='utf-8')
    print("Columns found in the file:", data_frame.columns.tolist())
    # convert the date in useful characteristics
    data_frame['period'] = pd.to_datetime(data_frame['period'])
    data_frame['hour'] = data_frame['period'].dt.hour
    data_frame['day_of_week'] = data_frame['period'].dt.dayofweek
    data_frame['is_weekend'] = data_frame['day_of_week'].isin([5, 6]).astype(int)

    # pretreatment of traffic_status_list
    def process_traffic_status(status_str):
        status_str = status_str.strip('{}')
        statuses = [s.strip() for s in status_str.split(',')]
        status_counts = pd.Series(statuses).value_counts()

        # creating characteristics from statuses
        features = {
            'freeFlow_ratio': status_counts.get('freeFlow', 0) / len(statuses),
            'heavy_ratio': status_counts.get('heavy', 0) / len(statuses),
            'congested_ratio': status_counts.get('congested', 0) / len(statuses),
            'unknown_ratio': status_counts.get('unknown', 0) / len(statuses),
            'status_count': len(statuses)
        }
        return pd.Series(features)

    traffic_features = data_frame['traffic_status_list'].apply(process_traffic_status)
    data_frame = pd.concat([data_frame, traffic_features], axis=1)

    data_frame = data_frame.sort_values(['num_troncon', 'period'])

    step_minutes = 5

    if forecast_horizon_minutes % step_minutes != 0:
        raise ValueError(f"forecast_horizon_minutes doit être un multiple de {step_minutes}")

    shift_steps = forecast_horizon_minutes // step_minutes

    data_frame['average_speed_future'] = data_frame.groupby('num_troncon')['average_speed'].shift(-shift_steps)
    data_frame['forecast_horizon_minutes'] = forecast_horizon_minutes

    data_frame = data_frame.dropna(subset=['average_speed_future'])
    return data_frame


# model training
def train_model(data_frame):
    # features and target selection
    features = [
        'num_troncon', 'total_vehicle_probe', 'average_travel_time',
        'average_travel_time_reliability', 'max_speed', 'hour',
        'day_of_week', 'is_weekend', 'freeFlow_ratio', 'heavy_ratio',
        'congested_ratio', 'unknown_ratio', 'status_count',
        'forecast_horizon_minutes'
    ]

    target = 'average_speed_future'

    # data separation
    X = data_frame[features]
    y = data_frame[target]

    # train/test separation
    X_train, X_test, y_train, y_test = train_test_split(X, y, test_size=0.2, random_state=42)

    # model creation
    my_model = LinearRegression()
    my_model.fit(X_train, y_train)

    # evaluation
    y_pred = my_model.predict(X_test)
    mse = mean_squared_error(y_test, y_pred)
    r2 = r2_score(y_test, y_pred)

    print(f"Model performance :")
    print(f"MSE: {mse:.2f}")
    print(f"R2 Score: {r2:.2f}")

    return my_model


# save model in saved_model directory
def save_model(my_model, filename):
    # go up one level from train_model/ to llm/
    llm_dir = os.path.dirname(__file__)
    save_dir = os.path.join(llm_dir, "..", "saved_model")
    save_dir = os.path.abspath(save_dir)
    os.makedirs(save_dir, exist_ok=True)
    full_path = os.path.join(save_dir, filename)

    joblib.dump(my_model, full_path)
    print(f"Model saved as : {full_path}")


if __name__ == "__main__":
    data_path = "../data/data.csv"
    print("Loading and Preprocessing Data...")
    forecast_horizon_minutes = 30
    df = load_and_preprocess(data_path, forecast_horizon_minutes)
    print("Preprocessing Complete")
    print("Train Model...")
    model = train_model(df)
    print("Model Successfully Trained")
    print("Saving Model...")
    save_model(model, f"speed_prediction_model_{forecast_horizon_minutes}min.joblib")
    print("Saving Model Complete")

    predictor = SpeedPredictor(f"../saved_model/speed_prediction_model_{forecast_horizon_minutes}min.joblib")


    example_input = {
        'num_troncon': 2,
        'total_vehicle_probe': 5,
        'average_travel_time': 15,
        'average_travel_time_reliability': 70,
        'max_speed': 90,
        'traffic_status_list': "{freeFlow,heavy,freeFlow}",
        'forecast_horizon_minutes': forecast_horizon_minutes
    }

    predicted_speed = predictor.predict(example_input)
    print(f"Predicted average speed : {predicted_speed[0]:.2f} km/h")
