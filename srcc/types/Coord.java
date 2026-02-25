package types;

public class Coord {

    private double[] coord;

    public Coord() {
        coord = new double[] {0, 0, 0};
    }
    public Coord(double x, double y, double z) {
        coord = new double[] {x, y, z};
    }

    public double getX() {
        return coord[0];
    }
    public double getY() {
        return coord[1];
    }
    public double getZ() {
        return coord[2];
    }
    public double getW() {
        return coord[3];
    }

    public void setCoord(Coord coord) {
        this.coord[0] = coord.getX();
        this.coord[1] = coord.getY();
        this.coord[2] = coord.getZ();
    }
    public void setX(double x) {
        coord[0] = x;
    }
    public void setY(double y) {
        coord[1] = y;
    }
    public void setZ(double z) {
        coord[2] = z;
    }

    public void addX(double inc) {coord[0] += inc;}
    public void addY(double inc) {coord[1] += inc;}
    public void addZ(double inc) {coord[2] += inc;}

    public void applyVector(Coord vec) {
        coord[0] += vec.getX();
        coord[1] += vec.getY();
        coord[2] += vec.getZ();
    }
    public double distanceTo(Coord p) {
        return Math.sqrt(Math.pow(coord[0] - p.getX(), 2) + Math.pow(coord[1] - p.getY(), 2) + Math.pow(coord[2] - p.getZ(), 2));
    }

    public String toString() {
        return "[x= " + coord[0] + ", y= " + coord[1] + ", z= " + coord[2] + "]";
    }
}
