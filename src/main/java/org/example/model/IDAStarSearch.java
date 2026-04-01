package org.example.model;

import java.util.*;

public class IDAStarSearch {

    private static class SearchResult {
        boolean found;
        int nextBound;

        SearchResult(boolean found, int nextBound) {
            this.found = found;
            this.nextBound = nextBound;
        }
    }

    private List<String> currentPath;
    private Set<SokobanState> currentPathSet;
    private Map<SokobanState, Integer> visitedPaths;

    // ÚJ: Időmérés változói
    private long startTime;
    private static final long TIME_LIMIT_MS = 10000; // 5 MÁSODPERC IDŐKORLÁT! (Ezt átírhatod)

    public List<String> search(SokobanState initialState, SokobanHeuristic heuristic) {
        currentPath = new ArrayList<>();
        currentPathSet = new HashSet<>();
        visitedPaths = new HashMap<>();

        // ÚJ: Elindítjuk a stoppert
        startTime = System.currentTimeMillis();

        int bound = heuristic.heur(initialState);
        currentPathSet.add(initialState);

        System.out.println("IDA* makró-keresés indítása (Időkorlát: " + (TIME_LIMIT_MS/1000) + " mp)...");

        while (true) {
            System.out.println("Új mélységi korlát (f-limit): " + bound);
            visitedPaths.clear();

            SearchResult result = searchDFS(initialState, 0, bound, heuristic);

            if (result.found) {
                System.out.println("Megoldás megtalálva!");
                return new ArrayList<>(currentPath);
            }
            if (result.nextBound == Integer.MAX_VALUE) {
                System.out.println("Nincs megoldás a pályára.");
                return null;
            }
            bound = result.nextBound;
        }
    }

    private SearchResult searchDFS(SokobanState current, int g, int bound, SokobanHeuristic heuristic) {
        // --- ÚJ: IDŐKORLÁT ELLENŐRZÉSE ---
        if (System.currentTimeMillis() - startTime > TIME_LIMIT_MS) {
            // Ha letelt az 5 másodperc, megszakítjuk az egészet egy hibával!
            throw new RuntimeException("Időtúllépés! Az algoritmus nem talált megoldást " + (TIME_LIMIT_MS/1000) + " másodperc alatt. Próbálj jobb heurisztikát vagy kisebb pályát!");
        }
        // ---------------------------------

        int f = g + heuristic.heur(current);

        if (f > bound) return new SearchResult(false, f);
        if (current.isGoal()) return new SearchResult(true, f);

        Integer previousG = visitedPaths.get(current);
        if (previousG != null && previousG <= g) {
            return new SearchResult(false, Integer.MAX_VALUE);
        }
        visitedPaths.put(current, g);

        int min = Integer.MAX_VALUE;

        for (SokobanState.PushMove move : current.getValidPushes()) {
            SokobanState nextState = move.nextState();

            if (!currentPathSet.contains(nextState)) {
                currentPath.add(move.description());
                currentPathSet.add(nextState);

                SearchResult result = searchDFS(nextState, g + 1, bound, heuristic);

                if (result.found) return result;
                if (result.nextBound < min) min = result.nextBound;

                currentPath.remove(currentPath.size() - 1);
                currentPathSet.remove(nextState);
            }
        }

        return new SearchResult(false, min);
    }
}