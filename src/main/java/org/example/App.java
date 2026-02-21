package org.example;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class App {

    // 1. Pálya beolvasó metódus (Java 21 Switch Expression használatával)
    public static SokobanState parseLevel(String levelData) {
        Set<Position> walls = new HashSet<>();
        Set<Position> boxes = new HashSet<>();
        Set<Position> targets = new HashSet<>();
        Position player = null;

        String[] lines = levelData.split("\n");
        for (int y = 0; y < lines.length; y++) {
            String line = lines[y];
            for (int x = 0; x < line.length(); x++) {
                char c = line.charAt(x);
                Position p = new Position(x, y);

                // Java 21 Switch Expression
                switch (c) {
                    case '#' -> walls.add(p);
                    case '@' -> player = p;
                    case '$' -> boxes.add(p);
                    case '.' -> targets.add(p);
                    case '*' -> {
                        boxes.add(p);
                        targets.add(p);
                    }
                    case '+' -> {
                        player = p;
                        targets.add(p);
                    }
                    case ' ' -> {} // Üres mező, nincs teendő
                    default -> {}
                }
            }
        }

        if (player == null) throw new IllegalArgumentException("Nincs játékos a pályán!");
        return new SokobanState(player, boxes, walls, targets);
    }

    public static void main(String[] args) {
        // 2. Kezdőállapot definíciója Java Text Block (szövegblokk) segítségével
        String level = """
                ######
                #@ $.#
                ######
                """;

        System.out.println("Pálya beolvasása...");
        SokobanState initialState = parseLevel(level);

        // 3. Egy egyszerű heurisztika implementálása (Manhattan-távolság)
        // Később ezt a logikát fogják a userek feltölteni a weboldalra!
        SokobanHeuristic manhattanHeuristic = state -> {
            int totalDistance = 0;
            for (Position box : state.getBoxes()) {
                int minDistance = Integer.MAX_VALUE;
                for (Position target : state.getTargets()) {
                    // Manhattan távolság: |x1 - x2| + |y1 - y2|
                    int dist = Math.abs(box.x() - target.x()) + Math.abs(box.y() - target.y());
                    if (dist < minDistance) {
                        minDistance = dist;
                    }
                }
                totalDistance += minDistance;
            }
            return totalDistance;
        };

        // 4. Kereső indítása
        System.out.println("Keresés indítása A* algoritmussal...");
        AStarSearch searcher = new AStarSearch();
        List<Direction> solution = searcher.search(initialState, manhattanHeuristic);

        // 5. Eredmény kiírása
        if (solution != null) {
            System.out.println("---------------------------------");
            System.out.println("MEGOLDÁS MEGTALÁLVA!");
            System.out.println("Lépések száma: " + solution.size());
            System.out.println("Lépések sorrendje: " + solution);
        } else {
            System.out.println("Nincs megoldás erre a pályára.");
        }
    }
}
