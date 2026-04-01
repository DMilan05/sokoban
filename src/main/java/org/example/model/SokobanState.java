package org.example.model;

import java.util.*;

public class SokobanState {
    private final Position player;
    private final Set<Position> boxes;
    private final Set<Position> walls;
    private final Set<Position> targets;

    public SokobanState(Position player, Set<Position> boxes, Set<Position> walls, Set<Position> targets) {
        this.player = player;
        this.boxes = new HashSet<>(boxes);
        this.walls = walls;
        this.targets = targets;
    }

    public boolean isGoal() {
        return targets.containsAll(boxes) && boxes.containsAll(targets);
    }

    // Alap getterek
    public Set<Position> getBoxes() { return boxes; }
    public Set<Position> getTargets() { return targets; }
    public Position getPlayer() { return player; } // ÚJ: Szükség lehet rá a heurisztikákban

    // --- MAKRÓ OPERÁTOROK LOGIKÁJA ---

    // Egy lehetséges doboztolást leíró rekord
    public record PushMove(String description, SokobanState nextState) {}

    // Megkeressük az összes mezőt, ahova a játékos doboztolás nélkül el tud sétálni
    private Set<Position> getReachablePositions() {
        Set<Position> reachable = new HashSet<>();
        Queue<Position> queue = new LinkedList<>();
        queue.add(player);
        reachable.add(player);

        while (!queue.isEmpty()) {
            Position curr = queue.poll();
            for (Direction d : Direction.values()) {
                Position next = curr.move(d);
                if (!walls.contains(next) && !boxes.contains(next) && !reachable.contains(next)) {
                    reachable.add(next);
                    queue.add(next);
                }
            }
        }
        return reachable;
    }

    // Visszaadjuk az összes szabályos doboztolást
    public List<PushMove> getValidPushes() {
        List<PushMove> moves = new ArrayList<>();
        Set<Position> reachable = getReachablePositions(); // Hova tudunk menni?

        for (Position box : boxes) {
            for (Direction dir : Direction.values()) {
                // Ahhoz, hogy a dobozt 'dir' irányba toljuk, a játékosnak a doboz MÖGÉ kell állnia
                Direction opposite = getOpposite(dir);
                Position standPos = box.move(opposite);
                Position targetPos = box.move(dir);

                // Ha oda tudunk állni a doboz mögé, és a célhely üres (se fal, se doboz)
                if (reachable.contains(standPos) && !walls.contains(targetPos) && !boxes.contains(targetPos)) {

                    Set<Position> newBoxes = new HashSet<>(boxes);
                    newBoxes.remove(box);
                    newBoxes.add(targetPos);

                    // A lépés után a játékos a doboz régi helyére kerül
                    SokobanState nextState = new SokobanState(box, newBoxes, walls, targets);

                    String moveDesc = "Tolás: (" + box.x() + "," + box.y() + ") doboz " + dir + " irányba";
                    moves.add(new PushMove(moveDesc, nextState));
                }
            }
        }
        return moves;
    }

    private Direction getOpposite(Direction d) {
        return switch(d) {
            case UP -> Direction.DOWN;
            case DOWN -> Direction.UP;
            case LEFT -> Direction.RIGHT;
            case RIGHT -> Direction.LEFT;
        };
    }

    // Visszaadja az új állapotot a "buta" keresőkhöz (pl. A*). Ezt megtartottuk kompatibilitás miatt.
    public SokobanState applyMove(Direction dir) {
        Position nextPos = player.move(dir);
        if (walls.contains(nextPos)) return null;

        if (boxes.contains(nextPos)) {
            Position boxNextPos = nextPos.move(dir);
            if (walls.contains(boxNextPos) || boxes.contains(boxNextPos)) return null;

            Set<Position> newBoxes = new HashSet<>(boxes);
            newBoxes.remove(nextPos);
            newBoxes.add(boxNextPos);
            return new SokobanState(nextPos, newBoxes, walls, targets);
        }
        return new SokobanState(nextPos, boxes, walls, targets);
    }

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

    public Set<Position> getWalls() { return walls; }
}