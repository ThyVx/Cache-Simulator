package org.example;

import java.util.LinkedHashMap;
import java.util.Map;

public class MainMemory {


    private final Map<Integer, MemoryBlock> blocks = new LinkedHashMap<>();

    public MemoryBlock readBlock(int blockNumber) {
        return blocks.computeIfAbsent(blockNumber, MemoryBlock::new);
    }

    public void writeBlock(int blockNumber, int newValue) {
        MemoryBlock b = blocks.computeIfAbsent(blockNumber, MemoryBlock::new);
        b.value = newValue;
        b.version++;
    }

    public Map<Integer, MemoryBlock> snapshot() {
        return blocks;
    }
}
