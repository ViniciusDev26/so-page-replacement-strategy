package org.example;

import java.util.*;

public class PageReplacement {
    private final int page_size;
    private int page_faults;

    // FIFO
    private final Queue<Integer> fifo_frames;

    // LRU
    private final LinkedHashMap<Integer, Integer> lru_frames;

    // CLOCK
    private List<Integer> clock_frames;
    private final Map<Integer, Boolean> reference_bits;
    private int clock_pointer;

    // NFU
    private final Map<Integer, Integer> nfu_frequency;

    // Aging
    private final Map<Integer, Integer> agingCounters;


    public PageReplacement(int page_size) {
        this.page_size = page_size;

        // FIFO
        this.fifo_frames = new LinkedList<>();

        // LRU
        this.lru_frames = new LinkedHashMap<>(page_size, 0.75f, true);

        // CLOCK
        this.clock_frames = new ArrayList<>(Collections.nCopies(page_size, -1));
        this.reference_bits = new HashMap<>();
        this.clock_pointer = 0;

        // NFU
        this.nfu_frequency = new HashMap<>();

        // AGING
        this.agingCounters = new HashMap<>();

        this.page_faults = 0;
    }

    public void simulateFIFO(int[] pages) {
        this.reset();

        for (int page : pages) {
            if (!fifo_frames.contains(page)) {
                page_faults++;
                if (fifo_frames.size() == page_size) {
                    fifo_frames.poll();
                }
                fifo_frames.add(page);
            }
            System.out.println("FIFO Frames: " + fifo_frames);
        }
        System.out.println("Total Page Faults: " + page_faults);
    }

    public void simulateLRU(int[] pages) {
        this.reset();

        for (int page : pages) {
            if (!lru_frames.containsKey(page)) {
                page_faults++;
                if (lru_frames.size() == page_size) {
                    int lruPage = lru_frames.keySet().iterator().next();
                    lru_frames.remove(lruPage);
                }
            }
            lru_frames.put(page, page);
            System.out.println("LRU Frames: " + lru_frames.keySet());
        }
        System.out.println("Total Page Faults (LRU): " + page_faults);
    }

    public void simulateClock(int[] pages) {
        this.reset();

        for (int page : pages) {
            if (!clock_frames.contains(page)) {
                page_faults++;
                while (true) {
                    int currentPage = clock_frames.get(clock_pointer);
                    if (currentPage == -1 || !reference_bits.getOrDefault(currentPage, false)) {

                        clock_frames.set(clock_pointer, page);
                        reference_bits.put(page, true);
                        clock_pointer = (clock_pointer + 1) % page_size; // Move clock pointer
                        break;
                    } else {
                        // If the page has a reference bit of 1, clear it and move the pointer
                        reference_bits.put(currentPage, false);
                        clock_pointer = (clock_pointer + 1) % page_size;
                    }
                }
            } else {
                reference_bits.put(page, true);
            }
            System.out.println("Clock Frames: " + clock_frames);
        }
        System.out.println("Total Page Faults (Clock): " + page_faults);
    }

    public void simulateOptimal(int[] pages) {
        this.reset();
        List<Integer> optimal_frames = new ArrayList<>(Collections.nCopies(page_size, -1));

        for (int i = 0; i < pages.length; i++) {
            int page = pages[i];

            // Check if page is already in frames
            if (!optimal_frames.contains(page)) {
                page_faults++;
                if (optimal_frames.contains(-1)) {
                    optimal_frames.set(optimal_frames.indexOf(-1), page);
                } else {
                    int furthestIndex = -1;
                    int pageToReplace = -1;

                    for (int frame_page : optimal_frames) {
                        int nextUse = Integer.MAX_VALUE;

                        for (int j = i + 1; j < pages.length; j++) {
                            if (pages[j] == frame_page) {
                                nextUse = j;
                                break;
                            }
                        }

                        if (nextUse == Integer.MAX_VALUE) {
                            pageToReplace = frame_page;
                            break;
                        } else if (nextUse > furthestIndex) {
                            furthestIndex = nextUse;
                            pageToReplace = frame_page;
                        }
                    }

                    optimal_frames.set(optimal_frames.indexOf(pageToReplace), page);
                }
            }

            System.out.println("Optimal Frames: " + optimal_frames);
        }
        System.out.println("Total Page Faults (Optimal): " + page_faults);
    }

    public void simulateNFU(int[] pages) {
        this.reset();
        List<Integer> nfuFrames = new ArrayList<>(Collections.nCopies(page_size, -1));

        for (int page : pages) {
            nfu_frequency.put(page, nfu_frequency.getOrDefault(page, 0) + 1);

            if (!nfuFrames.contains(page)) {
                page_faults++;
                if (nfuFrames.contains(-1)) {
                    int emptySlot = nfuFrames.indexOf(-1);
                    nfuFrames.set(emptySlot, page);
                } else {
                    int leastUsedPage = nfuFrames.getFirst();
                    for (int p : nfuFrames) {
                        if (nfu_frequency.get(p) < nfu_frequency.get(leastUsedPage)) {
                            leastUsedPage = p;
                        }
                    }
                    // Replace the least frequently used page
                    nfuFrames.set(nfuFrames.indexOf(leastUsedPage), page);
                    nfu_frequency.remove(leastUsedPage); // Reset frequency of the replaced page
                }
            }
            System.out.println("NFU Frames: " + nfuFrames);
        }
        System.out.println("Total Page Faults (NFU): " + page_faults);
    }

    public void simulateAging(int[] pages) {
        this.reset();
        List<Integer> agingFrames = new ArrayList<>(Collections.nCopies(page_size, -1));

        for (int page : pages) {
            // Update counters by shifting them to the right
            agingCounters.replaceAll((k, v) -> agingCounters.get(k) >> 1);

            if (!agingFrames.contains(page)) {
                page_faults++;
                if (agingFrames.contains(-1)) {
                    int emptySlot = agingFrames.indexOf(-1);
                    agingFrames.set(emptySlot, page);
                    agingCounters.put(page, 1 << 7); // Set the MSB for new page
                } else {
                    // Find the page with the smallest aging counter
                    int oldestPage = agingFrames.getFirst();
                    for (int p : agingFrames) {
                        if (agingCounters.getOrDefault(p, Integer.MAX_VALUE) < agingCounters.get(oldestPage)) {
                            oldestPage = p;
                        }
                    }
                    agingFrames.set(agingFrames.indexOf(oldestPage), page);
                    agingCounters.remove(oldestPage);
                    agingCounters.put(page, 1 << 7); // Set MSB for the new page
                }
            } else {
                // If page is already in frames, set its MSB
                agingCounters.put(page, (agingCounters.get(page) >> 1) | (1 << 7));
            }
            System.out.println("Aging Frames: " + agingFrames + ", Counters: " + agingCounters);
        }
        System.out.println("Total Page Faults (Aging): " + page_faults);
    }

    private void reset() {
        page_faults = 0;

        fifo_frames.clear();

        lru_frames.clear();

        clock_frames = new ArrayList<>(Collections.nCopies(page_size, -1));
        clock_pointer = 0;
        reference_bits.clear();

        nfu_frequency.clear();

        agingCounters.clear();
    }
}

