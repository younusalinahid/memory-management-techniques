package com.example.memorymanagement.model;

public class MemoryBlock {
    private int id;
    private boolean free;
    private int size;
    private String processId;

    private boolean used;

    public boolean isUsed() {
        return used;
    }

    public void setUsed(boolean used) {
        this.used = used;
    }

    public MemoryBlock() {}

    public MemoryBlock(int id, boolean free, int size) {
        this.id = id;
        this.free = free;
        this.size = size;
        this.processId = null;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public boolean isFree() {
        return free;
    }

    public void setFree(boolean free) {
        this.free = free;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public void allocate(String processId) {
        this.free = false;
        this.processId = processId;
    }

    public void deallocate() {
        this.free = true;
        this.processId = null;
    }

    @Override
    public String toString() {
        return "MemoryBlock{" +
                "id=" + id +
                ", free=" + free +
                ", size=" + size +
                ", processId='" + processId + '\'' +
                '}';
    }
}
