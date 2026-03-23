package org.example;

public class Main {
    public static void main(String[] args) {

        MainMemory memory = new MainMemory();

        Cache cache = new Cache(
                64,                     // cache size
                8,                      // block size
                2,                      // associativity
                WritePolicy.WRITE_BACK,
                ReplacementPolicy.LRU,
                memory                  // NEW argument
        );

        int[] memoryAccesses = {
                0, 8, 16, 24, 0, 8, 32, 40, 0, 16, 48, 56, 0
        };

        for (int addr : memoryAccesses) {
            cache.read(addr);
        }

        System.out.println("\nTesting writes");
        cache.write(0);
        cache.write(8);
        cache.write(32);
        cache.write(0);
        cache.write(64);

        // printStats() was removed in your current Cache
        System.out.println("\nHits = " + cache.getHits());
        System.out.println("Misses = " + cache.getMisses());
    }
}
