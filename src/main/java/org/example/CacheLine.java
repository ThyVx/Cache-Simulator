package org.example;

public class CacheLine {
    int tag;
    int blockNumber;   // NEW
    int value;         // NEW (simple “data” you can observe)
    boolean valid;
    boolean dirty;

    public CacheLine() {
        this.valid = false;
        this.dirty = false;
        this.value = 0;
        this.blockNumber = -1;
    }
}
