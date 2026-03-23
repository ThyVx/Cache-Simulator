package org.example;

import java.util.LinkedList;
import java.util.List;

public class SetLRU implements ICacheSet {

    private final LinkedList<CacheLine> lines;

    public SetLRU(int associativity) {
        lines = new LinkedList<>();
        for (int i = 0; i < associativity; i++) {
            lines.add(new CacheLine());
        }
    }

    @Override
    public List<CacheLine> getLines() {
        return lines;
    }

    @Override
    public CacheLine accessLine(int tag) {
        for (int i = 0; i < lines.size(); i++) {
            CacheLine line = lines.get(i);
            if (line.valid && line.tag == tag) {
                lines.remove(i);
                lines.addFirst(line);   // MRU front
                return line;
            }
        }
        return null;
    }

    @Override
    public CacheLine insert(CacheLine newLine) {
        // place into first invalid if possible
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).valid) {
                lines.remove(i);
                lines.addFirst(newLine);
                return null;
            }
        }

        // evict LRU (last)
        CacheLine evicted = lines.removeLast();
        lines.addFirst(newLine);
        return evicted;
    }
}
