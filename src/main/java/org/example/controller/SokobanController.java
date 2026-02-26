package org.example.controller;

import org.example.SokobanApplication;
import org.example.model.AStarSearch;
import org.example.model.Direction;
import org.example.model.SokobanHeuristic;
import org.example.model.SokobanState;
import org.example.service.DynamicCompilerService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class SokobanController {

    private final DynamicCompilerService compilerService;

    // A Spring automatikusan beinjektálja a szolgáltatásunkat
    public SokobanController(DynamicCompilerService compilerService) {
        this.compilerService = compilerService;
    }

    // Ez a metódus jeleníti meg a kezdőlapot (GET kérés)
    @GetMapping("/")
    public String index(Model model) {
        // Alapértelmezett kód, amit a weblap betöltésekor lát a user
        String defaultCode = """
                public int heur(SokobanState state) {
                    int totalDistance = 0;
                    for (Position box : state.getBoxes()) {
                        int minDistance = Integer.MAX_VALUE;
                        for (Position target : state.getTargets()) {
                            int dist = Math.abs(box.x() - target.x()) + Math.abs(box.y() - target.y());
                            if (dist < minDistance) {
                                minDistance = dist;
                            }
                        }
                        totalDistance += minDistance;
                    }
                    return totalDistance;
                }
                """;

        String defaultLevel = """
                ######
                #@ $.#
                ######
                """;

        model.addAttribute("code", defaultCode);
        model.addAttribute("level", defaultLevel);
        return "index"; // Visszaadja az index.html-t a templates mappából
    }

    // Ez a metódus fut le, amikor a user rákattint a "Futtatás" gombra
    @PostMapping("/solve")
    public String solve(
            @RequestParam("userCode") String userCode,
            @RequestParam("levelData") String levelData,
            Model model) {

        long startTime = System.currentTimeMillis();

        try {
            // 1. Lefordítjuk a user által beküldött Java kódot (Dinamikus injektálás!)
            SokobanHeuristic customHeuristic = compilerService.compileAndInstantiate(userCode, "UserHeuristic");

            // 2. Beolvassuk a pályát
            SokobanState initialState = SokobanApplication.parseLevel(levelData);

            // 3. Lefuttatjuk a keresőt a BEKÜLDÖTT heurisztikával
            AStarSearch searcher = new AStarSearch();
            List<Direction> solution = searcher.search(initialState, customHeuristic);

            long duration = System.currentTimeMillis() - startTime;

            // 4. Eredmények visszaküldése a weblapnak
            model.addAttribute("solution", solution);
            model.addAttribute("steps", solution != null ? solution.size() : 0);
            model.addAttribute("time", duration);
            model.addAttribute("message", solution != null ? "Sikeres megoldás!" : "Nincs megoldás a pályára.");

        } catch (Exception e) {
            model.addAttribute("error", "Hiba történt a kód fordítása vagy futtatása közben: " + e.getMessage());
        }

        // Visszatöltjük a formba a beküldött adatokat, hogy a user lássa, mit írt
        model.addAttribute("code", userCode);
        model.addAttribute("level", levelData);

        return "index";
    }
}