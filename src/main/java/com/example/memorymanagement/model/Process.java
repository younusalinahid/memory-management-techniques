package com.example.memorymanagement.model;

import java.time.LocalDateTime;

public class Process {
    private int id;
    private String name;
    private int size;
    private String status;
    private LocalDateTime createdAt;
    private int priority;

    public Process() {}

    public Process(int id, String name, int size, String status) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.priority = 0;
    }

    public Process(int id, String name, int size, String status, int priority) {
        this.id = id;
        this.name = name;
        this.size = size;
        this.status = status;
        this.createdAt = LocalDateTime.now();
        this.priority = priority;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public int getPriority() {
        return priority;
    }

    public void setPriority(int priority) {
        this.priority = priority;
    }

    @Override
    public String toString() {
        return "Process{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", size=" + size +
                ", status='" + status + '\'' +
                ", priority=" + priority +
                '}';
    }
}