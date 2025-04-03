#!/usr/bin/env python3
"""
This script performs asymptotic analysis on hashing performance data by reading metrics from a CSV file,
fitting multiple candidate models to the relationship between input size and execution time, and comparing
their fits. The candidate models include:

    - Constant:      f(x) = c           (O(1))
    - Logarithmic:   f(x) = a*log(x) + b  (O(log n))
    - Linear:        f(x) = a*x + b       (O(n))
    - Quadratic:     f(x) = a*x^2 + b*x + c (O(n^2))

For each candidate model, the script uses SciPy's curve_fit to estimate the model parameters,
computes the sum of squared residuals (SSR) as a goodness-of-fit metric, and plots all candidate curves
alongside the observed data. The best model (with the lowest SSR) is highlighted in the output.

The plots are saved as 'asymptotic_comparison.png' and 'residuals_plot.png'.
"""

import os  # For file path handling.
import sys  # For system exit.
import logging  # For logging errors and info.
import pandas as pd  # For reading CSV data.
import numpy as np  # For numerical operations.
import matplotlib.pyplot as plt  # For plotting.
from scipy.optimize import curve_fit  # For curve fitting using SciPy.

# Configure logging for detailed error reporting.
logging.basicConfig(level=logging.INFO)

def read_metrics_csv(file_path: str) -> pd.DataFrame:
    """
    Reads the metrics CSV file and returns a DataFrame containing the performance metrics.

    This function attempts to load the CSV file with performance metrics, including input sizes
    and execution times. It handles errors such as file not found or CSV parsing issues.

    Args:
        file_path: A string representing the path to the CSV file.

    Returns:
        A pandas DataFrame with the CSV data.

    Raises:
        FileNotFoundError: If the CSV file does not exist.
        pd.errors.ParserError: If the CSV file cannot be parsed.
        Exception: For any other unexpected errors during file reading.
    """
    try:
        # Attempt to read the CSV file into a DataFrame.
        df = pd.read_csv(file_path)
        return df
    except FileNotFoundError as fnf_error:
        logging.error(f"CSV file not found: {file_path}")
        raise fnf_error
    except pd.errors.ParserError as parse_error:
        logging.error(f"Error parsing CSV file: {file_path}")
        raise parse_error
    except Exception as e:
        logging.error(f"Unexpected error reading CSV file: {e}")
        raise e

def filter_and_prepare_data(df: pd.DataFrame) -> (np.ndarray, np.ndarray):
    """
    Filters the DataFrame to extract valid numeric data for input sizes and execution times.

    This function converts the 'input_size' and 'execution_time_sec' columns to numeric types,
    drops rows with invalid values, and returns numpy arrays for further analysis.

    Args:
        df: A pandas DataFrame containing the performance metrics.

    Returns:
        A tuple containing:
            - x: A numpy array of input sizes (number of lines).
            - y: A numpy array of execution times (in seconds).

    Raises:
        ValueError: If no valid numeric data is available for analysis.
        Exception: For any unexpected error during data preparation.
    """
    try:
        # Convert 'input_size' and 'execution_time_sec' columns to numeric, coercing errors to NaN.
        df['input_size'] = pd.to_numeric(df['input_size'], errors='coerce')
        df['execution_time_sec'] = pd.to_numeric(df['execution_time_sec'], errors='coerce')
        # Drop rows with NaN values in the relevant columns.
        df_clean = df.dropna(subset=['input_size', 'execution_time_sec'])
        if df_clean.empty:
            raise ValueError("No valid data found for 'input_size' and 'execution_time_sec'.")
        x = df_clean['input_size'].to_numpy()
        y = df_clean['execution_time_sec'].to_numpy()
        return x, y
    except Exception as e:
        logging.error(f"Error filtering and preparing data: {e}")
        raise e

# Candidate Model Definitions:

def constant_model(x: np.ndarray, c: float) -> np.ndarray:
    """
    Constant model: f(x) = c.

    Represents O(1) complexity.

    Args:
        x: A numpy array of input sizes (unused in the computation).
        c: The constant value.

    Returns:
        A numpy array where each element is c.
    """
    return np.full_like(x, c, dtype=float)

