package org.example;

public class Main {
    public static void main(String[] args) {
        int page_size = 3;
        int[] pages = {7, 0, 1, 2, 0, 3, 0, 4};

        PageReplacement simulator = new PageReplacement(page_size);

        System.out.println("FIFO Simulation:");
        simulator.simulateFIFO(pages);

        System.out.println("\nLRU Simulation:");
        simulator.simulateLRU(pages);

        System.out.println("\nClock Simulation:");
        simulator.simulateClock(pages);

        System.out.println("\nNFU Simulation:");
        simulator.simulateNFU(pages);

        System.out.println("\nAging Simulation:");
        simulator.simulateAging(pages);

        System.out.println("\nOptimal Simulation:");
        simulator.simulateOptimal(pages);
    }
}