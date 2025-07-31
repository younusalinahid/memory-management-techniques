package com.example.memorymanagement.model;

public class PageFrame {
    private int frameId;
    private int pageNumber;
    private boolean occupied;
    private long lastAccessTime;

    public PageFrame() {}

    public PageFrame(int frameId, int pageNumber) {
        this.frameId = frameId;
        this.pageNumber = pageNumber;
        this.occupied = pageNumber != -1;
        this.lastAccessTime = System.currentTimeMillis();
    }

    public int getFrameId() {
        return frameId;
    }

    public void setFrameId(int frameId) {
        this.frameId = frameId;
    }

    public int getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(int pageNumber) {
        this.pageNumber = pageNumber;
        this.occupied = pageNumber != -1;
        this.lastAccessTime = System.currentTimeMillis();
    }

    public boolean isOccupied() {
        return occupied;
    }

    public void setOccupied(boolean occupied) {
        this.occupied = occupied;
    }

    public long getLastAccessTime() {
        return lastAccessTime;
    }

    public void setLastAccessTime(long lastAccessTime) {
        this.lastAccessTime = lastAccessTime;
    }

    public void clear() {
        this.pageNumber = -1;
        this.occupied = false;
    }

    @Override
    public String toString() {
        return "PageFrame{" +
                "frameId=" + frameId +
                ", pageNumber=" + pageNumber +
                ", occupied=" + occupied +
                '}';
    }
}