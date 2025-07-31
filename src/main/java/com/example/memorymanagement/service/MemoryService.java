package com.example.memorymanagement.service;

import com.example.memorymanagement.model.MemoryBlock;
import com.example.memorymanagement.model.Process;
import com.example.memorymanagement.model.PageFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

@Service
public class MemoryService {

    @Autowired
    private SwappingService swappingService;

    private List<MemoryBlock> memoryBlocks;
    private List<Process> processes;
    private List<PageFrame> pageFrames;
    private int nextProcessId = 1;
    private int totalAllocations = 0;
    private int successfulAllocations = 0;
    private int gcCollections = 0;

    public MemoryService() {
        initializeMemory();
    }

    private void initializeMemory() {
        memoryBlocks = new ArrayList<>();
        processes = new ArrayList<>();
        pageFrames = new ArrayList<>();

        for (int i = 0; i < 100; i++) {
            memoryBlocks.add(new MemoryBlock(i, true, 1));
        }

        for (int i = 0; i < 4; i++) {
            pageFrames.add(new PageFrame(i, -1));
        }
    }

    public List<MemoryBlock> getMemoryBlocks() {
        return memoryBlocks;
    }

    public List<Process> getProcesses() {
        return processes;
    }

    public Map<String, Object> allocateMemory(int size, String algorithm) {
        totalAllocations++;
        MemoryBlock allocatedBlock = null;

        switch (algorithm.toLowerCase()) {
            case "first":
                allocatedBlock = firstFit(size);
                break;
            case "best":
                allocatedBlock = bestFit(size);
                break;
            case "worst":
                allocatedBlock = worstFit(size);
                break;
        }

        Map<String, Object> result = new HashMap<>();
        if (allocatedBlock != null) {
            successfulAllocations++;
            result.put("success", true);
            result.put("blockId", allocatedBlock.getId());
            result.put("message", "Memory allocated successfully using " + algorithm + " fit");
        } else {
            result.put("success", false);
            result.put("message", "No suitable memory block found");
        }

        result.put("memoryBlocks", memoryBlocks);
        result.put("stats", getMemoryStatistics());
        return result;
    }

    private MemoryBlock firstFit(int size) {
        int count = 0;
        for (int i = 0; i <= memoryBlocks.size() - size; i++) {
            boolean canAllocate = true;
            for (int j = 0; j < size; j++) {
                if (!memoryBlocks.get(i + j).isFree()) {
                    canAllocate = false;
                    break;
                }
            }
            if (canAllocate) {
                for (int j = 0; j < size; j++) {
                    memoryBlocks.get(i + j).setFree(false);
                }
                return memoryBlocks.get(i);
            }
        }
        return null;
    }

    private MemoryBlock bestFit(int size) {
        int bestStart = -1;
        int minWaste = Integer.MAX_VALUE;

        for (int i = 0; i <= memoryBlocks.size() - size; i++) {
            boolean canAllocate = true;
            int freeBlockSize = 0;

            for (int j = i; j < memoryBlocks.size() && memoryBlocks.get(j).isFree(); j++) {
                freeBlockSize++;
            }

            if (freeBlockSize >= size) {
                int waste = freeBlockSize - size;
                if (waste < minWaste) {
                    minWaste = waste;
                    bestStart = i;
                }
            }
        }

        if (bestStart != -1) {
            for (int j = 0; j < size; j++) {
                memoryBlocks.get(bestStart + j).setFree(false);
            }
            return memoryBlocks.get(bestStart);
        }
        return null;
    }

    private MemoryBlock worstFit(int size) {
        int worstStart = -1;
        int maxSize = 0;

        for (int i = 0; i <= memoryBlocks.size() - size; i++) {
            int freeBlockSize = 0;

            for (int j = i; j < memoryBlocks.size() && memoryBlocks.get(j).isFree(); j++) {
                freeBlockSize++;
            }

            if (freeBlockSize >= size && freeBlockSize > maxSize) {
                maxSize = freeBlockSize;
                worstStart = i;
            }
        }

        if (worstStart != -1) {
            for (int j = 0; j < size; j++) {
                memoryBlocks.get(worstStart + j).setFree(false);
            }
            return memoryBlocks.get(worstStart);
        }
        return null;
    }

