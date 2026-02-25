package types;

public class Pair {
    private final int x;
    private final int y;

    public Pair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }

    public boolean equals(Pair pos2) {
        return (this.x == pos2.getX() && this.y == pos2.getY());
    }
}
