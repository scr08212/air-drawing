import plotly.io as pio
pio.renderers.default = 'browser'

import plotly.graph_objects as go
import csv

x, y, z = [], [], []

with open('utils/3d_visualizer/ar_camera_poses.csv', newline='') as csvfile:
    reader = csv.reader(csvfile)
    next(reader)
    for row in reader:
        xi, yi, zi = map(float, row)
        x.append(xi)
        y.append(yi)
        z.append(zi)

fig = go.Figure(data=[go.Scatter3d(
    x=x, y=y, z=z,
    mode='markers',
    marker=dict(size=3, color='blue'),
)])
fig.update_layout(scene=dict(
    xaxis_title='X',
    yaxis_title='Y',
    zaxis_title='Z'
))

fig.show()
