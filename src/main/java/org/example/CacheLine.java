package org.example;

public class CacheLine {
    int tag;
    int blockNumber; 
    int value;         
    boolean valid;
    boolean dirty;

    public CacheLine() {
        this.valid = false;
        this.dirty = false;
        this.value = 0;
        this.blockNumber = -1;
    }
}
