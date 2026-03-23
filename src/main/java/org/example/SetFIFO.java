package org.example;

import java.util.LinkedList;
import java.util.List;

public class SetFIFO implements ICacheSet {

    private final LinkedList<CacheLine> lines;

    public SetFIFO(int associativity) {
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
        for (CacheLine line : lines) {
            if (line.valid && line.tag == tag) {
                return line; // FIFO doesn't reorder
            }
        }
        return null;
    }

    @Override
    public CacheLine insert(CacheLine newLine) {
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).valid) {
                lines.remove(i);
                lines.addLast(newLine); // new arrivals go to back
                return null;
            }
        }

        // evict oldest = front
        CacheLine evicted = lines.removeFirst();
        lines.addLast(newLine);
        return evicted;
    }
}
