package org.example;

import java.util.function.Consumer;

public class Cache {

    private final int numSets;
    private final int blockSize;
    private final int associativity;
    private final ICacheSet[] sets;

    private final WritePolicy writePolicy;
    private final ReplacementPolicy replacementPolicy;

    private final MainMemory memory; // NEW
    private Consumer<String> logger;

    private int hits = 0;
    private int misses = 0;

    public Cache(int cacheSize, int blockSize, int associativity,
                 WritePolicy writePolicy,
                 ReplacementPolicy replacementPolicy,
                 MainMemory memory) {

        this.blockSize = blockSize;
        this.associativity = associativity;
        this.numSets = cacheSize / (blockSize * associativity);
        this.writePolicy = writePolicy;
        this.replacementPolicy = replacementPolicy;
        this.memory = memory;

        sets = new ICacheSet[numSets];
        for (int i = 0; i < numSets; i++) {
            switch (replacementPolicy) {
                case FIFO -> sets[i] = new SetFIFO(associativity);
                case RANDOM -> sets[i] = new SetRandom(associativity);
                default -> sets[i] = new SetLRU(associativity);
            }
        }
    }

    public static Cache create(int cacheSize, int blockSize, int associativity,
                               WritePolicy writePolicy,
                               ReplacementPolicy replacementPolicy,
                               MainMemory memory) {
        return new Cache(cacheSize, blockSize, associativity, writePolicy, replacementPolicy, memory);
    }

    public void setLogger(Consumer<String> logger) {
        this.logger = logger;
    }

    private int getIndex(int address) {
        return (address / blockSize) % numSets;
    }

    private int getTag(int address) {
        return (address / blockSize) / numSets;
    }

    private int getBlockNumber(int address) {
        return address / blockSize;
    }

    // ================= READ =================
    public void read(int address) {
        int index = getIndex(address);
        int tag = getTag(address);
        int blockNumber = getBlockNumber(address);

        CacheLine line = sets[index].accessLine(tag);

        if (line != null && line.valid) {
            hits++;
            log("READ HIT  - address " + address + " | set=" + index + " tag=" + tag +
                    " | block=" + blockNumber + " value=" + line.value);
        } else {
            misses++;

            // load from memory
            MemoryBlock mb = memory.readBlock(blockNumber);

            CacheLine newLine = new CacheLine();
            newLine.valid = true;
            newLine.dirty = false;
            newLine.tag = tag;
            newLine.blockNumber = blockNumber;
            newLine.value = mb.value;

            CacheLine evicted = sets[index].insert(newLine);
            handleEvicted(evicted, index);

            log("READ MISS - address " + address + " | set=" + index + " tag=" + tag +
                    " | loaded block=" + blockNumber + " value=" + mb.value);
        }
    }

    // ================= WRITE =================
    public void write(int address) {
        int index = getIndex(address);
        int tag = getTag(address);
        int blockNumber = getBlockNumber(address);

        CacheLine line = sets[index].accessLine(tag);

        if (line != null && line.valid) {
            // WRITE HIT
            hits++;

            if (writePolicy == WritePolicy.WRITE_BACK) {
                line.value++;       // simulate update
                line.dirty = true;
                log("WRITE HIT (WB) - address " + address +
                        " | set=" + index + " tag=" + tag +
                        " | block=" + blockNumber + " value=" + line.value + " (dirty)");
            } else {
                // write-through: update cache + memory immediately
                line.value++;
                memory.writeBlock(blockNumber, line.value);
                log("WRITE HIT (WT) - address " + address +
                        " | set=" + index + " tag=" + tag +
                        " | block=" + blockNumber + " value=" + line.value +
                        " -> wrote to memory");
            }

        } else {
            // WRITE MISS (allocate in both policies in your design)
            misses++;

            // ensure block exists in memory
            MemoryBlock mb = memory.readBlock(blockNumber);

            CacheLine newLine = new CacheLine();
            newLine.valid = true;
            newLine.tag = tag;
            newLine.blockNumber = blockNumber;

            if (writePolicy == WritePolicy.WRITE_BACK) {
                // allocate, update cache only, mark dirty
                newLine.value = mb.value + 1;
                newLine.dirty = true;

                CacheLine evicted = sets[index].insert(newLine);
                handleEvicted(evicted, index);

                log("WRITE MISS (WB allocate) - address " + address +
                        " | set=" + index + " tag=" + tag +
                        " | block=" + blockNumber + " value=" + newLine.value + " (dirty)");
            } else {
                // WT allocate: update cache + memory immediately, keep clean
                newLine.value = mb.value + 1;
                newLine.dirty = false;

                CacheLine evicted = sets[index].insert(newLine);
                handleEvicted(evicted, index);

                memory.writeBlock(blockNumber, newLine.value);

                log("WRITE MISS (WT allocate) - address " + address +
                        " | set=" + index + " tag=" + tag +
                        " | block=" + blockNumber + " value=" + newLine.value +
                        " -> wrote to memory");
            }
        }
    }

    private void handleEvicted(CacheLine evicted, int index) {
        if (evicted == null || !evicted.valid) return;

        if (writePolicy == WritePolicy.WRITE_BACK && evicted.dirty) {
            memory.writeBlock(evicted.blockNumber, evicted.value);
            log("   -> Write-back: evicted dirty line (set=" + index +
                    ", block=" + evicted.blockNumber +
                    ", tag=" + evicted.tag +
                    ", value=" + evicted.value + ")");
        }
    }

    private void log(String msg) {
        if (logger != null) logger.accept(msg);
    }

    // ================= GETTERS =================
    public ICacheSet[] getSets() { return sets; }
    public int getHits() { return hits; }
    public int getMisses() { return misses; }
    public MainMemory getMemory() { return memory; } // NEW
}
