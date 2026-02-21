package org.example;

// SokobanState.java
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

public class SokobanState {
    private final Position player;
    private final Set<Position> boxes;

    // A falak és célok statikusak, elég, ha minden állapot csak referenciát tárol róluk,
    // hogy spóroljunk a memóriával a keresés során.
    private final Set<Position> walls;
    private final Set<Position> targets;

    public SokobanState(Position player, Set<Position> boxes, Set<Position> walls, Set<Position> targets) {
        this.player = player;
        this.boxes = new HashSet<>(boxes); // Másolat, hogy ne módosítsuk az eredetit
        this.walls = walls;
        this.targets = targets;
    }

    // Célállapot vizsgálata
    public boolean isGoal() {
        return targets.containsAll(boxes) && boxes.containsAll(targets);
    }

    // Visszaadja az új állapotot, ha a lépés szabályos. Ha érvénytelen, null-t ad.
    public SokobanState applyMove(Direction dir) {
        Position nextPos = player.move(dir);

        // 1. Falba ütközés
        if (walls.contains(nextPos)) return null;

        // 2. Doboz tolása
        if (boxes.contains(nextPos)) {
            Position boxNextPos = nextPos.move(dir);
            // Ha a doboz mögött fal vagy másik doboz van, nem lehet tolni
            if (walls.contains(boxNextPos) || boxes.contains(boxNextPos)) {
                return null;
            }

            // Új doboz halmaz létrehozása a tolt dobozzal
            Set<Position> newBoxes = new HashSet<>(boxes);
            newBoxes.remove(nextPos);
            newBoxes.add(boxNextPos);
            return new SokobanState(nextPos, newBoxes, walls, targets);
        }

        // 3. Sima lépés (üres mezőre)
        return new SokobanState(nextPos, boxes, walls, targets);
    }

    // Getterek a heurisztikához
    public Set<Position> getBoxes() { return boxes; }
    public Set<Position> getTargets() { return targets; }

    // Az equals és hashCode KÖTELEZŐ a HashSet (Zárt lista) helyes működéséhez!
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        SokobanState that = (SokobanState) o;
        return player.equals(that.player) && boxes.equals(that.boxes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(player, boxes);
    }
}
