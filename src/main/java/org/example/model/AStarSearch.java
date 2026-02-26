package org.example.model;

// AStarSearch.java
import org.example.model.SokobanHeuristic;
import org.example.model.Direction;
import org.example.model.SokobanState;

import java.util.*;

public class AStarSearch {

    // Egy csomópont a keresőfában
    private static class Node implements Comparable<Node> {
        SokobanState state;
        Node parent;
        Direction actionToHere;
        int g; // Megtett lépések száma (költség)
        int h; // Heurisztika (becsült hátralévő)

        public Node(SokobanState state, Node parent, Direction action, int g, int h) {
            this.state = state;
            this.parent = parent;
            this.actionToHere = action;
            this.g = g;
            this.h = h;
        }

        int getF() { return g + h; }

        @Override
        public int compareTo(Node other) {
            return Integer.compare(this.getF(), other.getF());
        }
    }

    public List<Direction> search(SokobanState initialState, SokobanHeuristic heuristic) {
        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Set<SokobanState> closedSet = new HashSet<>();

        Node startNode = new Node(initialState, null, null, 0, heuristic.heur(initialState));
        openSet.add(startNode);

        int expandedNodes = 0;

        while (!openSet.isEmpty()) {
            Node current = openSet.poll();

            // A feladat követelménye: írjuk ki a keresés lépéseit
            System.out.println("Kifejtett állapot (g=" + current.g + ", h=" + current.h + ", f=" + current.getF() + ") Dobozok: " + current.state.getBoxes());

            if (current.state.isGoal()) {
                System.out.println("Célállapot elérve! Kifejtett csomópontok: " + expandedNodes);
                return reconstructPath(current);
            }

            closedSet.add(current.state);
            expandedNodes++;

            for (Direction dir : Direction.values()) {
                SokobanState nextState = current.state.applyMove(dir);

                // Ha érvényes a lépés és még nem vizsgáltuk ezt az állapotot
                if (nextState != null && !closedSet.contains(nextState)) {
                    int nextG = current.g + 1;
                    int nextH = heuristic.heur(nextState);

                    Node neighbor = new Node(nextState, current, dir, nextG, nextH);

                    // Ellenőrizzük, van-e már a nyílt listában jobb úttal (egyszerűsített kezelés)
                    openSet.add(neighbor);
                }
            }
        }

        System.out.println("Nincs megoldás.");
        return null; // Nincs megoldás
    }

    private List<Direction> reconstructPath(Node node) {
        List<Direction> path = new ArrayList<>();
        while (node.parent != null) {
            path.add(node.actionToHere);
            node = node.parent;
        }
        Collections.reverse(path);
        return path;
    }
}