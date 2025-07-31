package com.example.memorymanagement.controller;

import com.example.memorymanagement.service.SwappingService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/swap")
@CrossOrigin(origins = "*")
public class SwappingController {

    private final SwappingService swappingService;

    public SwappingController(SwappingService swappingService) {
        this.swappingService = swappingService;
    }

    @PostMapping("/out")
    public String swapOut(@RequestParam String processId) {
        return swappingService.swapOut(processId);
    }

    @PostMapping("/in")
    public String swapIn(@RequestParam String processId) {
        return swappingService.swapIn(processId);
    }

    @GetMapping("/ram")
    public List<String> getRamProcesses() {
        return swappingService.getRamProcesses();
    }

    @GetMapping("/space")
    public List<String> getSwapProcesses() {
        return swappingService.getSwapProcesses();
    }

}
