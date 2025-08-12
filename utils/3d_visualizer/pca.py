import numpy as np
from sklearn.decomposition import PCA
import matplotlib.pyplot as plt
import csv

coords = []

with open('ar_camera_poses.csv', newline='') as csvfile:
    reader = csv.reader(csvfile)
    next(reader)
    for row in reader:
        xi, yi, zi = map(float, row)
        coords.append([xi, yi, zi])

# 중심화 (PCA에 필요)
coords_centered = coords - np.mean(coords, axis=0)

# PCA 분석 (3차원 → 2차원으로 투영)
pca = PCA(n_components=2)
projected_coords = pca.fit_transform(coords_centered)

# 결과 출력
print("📌 투영된 2D 좌표 (PC1, PC2):")
for pt in projected_coords:
    print(f"{pt[0]:.4f}, {pt[1]:.4f}")

# 2D 시각화
plt.figure(figsize=(6, 6))
plt.plot(projected_coords[:, 0], projected_coords[:, 1], 'bo-', label='Projected h')
plt.xlabel("PC1 (1st Principal Component)")
plt.ylabel("PC2 (2nd Principal Component)")
plt.title("h 좌표의 PCA 기반 2D 투영")
plt.grid(True)
plt.axis('equal')
plt.legend()
plt.tight_layout()
plt.show()