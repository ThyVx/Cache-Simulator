package org.example;

import java.util.List;

public interface ICacheSet {
    CacheLine accessLine(int tag);

    CacheLine insert(CacheLine newLine);

    List<CacheLine> getLines();
}
