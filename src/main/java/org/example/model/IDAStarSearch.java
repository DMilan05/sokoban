package org.example.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class IDAStarSearch {

    private static class SearchResult {
        boolean found;
        int nextBound;

        SearchResult(boolean found, int nextBound) {
            this.found = found;
            this.nextBound = nextBound;
        }
    }

    private List<String> currentPath; // ÚJ: Irányok helyett Stringeket (akció leírásokat) tárolunk
    private Set<SokobanState> currentPathSet;

    public List<String> search(SokobanState initialState, SokobanHeuristic heuristic) {
        currentPath = new ArrayList<>();
        currentPathSet = new HashSet<>();

        int bound = heuristic.heur(initialState);
        currentPathSet.add(initialState);

        System.out.println("IDA* makró-keresés indítása...");

        while (true) {
            System.out.println("Új mélységi korlát (f-limit): " + bound);

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
        int f = g + heuristic.heur(current);

        if (f > bound) return new SearchResult(false, f);
        if (current.isGoal()) return new SearchResult(true, f);

        int min = Integer.MAX_VALUE;

        // --- ÚJ RÉSZ: A buta 4 irány helyett az "okos" doboztolásokat kérjük el! ---
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