def logarithmic_model(x: np.ndarray, a: float, b: float) -> np.ndarray:
    """
    Logarithmic model: f(x) = a * log(x) + b.

    Represents O(log n) complexity. Note: x must be > 0.

    Args:
        x: A numpy array of input sizes.
        a: The coefficient for log(x).
        b: The constant offset.

    Returns:
        A numpy array representing the model output.
    """
    return a * np.log(x) + b

def linear_model(x: np.ndarray, a: float, b: float) -> np.ndarray:
    """
    Linear model: f(x) = a * x + b.

    Represents O(n) complexity.

    Args:
        x: A numpy array of input sizes.
        a: The linear coefficient.
        b: The constant offset.

    Returns:
        A numpy array representing the model output.
    """
    return a * x + b

def quadratic_model(x: np.ndarray, a: float, b: float, c: float) -> np.ndarray:
    """
    Quadratic model: f(x) = a * x^2 + b * x + c.

    Represents O(n^2) complexity.

    Args:
        x: A numpy array of input sizes.
        a: The quadratic coefficient.
        b: The linear coefficient.
        c: The constant offset.

    Returns:
        A numpy array representing the model output.
    """
    return a * x**2 + b * x + c

def fit_candidate_model(model_func, x: np.ndarray, y: np.ndarray, p0: list) -> (np.ndarray, float):
    """
    Fits a candidate model to the data using SciPy's curve_fit and computes the sum of squared residuals (SSR).

    Args:
        model_func: The candidate model function to fit.
        x: A numpy array of input sizes.
        y: A numpy array of execution times.
        p0: Initial guess for the parameters.

    Returns:
        A tuple containing:
            - popt: The optimized parameters as a numpy array.
            - ssr: The sum of squared residuals (SSR) as a float.

    Raises:
        Exception: If the fitting process fails.
    """
    try:
        # Fit the model to the data.
        popt, _ = curve_fit(model_func, x, y, p0=p0, maxfev=10000)
        # Calculate the residuals (differences between observed and model predictions).
        residuals = y - model_func(x, *popt)
        # Compute the sum of squared residuals (SSR).
        ssr = np.sum(residuals ** 2)
        return popt, ssr
    except Exception as e:
        logging.error(f"Fitting model {model_func.__name__} failed: {e}")
        raise e

def plot_asymptotic_models(x: np.ndarray, y: np.ndarray, model_results: dict) -> None:
    """
    Plots the observed data and the fitted candidate models on a single scatter plot.

    Each candidate model's fitted curve is plotted with a unique color and a legend entry. The best-fit model
    (with the lowest SSR) is highlighted in the legend.

    The plot is saved as 'asymptotic_comparison.png'.

    Args:
        x: A numpy array of input sizes.
        y: A numpy array of execution times.
        model_results: A dictionary with keys as model names and values as tuples (popt, ssr, model_func).

    Returns:
        None.
    """
    try:
        plt.figure()  # Initialize a new figure.
        # Scatter plot of the observed data.
        plt.scatter(x, y, color='black', label="Observed Data", zorder=5)

        # Generate a smooth range of x values for plotting the model curves.
        x_fit = np.linspace(min(x), max(x), 500)

        # Track the best model (lowest SSR).
        best_model_name = None
        best_ssr = np.inf

        # Plot each candidate model.
        for model_name, (popt, ssr, model_func) in model_results.items():
            y_fit = model_func(x_fit, *popt)
            plt.plot(x_fit, y_fit, label=f"{model_name} (SSR={ssr:.2f})")
            if ssr < best_ssr:
                best_ssr = ssr
                best_model_name = model_name

        plt.xlabel("Input Size (number of lines)")
        plt.ylabel("Execution Time (sec)")
        plt.title("Asymptotic Analysis: Model Comparison")
        plt.legend()
        plt.savefig("asymptotic_comparison.png")
        plt.show()

        logging.info(f"Best fitting model: {best_model_name} with SSR = {best_ssr:.2f}")
    except Exception as e:
        logging.error(f"Error plotting asymptotic models: {e}")
        raise e

