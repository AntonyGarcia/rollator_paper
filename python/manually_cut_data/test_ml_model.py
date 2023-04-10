import pandas as pd
import numpy as np
from sklearn.metrics import accuracy_score
from sklearn.preprocessing import LabelEncoder
import joblib

def load_dataset():
    test_data = pd.read_csv("C:\\Users\\Antony Garcia\\Desktop\\wpi\\rollator_paper\\python\\manually_cut_data\\final_datasets\\test.csv")

    # Extract labels from data
    test_y = np.array(test_data.iloc[:, 0])

    # Convert labels to numerical values
    label_encoder = LabelEncoder()
    test_y = label_encoder.fit_transform(test_y)

    # Remove timestamp column from data
    test_x = np.array(test_data.iloc[:, 2:])

    return test_x, test_y

def load_model():
    # Load the trained model from file
    model = joblib.load('C:\\Users\\Antony Garcia\\Desktop\\wpi\\rollator_paper\\python\\manually_cut_data\\final_trained_model\\trained_model.pkl')
    return model

def test_model(model, test_x, test_y):
    # Use the trained model to make predictions on the test set
    predicted_y = model.predict(test_x)

    print(accuracy_score(test_y, predicted_y))

    #print(predicted_y[0])

    # 2 -> Step, 1 -> Motion, 0 -> Iddle

def main():
    # Load the test data
    test_x, test_y = load_dataset()

    # Load the trained model
    model = load_model()

    # Test the model on the test set
    test_model(model, test_x, test_y)

if __name__ == "__main__":
    main()
