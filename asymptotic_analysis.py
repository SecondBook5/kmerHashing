import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
import numpy as np

# Load metrics summary CSV
df = pd.read_csv("summary/metrics_summary.csv")

# --- Fix: derive group column from actual columns ---
df['group'] = df['strategy'].astype(str) + "_" + df['hashing_method'].astype(str)

# Optional: rename columns for clarity
df.rename(columns={
    'comparisons': 'total_comparisons',
    'collisions': 'total_collisions',
    'execution_time_sec': 'execution_time'
}, inplace=True)

# Seaborn styling
sns.set(style="darkgrid", context="notebook")

# Color palette by group
palette = sns.color_palette("Set1", n_colors=len(df['group'].unique()))
group_palette = {group: color for group, color in zip(df['group'].unique(), palette)}

# ------------------ Plot 1: Total Comparisons vs Input Size + Asymptotics ------------------
plt.figure(figsize=(14, 6))
for group in df['group'].unique():
    subset = df[df['group'] == group]
    sns.lineplot(
        data=subset,
        x="input_size",
        y="total_comparisons",
        label=group,
        marker='o',
        linewidth=2,
        color=group_palette[group]
    )

# Overlay theoretical asymptotic curves
n_vals = sorted(df['input_size'].unique())
plt.plot(n_vals, [x for x in n_vals], '--', color='black', label='O(n)')
plt.plot(n_vals, [np.log2(x) * 500 for x in n_vals], '--', color='gray', label='O(log n)')
plt.plot(n_vals, [500] * len(n_vals), '--', color='lightgray', label='O(1)')

plt.title("Total Comparisons vs Input Size with Asymptotic Benchmarks")
plt.xlabel("Input Size")
plt.ylabel("Comparisons")
plt.legend(loc="upper left", fontsize='small')
plt.tight_layout()
plt.savefig("asymptotic_benchmark_plot.png")
plt.show()

# ------------------ Plot 2: Subplots for Comparisons, Collisions, and Time ------------------
fig, axs = plt.subplots(3, 1, figsize=(12, 10), sharex=True)
metrics = ["total_comparisons", "total_collisions", "execution_time"]
titles = ["Total Comparisons", "Total Collisions", "Execution Time (seconds)"]

for i, (metric, title) in enumerate(zip(metrics, titles)):
    sns.lineplot(
        data=df,
        x="input_size",
        y=metric,
        hue="group",
        marker='o',
        ax=axs[i],
        palette=group_palette,
        linewidth=2
    )
    axs[i].set_title(f"{title} vs Input Size", fontsize=12)
    axs[i].set_ylabel(title)

axs[2].set_xlabel("Input Size")
plt.tight_layout()
plt.savefig("asymptotic_subplots.png")
plt.show()