def plot_residuals(x: np.ndarray, y: np.ndarray, model_func, popt, model_name: str) -> None:
    """
    Plots the residuals for a given model fit.

    The residuals are computed as the difference between observed execution times and the model predictions.
    The plot is saved as 'residuals_plot_{model_name}.png'.

    Args:
        x: A numpy array of input sizes.
        y: A numpy array of execution times.
        model_func: The model function used for fitting.
        popt: The optimized parameters from the fitting process.
        model_name: The name of the model (used for plot labeling and file naming).

    Returns:
        None.
    """
    try:
        # Calculate the model predictions.
        y_pred = model_func(x, *popt)
        # Compute the residuals.
        residuals = y - y_pred

        plt.figure()  # Initialize a new figure.
        plt.scatter(x, residuals, label="Residuals")
        plt.axhline(0, color="red", linestyle="--", label="Zero Error")
        plt.xlabel("Input Size (number of lines)")
        plt.ylabel("Residuals (sec)")
        plt.title(f"Residuals for {model_name} Model")
        plt.legend()
        filename = f"residuals_plot_{model_name.replace(' ', '_')}.png"
        plt.savefig(filename)
        plt.show()
    except Exception as e:
        logging.error(f"Error plotting residuals for {model_name}: {e}")
        raise e

def main() -> None:
    """
    Main function that orchestrates reading the performance data, fitting candidate models, and plotting
    both the model comparisons and residuals.

    The function reads the CSV file, filters the data, fits candidate models (constant, logarithmic, linear,
    and quadratic), compares the sum of squared residuals (SSR) for each model, and generates corresponding plots.

    Returns:
        None.
    """
    try:
        # Define the path to the CSV file containing performance metrics.
        csv_file: str = "summary/metrics_summary.csv"

        # Read the CSV data.
        df: pd.DataFrame = read_metrics_csv(csv_file)

        # Prepare data: extract input sizes and execution times.
        x, y = filter_and_prepare_data(df)

        # Ensure that x values are positive for the logarithmic model.
        if np.any(x <= 0):
            raise ValueError("All input sizes must be > 0 for logarithmic model fitting.")

        # Dictionary to hold the results for each candidate model.
        model_results = {}

        # Fit the constant model (O(1)): f(x) = c.
        popt_const, ssr_const = fit_candidate_model(constant_model, x, y, p0=[np.mean(y)])
        model_results["Constant (O(1))"] = (popt_const, ssr_const, constant_model)

        # Fit the logarithmic model (O(log n)): f(x) = a*log(x) + b.
        popt_log, ssr_log = fit_candidate_model(logarithmic_model, x, y, p0=[1.0, np.mean(y)])
        model_results["Logarithmic (O(log n))"] = (popt_log, ssr_log, logarithmic_model)

        # Fit the linear model (O(n)): f(x) = a*x + b.
        popt_linear, ssr_linear = fit_candidate_model(linear_model, x, y, p0=[1.0, np.mean(y)])
        model_results["Linear (O(n))"] = (popt_linear, ssr_linear, linear_model)

        # Fit the quadratic model (O(n^2)): f(x) = a*x^2 + b*x + c.
        popt_quad, ssr_quad = fit_candidate_model(quadratic_model, x, y, p0=[1e-6, 1e-3, np.mean(y)])
        model_results["Quadratic (O(n^2))"] = (popt_quad, ssr_quad, quadratic_model)

        # Print a summary table of model fits.
        print("Model Fit Summary:")
        print(f"{'Model':<25} {'SSR':>15}")
        print("-" * 40)
        for model_name, (popt, ssr, _) in model_results.items():
            print(f"{model_name:<25} {ssr:15.2f}")

        # Plot all candidate models with the observed data.
        plot_asymptotic_models(x, y, model_results)

        # Optionally, plot residuals for the best model (lowest SSR) for further analysis.
        best_model = min(model_results.items(), key=lambda item: item[1][1])
        best_model_name, (popt_best, _, best_model_func) = best_model
        plot_residuals(x, y, best_model_func, popt_best, best_model_name)

    except Exception as e:
        logging.error(f"Failed in main execution: {e}")
        sys.exit(1)

if __name__ == "__main__":
    main()
