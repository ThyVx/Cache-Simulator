package org.example;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

public class SetRandom implements ICacheSet {

    private final LinkedList<CacheLine> lines;
    private final Random random = new Random();

    public SetRandom(int associativity) {
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
                return line;
            }
        }
        return null;
    }

    @Override
    public CacheLine insert(CacheLine newLine) {
        for (int i = 0; i < lines.size(); i++) {
            if (!lines.get(i).valid) {
                lines.remove(i);
                lines.add(newLine);
                return null;
            }
        }

        int idx = random.nextInt(lines.size());
        CacheLine evicted = lines.remove(idx);
        lines.add(newLine);
        return evicted;
    }
}
