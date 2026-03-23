# Cache Simulator (Java)

This project implements a configurable cache memory simulator that models how a CPU cache behaves under different configurations

## Features
- Supports multiple mapping techniques:
  - Direct mapping
  - Set-associative
  - Fully associative
    
- Replacement policies:
  - LRU (Least Recently Used)
  - FIFO
  - Random
    
- Write policies:
  - Write-through
  - Write-back
    
- Tracks performance metrics:
  - Cache hits and misses
  - Hit rate
    
- Interactive graphical user interface

## Design

The simulator is built using a modular architecture:

- Controller – manages each memory access
- Cache Core – stores cache lines and sets
- Mapper – computes index and tag from addresses
- Replacement Policies – LRU, FIFO, Random
- Write Policy – controls memory updates
- Statistics Collector – tracks performance
- Main Memory Model – simulates memory
- GUI – visualizes cache state and operations

## What I learned

- How cache memory works internally
- How replacement and write policies affect performance
- How to design modular systems using interfaces
- How low-level system behavior can be simulated in software
