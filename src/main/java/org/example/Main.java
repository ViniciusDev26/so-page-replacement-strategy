package org.example;

import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.data.category.DefaultCategoryDataset;
import org.jfree.chart.ChartUtils;

import java.io.File;

public class Main {
    public static void main(String[] args) {
        int frame_count = 3;
        int[] pages = {1, 2, 3, 4, 2, 1, 5, 3, 2, 4, 6};

        PageReplacement simulator = new PageReplacement(frame_count);

        System.out.println("FIFO Simulation:");
        int fifo_fault = simulator.simulateFIFO(pages);

        System.out.println("\nLRU Simulation:");
        int lru_fault = simulator.simulateLRU(pages);

        System.out.println("\nClock Simulation:");
        int clock_fault = simulator.simulateClock(pages);

        System.out.println("\nNFU Simulation:");
        int nfu_fault = simulator.simulateNFU(pages);

        System.out.println("\nAging Simulation:");
        int aging_fault = simulator.simulateAging(pages);

        System.out.println("\nOptimal Simulation:");
        int optimal_fault = simulator.simulateOptimal(pages);

        createBarChart(fifo_fault, lru_fault, clock_fault, nfu_fault, aging_fault, optimal_fault);
    }

    private static void createBarChart(int fifo, int lru, int clock, int nfu, int aging, int optimal) {
        DefaultCategoryDataset dataset = new DefaultCategoryDataset();
        dataset.addValue(fifo, "algorithms", "FIFO");
        dataset.addValue(lru, "algorithms", "LRU");
        dataset.addValue(clock, "algorithms", "CLOCK");
        dataset.addValue(nfu, "algorithms", "NFU");
        dataset.addValue(aging, "algorithms", "AGING");
        dataset.addValue(optimal, "algorithms", "OPTIMAL");

        JFreeChart chart = ChartFactory.createBarChart(
                "Algorithms Page Faults",
                "algorithms",
                "page faults",
                dataset
        );

        try {
            ChartUtils.saveChartAsPNG(new File("bar_chart.png"), chart, 800, 600);
            System.out.println("Chart saved as bar_chart.png");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}