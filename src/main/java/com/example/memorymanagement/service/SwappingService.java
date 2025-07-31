package com.example.memorymanagement.service;

import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
public class SwappingService {
    private Set<String> ramProcesses = new HashSet<>();
    private Set<String> swapProcesses = new HashSet<>();

    @PostConstruct
    public void init() {
        ramProcesses.add("Process-1");
        ramProcesses.add("Process-2");
        ramProcesses.add("Process-3");
        swapProcesses.add("Process-4");
        swapProcesses.add("Process-5");
    }

    public void addProcessToRam(String processName) {
        ramProcesses.add(processName);
    }

    public String swapOut(String processId) {
        if (ramProcesses.contains(processId)) {
            ramProcesses.remove(processId);
            swapProcesses.add(processId);
            return "✅ Process " + processId + " swapped out to disk successfully!";
        }
        return "❌ Process " + processId + " not found in RAM!";
    }

    public String swapIn(String processId) {
        if (swapProcesses.contains(processId)) {
            swapProcesses.remove(processId);
            ramProcesses.add(processId);
            return "✅ Process " + processId + " swapped in to RAM successfully!";
        }
        return "❌ Process " + processId + " not found in swap space!";
    }

    public List<String> getRamProcesses() {
        return new ArrayList<>(ramProcesses);
    }

    public List<String> getSwapProcesses() {
        return new ArrayList<>(swapProcesses);
    }
}
