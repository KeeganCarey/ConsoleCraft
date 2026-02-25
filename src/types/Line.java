package types;

public class Line {
    private final int start;
    private final int end;

    public Line(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
    }

    @Override
    public boolean equals(Object o) {
        // If the object is compared with itself then return true
        if (o == this) {
            return true;
        }

        /* Check if o is an instance of Complex or not
          "null instanceof [type]" also returns false */
        if (!(o instanceof Line c)) {
            return false;
        }

        // typecast o to Complex so that we can compare data members

        // Compare the data members and return accordingly
        return Double.compare(start, c.getStart()) == 0
                && Double.compare(end, c.getEnd()) == 0;
    }
}
