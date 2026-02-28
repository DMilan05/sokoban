package org.example.controller;

import org.example.SokobanApplication;
import org.example.model.AStarSearch;
import org.example.model.Direction;
import org.example.model.SokobanHeuristic;
import org.example.model.SokobanState;
import org.example.model.User;
import org.example.model.HeuristicSubmission;
import org.example.repository.HeuristicSubmissionRepository;
import org.example.repository.UserRepository;
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
    private final UserRepository userRepository;
    private final HeuristicSubmissionRepository submissionRepository;

    public SokobanController(DynamicCompilerService compilerService,
                             UserRepository userRepository,
                             HeuristicSubmissionRepository submissionRepository) {
        this.compilerService = compilerService;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
    }

    @GetMapping("/")
    public String index(Model model) {
        // Alapértelmezett kód
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

        // Alapértelmezett pálya
        String defaultLevel = """
                ######
                #@ $.#
                ######
                """;

        model.addAttribute("code", defaultCode);
        model.addAttribute("level", defaultLevel);

        // Ranglista betöltése
        model.addAttribute("leaderboard", submissionRepository.findAllByOrderByStepsToSolveAsc());
        return "index";
    }

    @PostMapping("/solve")
    public String solve(
            @RequestParam("username") String username,
            @RequestParam("userCode") String userCode,
            @RequestParam("levelData") String levelData,
            Model model) {

        long startTime = System.currentTimeMillis();

        try {
            // 1. Dinamikus fordítás
            SokobanHeuristic customHeuristic = compilerService.compileAndInstantiate(userCode, "UserHeuristic");

            // 2. Pálya beolvasása
            SokobanState initialState = SokobanApplication.parseLevel(levelData);

            // 3. A* Keresés
            AStarSearch searcher = new AStarSearch();
            List<Direction> solution = searcher.search(initialState, customHeuristic);

            long duration = System.currentTimeMillis() - startTime;

            // 4. Eredmények a felületre
            model.addAttribute("solution", solution);
            model.addAttribute("steps", solution != null ? solution.size() : 0);
            model.addAttribute("time", duration);
            model.addAttribute("message", solution != null ? "Sikeres megoldás! Eredmény elmentve." : "Nincs megoldás a pályára.");

            // 5. Adatbázis mentés
            if (solution != null && username != null && !username.trim().isEmpty()) {
                String cleanUsername = username.trim();

                User user = userRepository.findByUsername(cleanUsername);
                if (user == null) {
                    user = new User(cleanUsername);
                    userRepository.save(user);
                }

                HeuristicSubmission submission = new HeuristicSubmission(user, userCode, solution.size(), duration);
                submissionRepository.save(submission);
            }

        } catch (Exception e) {
            model.addAttribute("error", "Hiba történt a kód fordítása vagy futtatása közben: " + e.getMessage());
        }

        // Form visszaállítása és ranglista frissítése
        model.addAttribute("username", username);
        model.addAttribute("code", userCode);
        model.addAttribute("level", levelData);
        model.addAttribute("leaderboard", submissionRepository.findAllByOrderByStepsToSolveAsc());

        return "index";
    }
}