package edu.jhu.algos.utils;

import edu.jhu.algos.hashing.HashTable;
import edu.jhu.algos.data_structures.LinkedListChain;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartUtils;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.block.BlockBorder;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.DefaultCategoryDataset;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.imageio.ImageIO;

/**
 * HashingUtils provides visualization support for hashing analysis.
 * This version focuses solely on high-quality plotting of the bucket distribution.
 */
public class HashingUtils {

    /** Determines if the `--generate-plots` flag was passed */
    public static boolean shouldGeneratePlots(String[] args) {
        for (String arg : args) {
            if (arg.equalsIgnoreCase("--generate-plots")) return true;
        }
        return false;
    }

    /**
     * Maps the contents of a hash table into a distribution of keys per bucket.
     *
     * @param table HashTable instance
     * @return Map from bucket index to number of stored keys
     */
    public static Map<Integer, Integer> mapBucketDistribution(HashTable table) {
        Map<Integer, Integer> distribution = new LinkedHashMap<>();
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

    /**
     * Plots a high-quality, publication-ready bar chart showing how keys
     * are distributed across the hash table buckets.
     *
     * @param distribution   Map of bucket index to key count
     * @param filePath       Output PNG file path
     * @param runLabel       Run identifier (e.g., "scheme_12")
     * @param hashingMethod  Hash function used ("division", "custom")
     * @param modValue       Modulus value (e.g., 113)
     * @param bucketSize     Bucket size (e.g., 1 or 3)
     * @param strategy       Collision resolution strategy
     * @param c1             Optional c1 coefficient (quadratic)
     * @param c2             Optional c2 coefficient (quadratic)
     */
    public static void plotBucketDistribution(
            Map<Integer, Integer> distribution,
            String filePath,
            String runLabel,
            String hashingMethod,
            String modValue,
            String bucketSize,
            String strategy,
            String c1,
            String c2
    ) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        int totalKeys = 0;

        for (Map.Entry<Integer, Integer> entry : distribution.entrySet()) {
            int keyCount = entry.getValue();
            totalKeys += keyCount;
            dataset.addValue(keyCount, "Keys", String.valueOf(entry.getKey()));
        }

        // Compute load factor
        double loadFactor = totalKeys / (double) distribution.size();

        // Build title
        StringBuilder title = new StringBuilder("Hash Table Bucket Distribution");
        if (runLabel != null && !runLabel.isEmpty()) title.append(" — ").append(runLabel);
        if (hashingMethod != null) title.append(" | Hashing: ").append(hashingMethod);
        if (modValue != null) title.append(" mod=").append(modValue);
        if (bucketSize != null) title.append(" | bucket=").append(bucketSize);
        if (strategy != null) {
            title.append(" | Strategy: ").append(cap(strategy));
            if ("quadratic".equalsIgnoreCase(strategy)) {
                title.append(" (c1=").append(c1).append(", c2=").append(c2).append(")");
            }
        }

        JFreeChart chart = ChartFactory.createBarChart(
                title.toString(),
                "Bucket Index",
                "Number of Keys",
                dataset,
                PlotOrientation.VERTICAL,
                false, true, false
        );

        // Style and colors
        CategoryPlot plot = chart.getCategoryPlot();
        BarRenderer renderer = (BarRenderer) plot.getRenderer();

        renderer.setSeriesPaint(0, new Color(52, 152, 219));
        renderer.setDrawBarOutline(false);
        renderer.setMaximumBarWidth(0.015);
        renderer.setItemMargin(0.0);

        // Show value on top of each bar
        renderer.setDefaultItemLabelsVisible(true);
        renderer.setDefaultItemLabelGenerator(new org.jfree.chart.labels.StandardCategoryItemLabelGenerator());
        renderer.setDefaultItemLabelFont(new Font("SansSerif", Font.PLAIN, 10));

        // Simplify X-axis labels (every 5th only)
        java.util.List<?> categories = dataset.getColumnKeys();
        for (int i = 0; i < categories.size(); i++) {
            if (i % 5 != 0) {
                plot.getDomainAxis().setCategoryLabelPositions(
                        org.jfree.chart.axis.CategoryLabelPositions.UP_90
                );
                renderer.setSeriesItemLabelsVisible(0, true);
            }
        }

        // Annotate load factor
        org.jfree.chart.annotations.CategoryTextAnnotation loadFactorNote =
                new org.jfree.chart.annotations.CategoryTextAnnotation(
                        String.format("Load Factor: %.3f", loadFactor),
                        dataset.getColumnKey(0),   // First column
                        plot.getRangeAxis().getUpperBound() * 0.95 // near top
                );
        loadFactorNote.setFont(new Font("SansSerif", Font.BOLD, 14));
        loadFactorNote.setPaint(Color.DARK_GRAY);
        plot.addAnnotation(loadFactorNote);

        // Final plot polish
        plot.setBackgroundPaint(Color.WHITE);
        plot.setDomainGridlinesVisible(true);
        plot.setRangeGridlinePaint(Color.LIGHT_GRAY);
        plot.setOutlineVisible(false);

        plot.getDomainAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        plot.getDomainAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));
        plot.getRangeAxis().setLabelFont(new Font("SansSerif", Font.BOLD, 14));
        plot.getRangeAxis().setTickLabelFont(new Font("SansSerif", Font.PLAIN, 10));

        chart.getTitle().setFont(new Font("SansSerif", Font.BOLD, 16));
        if (chart.getLegend() != null) {
            chart.getLegend().setFrame(BlockBorder.NONE);
        }

        // Dynamic width scaling
        int width = Math.max(1400, distribution.size() * 10);
        int height = 600;
        saveChart(chart, filePath, width, height);
    }

    /** Capitalizes the first letter of a string. */
    private static String cap(String str) {
        if (str == null || str.isEmpty()) return "";
        return str.substring(0, 1).toUpperCase() + str.substring(1);
    }

    /**
     * Saves a JFreeChart to the specified file as a PNG.
     *
     * @param chart    JFreeChart object to save
     * @param filePath Output file path
     * @param width    Image width in pixels
     * @param height   Image height in pixels
     */
    private static void saveChart(JFreeChart chart, String filePath, int width, int height) {
        try {
            File file = new File(filePath);
            File dir = file.getParentFile();
            if (dir != null && !dir.exists()) dir.mkdirs();
            ChartUtils.saveChartAsPNG(file, chart, width, height);
            System.out.println(" Chart saved to: " + filePath);
        } catch (IOException e) {
            System.err.println("Failed to save chart: " + e.getMessage());
        }
    }
}
