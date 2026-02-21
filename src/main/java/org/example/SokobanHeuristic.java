package org.example;

// SokobanHeuristic.java
public interface SokobanHeuristic {
    /**
     * Visszaadja a becsült hátralévő lépések számát a célállapotig.
     */
    int heur(SokobanState state);
}