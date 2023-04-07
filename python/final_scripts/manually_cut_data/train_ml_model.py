import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.preprocessing import LabelEncoder
import joblib

def load_dataset():
    train_data = pd.read_csv('final_datasets/train.csv', skiprows=[0])

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
    model = RandomForestClassifier(n_estimators=100)
    model.fit(train_x, train_y)

    # Save the trained model to a file
    joblib.dump(model, 'final_trained_model/trained_model.pkl')
    print("Trained model saved to trained_hmm_model.pkl.")

def main():
    # Load the data
    train_x, train_y = load_dataset()

    # Train and save the model
    train_and_save_model(train_x, train_y)

if __name__ == "__main__":
    main()
