import os
import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.svm import SVC
from sklearn.metrics import accuracy_score, confusion_matrix
from sklearn.model_selection import train_test_split
from sklearn.preprocessing import StandardScaler

# File paths dictionary (company name: file path)
files = {
    "SelvasAI": "셀바스AI_6개월_시세추이.csv",
    "Saltlux": "솔트룩스_6개월_시세추이.csv",
    "Alchera": "알체라_6개월_시세추이.csv",
    "KonanTech": "코난테크놀로지_6개월_시세추이.csv",
    "Hancom": "한글과컴퓨터_6개월_시세추이.csv"
}

# Directory to save plots
image_dir = "plots"
os.makedirs(image_dir, exist_ok=True)

for name, path in files.items():
    df = pd.read_csv(path, encoding='cp949')
    df = df.sort_values(by='일자')  # Keep original Korean column name
    df['MA5'] = df['종가'].rolling(window=5).mean()
    df['VolChange'] = df['거래량'].pct_change()
    df['UpDown'] = (df['종가'].diff() > 0).astype(int)
    df.dropna(inplace=True)

    X = df[['VolChange', 'MA5']]
    y = df['UpDown']

    scaler = StandardScaler()
    X_scaled = scaler.fit_transform(X)

    X_train, X_test, y_train, y_test = train_test_split(X_scaled, y, test_size=0.3, shuffle=False)

    model = SVC(kernel='linear')
    model.fit(X_train, y_train)
    y_pred = model.predict(X_test)

    acc = accuracy_score(y_test, y_pred)
    cm = confusion_matrix(y_test, y_pred)

    # Confusion Matrix
    plt.figure(figsize=(4, 3))
    sns.heatmap(cm, annot=True, fmt="d", cmap="Blues",
                xticklabels=['Pred ↓', 'Pred ↑'],
                yticklabels=['Actual ↓', 'Actual ↑'])
    plt.title(f"{name} Confusion Matrix\nAccuracy: {acc:.2%}")
    plt.tight_layout()
    plt.savefig(os.path.join(image_dir, f"{name}_confusion_matrix.png"))
    plt.close()

    # Scatter Plot for Prediction Accuracy
    correct = y_pred == y_test.values
    colors = ['blue' if c else 'red' for c in correct]

    test_start_idx = int(len(df) * 0.7)
    dates = df.iloc[test_start_idx:]['일자'].iloc[:len(y_test)]
    closes = df.iloc[test_start_idx:]['종가'].iloc[:len(y_test)]

    plt.figure(figsize=(10, 4))
    plt.scatter(dates, closes, c=colors, alpha=0.6, s=20)
    plt.xticks(rotation=45)
    plt.title(f"{name} Close Price Prediction Accuracy")
    plt.tight_layout()
    plt.savefig(os.path.join(image_dir, f"{name}_prediction_scatter.png"), dpi=300)
    plt.close()