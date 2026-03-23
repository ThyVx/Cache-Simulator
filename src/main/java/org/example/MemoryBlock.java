package org.example;

public class MemoryBlock {
    public final int blockNumber;
    public int value;
    public int version;

    public MemoryBlock(int blockNumber) {
        this.blockNumber = blockNumber;
        this.value = 0;
        this.version = 0;
    }
}
