package types;

import java.util.Objects;

public class Pixel {


    private double depth;
    private char value;
    private final int x;
    private final int y;


    public void setValue(char value) {
        this.value = value;
    }

    public Pixel(double depth, char value, Pair pos) {
        this.depth = depth;
        this.value = value;
        this.x = (int)Math.round(pos.getX());
        this.y = (int)Math.round(pos.getY());
    }
    public Pixel(double depth, char value, int x, int y) {
        this.depth = depth;
        this.value = value;
        this.x = x;
        this.y = y;
    }


    public Pair getPos() {
        return new Pair(x, y);
    }
    public int getX() {
        return x;
    }
    public int getY() {
        return y;
    }
    public double getDepth() {
        return depth;
    }
    public char getValue() {
        return value;
    }

    public void setDepth(double depth) {
        this.depth = depth;
    }

    @Override
    public int hashCode() {
        return Objects.hash(x, y);
    }

    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Pixel c)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members

        // Compare the data members and return accordingly
        return Double.compare(x, c.x) == 0
                && Double.compare(y, c.y) == 0;
    }
}

