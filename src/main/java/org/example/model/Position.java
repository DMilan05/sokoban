package org.example.model;

public record Position(int x, int y) {
    public Position move(Direction d) {
        return new Position(this.x + d.getDx(), this.y + d.getDy());
    }
}