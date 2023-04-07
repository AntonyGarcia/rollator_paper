import pandas as pd
import numpy as np
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import accuracy_score
from sklearn.preprocessing import LabelEncoder

def load_dataset():
    train_data = pd.read_csv('final_datasets/train.csv', skiprows=[0])
    test_data = pd.read_csv('final_datasets/test.csv', skiprows=[0])

    # Extract labels from data
    train_y = np.array(train_data.iloc[:, 0])
    test_y = np.array(test_data.iloc[:, 0])

    # Convert labels to numerical values
    label_encoder = LabelEncoder()
    train_y = label_encoder.fit_transform(train_y)
    test_y = label_encoder.transform(test_y)

    # Remove timestamp column from data
    train_x = np.array(train_data.iloc[:, 2:])
    test_x = np.array(test_data.iloc[:, 2:])

    return train_x, train_y, test_x, test_y


def train_and_evaluate(train_x, train_y, test_x, test_y):
    # Create the model
    model = RandomForestClassifier(n_estimators=100)

    # Train the model
    model.fit(train_x, train_y)

    # Evaluate the model on the test set
    y_pred = model.predict(test_x)

    # Calculate accuracy
    accuracy = accuracy_score(test_y, y_pred)

    # Print the results
    print("Results")
    print("-------------------------------")
    print("Training samples: ", len(train_x))
    print("Testing samples: ", len(test_x))
    print("Testing accuracy: ", round(accuracy * 100, 2), "%")


def main():
    # Load the data
    train_x, train_y, test_x, test_y = load_dataset()

    # Train and evaluate the model
    train_and_evaluate(train_x, train_y, test_x, test_y)


if __name__ == "__main__":
    main()
