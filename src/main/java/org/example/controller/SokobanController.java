package org.example.controller;

import org.example.SokobanApplication;
import org.example.model.IDAStarSearch;
import org.example.model.SokobanHeuristic;
import org.example.model.SokobanState;
import org.example.model.Position; // ÚJ import a heurisztikához
import org.example.model.User;
import org.example.model.HeuristicSubmission;
import org.example.repository.HeuristicSubmissionRepository;
import org.example.repository.UserRepository;
import org.example.service.DynamicCompilerService;
import org.example.service.LevelService;
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
    private final LevelService levelService;

    public SokobanController(DynamicCompilerService compilerService,
                             UserRepository userRepository,
                             HeuristicSubmissionRepository submissionRepository,
                             LevelService levelService) {
        this.compilerService = compilerService;
        this.userRepository = userRepository;
        this.submissionRepository = submissionRepository;
        this.levelService = levelService;
    }

    @GetMapping("/")
    public String index(Model model) {
        // Fejlettebb alapértelmezett kód
        String defaultCode = """
                public int heur(SokobanState state) {
                     int totalDistance = 0;
                    \s
                     // Másolatot csinálunk a célokról, hogy törölni tudjuk, amit már "lefoglalt" egy doboz
                     java.util.List<Position> availableTargets = new java.util.ArrayList<>(state.getTargets());
                     java.util.Set<Position> walls = state.getWalls();
                     java.util.Set<Position> targets = state.getTargets();
                 
                     for (Position box : state.getBoxes()) {
                         if (targets.contains(box)) {
                             availableTargets.remove(box);
                             continue; // Ha már célon van, kipipáljuk
                         }
                 
                         // SAROK-DEADLOCK ELLENŐRZÉS
                         boolean wallUp = walls.contains(new Position(box.x(), box.y() - 1));
                         boolean wallDown = walls.contains(new Position(box.x(), box.y() + 1));
                         boolean wallLeft = walls.contains(new Position(box.x() - 1, box.y()));
                         boolean wallRight = walls.contains(new Position(box.x() + 1, box.y()));
                 
                         if ((wallUp || wallDown) && (wallLeft || wallRight)) {
                             return 100000; // Végtelen költség -> Sarok csapda!
                         }
                 
                         // MOHÓ PÁROSÍTÁS (Keresünk egy szabad célt, és lefoglaljuk)
                         int minDistance = Integer.MAX_VALUE;
                         Position bestTarget = null;
                        \s
                         for (Position target : availableTargets) {
                             int dist = Math.abs(box.x() - target.x()) + Math.abs(box.y() - target.y());
                             if (dist < minDistance) {
                                 minDistance = dist;
                                 bestTarget = target;
                             }
                         }
                        \s
                         if (bestTarget != null) {
                             totalDistance += minDistance;
                             availableTargets.remove(bestTarget); // Lefoglalva! A többi doboz keressen mást.
                         }
                     }
                     return totalDistance;
                 }
                """;

        model.addAttribute("code", defaultCode);
        model.addAttribute("level", "######\n#@ $.#\n######\n");
        model.addAttribute("availableLevels", levelService.getAllLevels());
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
            SokobanHeuristic customHeuristic = compilerService.compileAndInstantiate(userCode, "UserHeuristic");
            SokobanState initialState = SokobanApplication.parseLevel(levelData);

            IDAStarSearch searcher = new IDAStarSearch();
            // JAVÍTÁS: List<String> a List<Direction> helyett!
            List<String> solution = searcher.search(initialState, customHeuristic);

            long duration = System.currentTimeMillis() - startTime;

            model.addAttribute("solution", solution);
            model.addAttribute("steps", solution != null ? solution.size() : 0);
            model.addAttribute("time", duration);
            model.addAttribute("message", solution != null ? "Sikeres megoldás! Eredmény elmentve." : "Nincs megoldás a pályára.");

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

        model.addAttribute("username", username);
        model.addAttribute("code", userCode);
        model.addAttribute("level", levelData);
        model.addAttribute("leaderboard", submissionRepository.findAllByOrderByStepsToSolveAsc());
        model.addAttribute("availableLevels", levelService.getAllLevels());

        return "index";
    }
}