    public Map<String, Object> deallocateMemory(int blockId) {
        if (blockId >= 0 && blockId < memoryBlocks.size()) {
            memoryBlocks.get(blockId).setFree(true);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Memory deallocated successfully");
        result.put("memoryBlocks", memoryBlocks);
        result.put("stats", getMemoryStatistics());
        return result;
    }

    public Map<String, Object> resetMemory() {
        initializeMemory();
        totalAllocations = 0;
        successfulAllocations = 0;
        gcCollections = 0;

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("message", "Memory reset successfully");
        result.put("memoryBlocks", memoryBlocks);
        result.put("stats", getMemoryStatistics());
        return result;
    }

    public Map<String, Object> simulatePageReplacement(String algorithm) {
        int[] pageSequence = {7, 0, 1, 2, 0, 3, 0, 4, 2, 3, 0, 3, 2, 1, 2, 0, 1, 7, 0, 1};
        int pageFaults = 0;
        int pageHits = 0;

        for (PageFrame frame : pageFrames) {
            frame.setPageNumber(-1);
        }

        switch (algorithm.toLowerCase()) {
            case "fifo":
                pageFaults = simulateFIFO(pageSequence);
                break;
            case "lru":
                pageFaults = simulateLRU(pageSequence);
                break;
            case "optimal":
                pageFaults = simulateOptimal(pageSequence);
                break;
        }

        pageHits = pageSequence.length - pageFaults;
        double hitRatio = (double) pageHits / pageSequence.length * 100;

        Map<String, Object> result = new HashMap<>();
        result.put("algorithm", algorithm);
        result.put("pageFaults", pageFaults);
        result.put("pageHits", pageHits);
        result.put("hitRatio", Math.round(hitRatio));
        result.put("pageFrames", pageFrames);
        return result;
    }

    private int simulateFIFO(int[] pageSequence) {
        int pageFaults = 0;
        int frameIndex = 0;

        for (int page : pageSequence) {
            boolean found = false;
            for (PageFrame frame : pageFrames) {
                if (frame.getPageNumber() == page) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                pageFrames.get(frameIndex).setPageNumber(page);
                frameIndex = (frameIndex + 1) % pageFrames.size();
                pageFaults++;
            }
        }

        return pageFaults;
    }

    private int simulateLRU(int[] pageSequence) {
        int pageFaults = 0;
        Map<Integer, Integer> lastUsed = new HashMap<>();

        for (int i = 0; i < pageSequence.length; i++) {
            int page = pageSequence[i];
            boolean found = false;

            for (PageFrame frame : pageFrames) {
                if (frame.getPageNumber() == page) {
                    found = true;
                    lastUsed.put(page, i);
                    break;
                }
            }

            if (!found) {
                int replaceIndex = 0;
                if (pageFrames.stream().anyMatch(f -> f.getPageNumber() == -1)) {
                    // Find empty frame
                    for (int j = 0; j < pageFrames.size(); j++) {
                        if (pageFrames.get(j).getPageNumber() == -1) {
                            replaceIndex = j;
                            break;
                        }
                    }
                } else {
                    // Find LRU page
                    int minTime = Integer.MAX_VALUE;
                    for (int j = 0; j < pageFrames.size(); j++) {
                        int framePageNumber = pageFrames.get(j).getPageNumber();
                        int time = lastUsed.getOrDefault(framePageNumber, -1);
                        if (time < minTime) {
                            minTime = time;
                            replaceIndex = j;
                        }
                    }
                }

                pageFrames.get(replaceIndex).setPageNumber(page);
                lastUsed.put(page, i);
                pageFaults++;
            }
        }

        return pageFaults;
    }

    private int simulateOptimal(int[] pageSequence) {
        int pageFaults = 0;

        for (int i = 0; i < pageSequence.length; i++) {
            int page = pageSequence[i];
            boolean found = false;

            for (PageFrame frame : pageFrames) {
                if (frame.getPageNumber() == page) {
                    found = true;
                    break;
                }
            }

            if (!found) {
                int replaceIndex = 0;
                if (pageFrames.stream().anyMatch(f -> f.getPageNumber() == -1)) {
                    // Find empty frame
                    for (int j = 0; j < pageFrames.size(); j++) {
                        if (pageFrames.get(j).getPageNumber() == -1) {
                            replaceIndex = j;
                            break;
                        }
                    }
                } else {
                    // Find optimal page to replace
                    int farthest = -1;
                    for (int j = 0; j < pageFrames.size(); j++) {
                        int framePageNumber = pageFrames.get(j).getPageNumber();
                        int nextUse = pageSequence.length;

                        for (int k = i + 1; k < pageSequence.length; k++) {
                            if (pageSequence[k] == framePageNumber) {
                                nextUse = k;
                                break;
                            }
                        }

                        if (nextUse > farthest) {
                            farthest = nextUse;
                            replaceIndex = j;
                        }
                    }
                }

                pageFrames.get(replaceIndex).setPageNumber(page);
                pageFaults++;
            }
        }

        return pageFaults;
    }

    public Map<String, Object> runGarbageCollection() {
        gcCollections++;
        int objectsCollected = 0;

        Random random = new Random();
        for (MemoryBlock block : memoryBlocks) {
            if (!block.isFree() && random.nextDouble() < 0.3) {
                block.setFree(true);
                objectsCollected++;
            }
        }

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("objectsCollected", objectsCollected);
        result.put("gcCollections", gcCollections);
        result.put("memoryBlocks", memoryBlocks);
        result.put("stats", getMemoryStatistics());
        return result;
    }

    public Map<String, Object> getMemoryStatistics() {
        long allocatedBlocks = memoryBlocks.stream().filter(b -> !b.isFree()).count();
        long freeBlocks = memoryBlocks.stream().filter(MemoryBlock::isFree).count();
        double utilizationPercentage = (double) allocatedBlocks / memoryBlocks.size() * 100;
        double successRate = totalAllocations > 0 ? (double) successfulAllocations / totalAllocations * 100 : 0;

        Map<String, Object> stats = new HashMap<>();
        stats.put("allocatedBlocks", allocatedBlocks);
        stats.put("freeBlocks", freeBlocks);
        stats.put("totalBlocks", memoryBlocks.size());
        stats.put("utilizationPercentage", Math.round(utilizationPercentage));
        stats.put("totalAllocations", totalAllocations);
        stats.put("successfulAllocations", successfulAllocations);
        stats.put("successRate", Math.round(successRate));
        stats.put("activeProcesses", processes.size());
        stats.put("gcCollections", gcCollections);
        return stats;
    }
    public List<MemoryBlock> getMemoryStatus() {
        return memoryBlocks; // assuming this is your list of RAM blocks
    }

    public String allocateMemory(String processId) {
        return "Process " + processId + " allocated.";
    }

    public Map<String, Object> createProcess(String name, int size) {
        Map<String, Object> result = new HashMap<>();

        try {
            Process newProcess = new Process();
            newProcess.setId(nextProcessId++);
            newProcess.setName(name);
            newProcess.setSize(size);
            newProcess.setStatus("Ready");

            processes.add(newProcess);
            swappingService.addProcessToRam(name);

            result.put("success", true);
            result.put("processes", processes);
            result.put("message", "Process created and added to RAM");

        } catch (Exception e) {
            result.put("success", false);
            result.put("message", e.getMessage());
        }

        return result;
    }

}
