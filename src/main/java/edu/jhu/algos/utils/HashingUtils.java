package edu.jhu.algos.utils;

import edu.jhu.algos.hashing.HashTable;
import edu.jhu.algos.data_structures.LinkedListChain;
import org.jfree.chart.*;
import org.jfree.chart.annotations.XYTextAnnotation;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.*;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.chart.renderer.xy.XYLineAndShapeRenderer;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.data.xy.XYSeries;
import org.jfree.data.xy.XYSeriesCollection;
import org.jfree.chart.ui.TextAnchor;

import java.awt.*;
import java.io.*;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * HashingUtils provides all support for hashing analysis:
 * - Mapping and plotting bucket distributions
 * - Collisions, probes, comparisons, and load factor visualizations
 * - Asymptotic analysis comparisons
 * - Grouped strategy comparisons
 * - Continuous comparisons from summary CSVs (for batch benchmarking)
 */
public class HashingUtils {

    private static final Logger logger = Logger.getLogger(HashingUtils.class.getName());

    public static boolean shouldGeneratePlots(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--generate-plots")) return true;
        }
        return false;
    }

    public static Map<Integer, Integer> mapBucketDistribution(HashTable table) {
        Map<Integer, Integer> distribution = new HashMap<>();
        Object[] rawTable = table.getRawTable();

        for (int i = 0; i < rawTable.length; i++) {
            if (rawTable[i] == null) {
                distribution.put(i, 0);
            } else if (rawTable[i] instanceof LinkedListChain chain) {
                distribution.put(i, chain.size());
            } else {
                distribution.put(i, 1);
            }
        }
        return distribution;
    }

    public static void plotBucketDistribution(Map<Integer, Integer> distribution, String filePath) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        distribution.forEach((index, count) -> dataset.addValue(count, "Keys", index));

        JFreeChart chart = ChartFactory.createBarChart(
                "Bucket Distribution", "Bucket Index", "Number of Keys",
                dataset, PlotOrientation.VERTICAL, false, true, false);

        formatBarChart(chart);
        saveChart(chart, filePath, 1000, 600);
    }

    public static void plotMetricsBarChart(String title, Map<String, Integer> metrics, String filePath) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        metrics.forEach((label, value) -> dataset.addValue(value, "Metrics", label));

        JFreeChart chart = ChartFactory.createBarChart(title, "Metric", "Count", dataset,
                PlotOrientation.VERTICAL, false, true, false);

        formatBarChart(chart);
        saveChart(chart, filePath, 800, 500);
    }

    public static void plotLoadFactorTrend(Map<Integer, Double> loadFactors, String filePath) {
        XYSeries series = new XYSeries("Load Factor");
        loadFactors.forEach(series::add);

        XYSeriesCollection dataset = new XYSeriesCollection(series);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Load Factor Trend", "Insertions", "Load Factor", dataset,
                PlotOrientation.VERTICAL, true, true, false);

        formatLineChart(chart, dataset, "Load Factor", Color.BLUE);
        saveChart(chart, filePath, 800, 500);
    }

    public static void plotGroupedComparison(Map<String, Map<String, Integer>> strategyMetrics,
                                             String title, String filePath, String yAxisLabel) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        strategyMetrics.forEach((strategy, inputMap) ->
                inputMap.forEach((input, value) ->
                        dataset.addValue(value, strategy, input)));

        JFreeChart chart = ChartFactory.createBarChart(
                title, "Input Size", yAxisLabel, dataset,
                PlotOrientation.VERTICAL, true, true, false);

        formatBarChart(chart);
        saveChart(chart, filePath, 1000, 600);
    }

    public static void plotAsymptoticAnalysis(Map<Integer, Integer> observedComparisons, String filePath) {
        XYSeries observed = new XYSeries("Observed Comparisons");
        XYSeries logN = new XYSeries("O(log n)");
        XYSeries linear = new XYSeries("O(n)");
        XYSeries constant = new XYSeries("O(1)");

        for (Map.Entry<Integer, Integer> entry : observedComparisons.entrySet()) {
            int n = entry.getKey();
            int comparisons = entry.getValue();
            observed.add(n, comparisons);
            logN.add(n, Math.log(n));
            linear.add(n, n);
            constant.add(n, 1);
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(observed);
        dataset.addSeries(logN);
        dataset.addSeries(linear);
        dataset.addSeries(constant);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Asymptotic Complexity vs Observed Comparisons",
                "Input Size (n)",
                "Comparisons",
                dataset,
                PlotOrientation.VERTICAL,
                true, true, false);

        formatLineChart(chart, dataset, "Observed Comparisons", Color.RED);
        saveChart(chart, filePath, 1000, 600);
    }

    public static void plotSummaryComparisonsFromCSV(String csvPath, String outputPath) {
        XYSeries comparisons = new XYSeries("Total Comparisons");
        List<String[]> annotations = new ArrayList<>();
        int index = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            br.readLine(); // skip header
            String line;

            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 14 || tokens[13] == null || tokens[13].equals("NA")) continue;

                int x = index++;
                int y = (int) Double.parseDouble(tokens[13]);
                String label = String.format("%s | mod=%s | %s | c1=%s c2=%s",
                        tokens[0], tokens[4], tokens[6], tokens[7], tokens[8]);

                comparisons.add(x, y);
                annotations.add(new String[]{String.valueOf(x), String.valueOf(y), label});
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read CSV file for plotting", e);
            return;
        }

        XYSeriesCollection dataset = new XYSeriesCollection();
        dataset.addSeries(comparisons);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Comparisons Across All Runs",
                "Configuration Index", "Comparisons", dataset,
                PlotOrientation.VERTICAL, true, true, false);

        formatLineChart(chart, dataset, "Total Comparisons", Color.MAGENTA);

        XYPlot plot = chart.getXYPlot();
        for (String[] ann : annotations) {
            XYTextAnnotation annotation = new XYTextAnnotation(ann[2],
                    Double.parseDouble(ann[0]), Double.parseDouble(ann[1]));
            annotation.setFont(new Font("Dialog", Font.PLAIN, 10));
            annotation.setTextAnchor(TextAnchor.BOTTOM_LEFT);
            plot.addAnnotation(annotation);
        }

        saveChart(chart, outputPath, 1400, 1000);
    }

    public static void plotRunAllOnlyFromCSV(String csvPath, String outputPath) {
        XYSeries comparisons = new XYSeries("Run-All Comparisons");
        List<String[]> annotations = new ArrayList<>();
        int index = 0;

        try (BufferedReader br = new BufferedReader(new FileReader(csvPath))) {
            String header = br.readLine();
            String line;

            while ((line = br.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length < 14) continue;

                String predefinedFlag = tokens[12].trim();
                if (!predefinedFlag.equalsIgnoreCase("TRUE")) continue;

                int x = index++;
                int y = (int) Double.parseDouble(tokens[13]);

                String label = String.format("%s | mod=%s | %s | c1=%s c2=%s",
                        tokens[0], tokens[4], tokens[6], tokens[7], tokens[8]);

                comparisons.add(x, y);
                annotations.add(new String[]{String.valueOf(x), String.valueOf(y), label});
            }

        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to read CSV for --run-all plotting", e);
            return;
        }

        XYSeriesCollection dataset = new XYSeriesCollection(comparisons);

        JFreeChart chart = ChartFactory.createXYLineChart(
                "Comparisons for 14 Predefined Schemes",
                "Strategy Index", "Comparisons", dataset,
                PlotOrientation.VERTICAL, true, true, false);

        formatLineChart(chart, dataset, "Run-All Comparisons", Color.BLUE);

        XYPlot plot = chart.getXYPlot();
        for (String[] ann : annotations) {
            XYTextAnnotation annotation = new XYTextAnnotation(ann[2],
                    Double.parseDouble(ann[0]), Double.parseDouble(ann[1]));
            annotation.setFont(new Font("Dialog", Font.PLAIN, 10));
            annotation.setTextAnchor(TextAnchor.BOTTOM_LEFT);
            plot.addAnnotation(annotation);
        }

        saveChart(chart, outputPath, 1400, 1000);
    }

    private static void formatBarChart(JFreeChart chart) {
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();
        renderer.setSeriesPaint(0, Color.BLUE);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setRangeGridlinePaint(Color.GRAY);

        // ✅ Safe legend handling
        if (chart.getLegend() != null) {
            chart.getLegend().setFrame(BlockBorder.NONE);
        }
    }

    private static void formatLineChart(JFreeChart chart, XYSeriesCollection dataset,
                                        String primaryLabel, Color primaryColor) {
        XYPlot plot = chart.getXYPlot();
        XYLineAndShapeRenderer renderer = new XYLineAndShapeRenderer();
        for (int i = 0; i < dataset.getSeriesCount(); i++) {
            renderer.setSeriesPaint(i, i == 0 ? primaryColor : Color.GRAY);
            renderer.setSeriesShapesVisible(i, true);
        }
        plot.setRenderer(renderer);
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinePaint(Color.GRAY);
        plot.setRangeGridlinePaint(Color.GRAY);

        // Safe legend handling
        if (chart.getLegend() != null) {
            chart.getLegend().setFrame(BlockBorder.NONE);
        }
    }
    
    private static void saveChart(JFreeChart chart, String filePath, int width, int height) {
        File file = new File(filePath);
        if (!file.getParentFile().exists()) {
            if (!file.getParentFile().mkdirs()) {
                logger.warning("Could not create output directory: " + file.getParent());
                return;
            }
        }

        try {
            ChartUtils.saveChartAsPNG(file, chart, width, height);
            logger.info("Chart saved: " + filePath);
        } catch (IOException e) {
            logger.log(Level.SEVERE, "Failed to save chart to " + filePath, e);
        }
    }

    /**
     * Generates all plots for hashing experiments.
     */
    public static void generateAllPlots(HashTable table, PerformanceMetrics metrics,
                                        Map<Integer, Double> loadFactors,
                                        Map<String, Map<String, Integer>> strategyMetrics,
                                        Map<Integer, Integer> observedComparisons,
                                        String outputDir) {
        File outDir = new File(outputDir);
        if (!outDir.exists()) outDir.mkdirs();

        // Only generate bucket distribution if table is provided
        if (table != null) {
            plotBucketDistribution(mapBucketDistribution(table), outputDir + "/bucket_distribution.png");
        }

        // Only generate summary bar chart if metrics are provided
        if (metrics != null) {
            Map<String, Integer> metricMap = Map.of(
                    "Collisions", Math.toIntExact(metrics.getTotalCollisions()),
                    "Probes", Math.toIntExact(metrics.getTotalProbes()),
                    "Comparisons", Math.toIntExact(metrics.getTotalComparisons())
            );
            plotMetricsBarChart("Collisions, Probes, Comparisons", metricMap,
                    outputDir + "/collision_probes_comparisons.png");
        }

        // Always generate multi-scheme plots
        plotLoadFactorTrend(loadFactors, outputDir + "/load_factor_trend.png");

        plotGroupedComparison(strategyMetrics,
                "Strategy Comparison by Input Size",
                outputDir + "/strategy_comparison.png", "Total Comparisons");

        plotAsymptoticAnalysis(observedComparisons, outputDir + "/asymptotic_analysis.png");
    }

}
