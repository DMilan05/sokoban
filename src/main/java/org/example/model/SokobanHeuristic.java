package org.example.model;

import org.example.model.SokobanState;

// SokobanHeuristic.java
public interface SokobanHeuristic {
    /**
     * Visszaadja a becsült hátralévő lépések számát a célállapotig.
     */
    int heur(SokobanState state);
}