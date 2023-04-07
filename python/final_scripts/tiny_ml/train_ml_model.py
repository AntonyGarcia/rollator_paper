from micromlgen import port
import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import LabelEncoder


def load_dataset():
    train_data = pd.read_csv('../manually_cut_data/final_datasets/train.csv', skiprows=[0])

    # Extract labels from data
    train_y = np.array(train_data.iloc[:, 0])

    # Convert labels to numerical values
    label_encoder = LabelEncoder()
    train_y = label_encoder.fit_transform(train_y)

    # Remove timestamp column from data
    train_x = np.array(train_data.iloc[:, 2:])

    return train_x, train_y

def train_and_save_model(train_x, train_y):
    # Train the random forest model
    classifier = RandomForestClassifier(n_estimators=100)
    classifier.fit(train_x, train_y)

    # Step 5: Convert the trained model to C++ code that can run on the Arduino
    arduino_code = port(classifier)

    # Save the code to a text file
    with open('classifier.cpp', 'w') as f:
        f.write(arduino_code)


def main():
    # Load the data
    train_x, train_y = load_dataset()

    # Train and save the model
    train_and_save_model(train_x, train_y)

if __name__ == "__main__":
    main()
