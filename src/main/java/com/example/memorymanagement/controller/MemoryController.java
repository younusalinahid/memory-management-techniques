package com.example.memorymanagement.controller;

import com.example.memorymanagement.model.MemoryBlock;
import com.example.memorymanagement.model.Process;
import com.example.memorymanagement.service.MemoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/")
public class MemoryController {

    @Autowired
    private MemoryService memoryService;

    // Web Page
    @GetMapping("/")
    public String index(Model model) {
        model.addAttribute("title", "Memory Management Techniques");
        return "index";
    }

    // REST APIs
    @ResponseBody
    @GetMapping("/api/memory-blocks")
    public List<MemoryBlock> getMemoryBlocks() {
        return memoryService.getMemoryBlocks();
    }

    @ResponseBody
    @PostMapping("/api/allocate")
    public Map<String, Object> allocateMemory(@RequestParam int size, @RequestParam String algorithm) {
        return memoryService.allocateMemory(size, algorithm);
    }

    @ResponseBody
    @PostMapping("/api/deallocate")
    public Map<String, Object> deallocateMemory(@RequestParam int blockId) {
        return memoryService.deallocateMemory(blockId);
    }

    @ResponseBody
    @PostMapping("/api/reset")
    public Map<String, Object> resetMemory() {
        return memoryService.resetMemory();
    }

    @ResponseBody
    @GetMapping("/api/processes")
    public List<Process> getProcesses() {
        return memoryService.getProcesses();
    }

    @ResponseBody
    @PostMapping("/api/create-process")
    public Map<String, Object> createProcess(@RequestParam String name, @RequestParam int size) {
        return memoryService.createProcess(name, size);
    }

    @ResponseBody
    @PostMapping("/api/page-replacement")
    public Map<String, Object> simulatePageReplacement(@RequestParam String algorithm) {
        return memoryService.simulatePageReplacement(algorithm);
    }

    @ResponseBody
    @GetMapping("/api/memory-stats")
    public Map<String, Object> getMemoryStats() {
        return memoryService.getMemoryStatistics();
    }

    @ResponseBody
    @PostMapping("/api/garbage-collect")
    public Map<String, Object> runGarbageCollection() {
        return memoryService.runGarbageCollection();
    }
}
