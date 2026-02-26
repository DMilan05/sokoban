package org.example;

import org.example.model.Position;
import org.example.model.SokobanState;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.HashSet;
import java.util.Set;

@SpringBootApplication
public class SokobanApplication {

    public static void main(String[] args) {
        SpringApplication.run(SokobanApplication.class, args);
    }

    // Visszatettük ide a pályabeolvasó metódusodat!
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
                    case ' ' -> {}
                    default -> {}
                }
            }
        }

        if (player == null) throw new IllegalArgumentException("Nincs játékos a pályán!");
        return new SokobanState(player, boxes, walls, targets);
    }
}