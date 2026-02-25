import types.Coord;

public class Camera {
    private Coord pos;
    private Coord rot;

    public Camera(Coord pos, Coord rot) {
        this.pos = pos;
        this.rot = rot;
    }

    public Coord getPos() {
        return pos;
    }

    public void setPos(Coord pos) {
        this.pos = pos;
    }

    public Coord getRot() {
        return rot;
    }

    public void setRot(Coord rot) {
        this.rot = rot;
    }

    public void applyVector(Coord coord) {
        pos.addX(coord.getX());
        pos.addY(coord.getY());
        pos.addZ(coord.getZ());
    }

    public void applyRot(Coord coord) {
        rot.addX(coord.getX());
        rot.addY(coord.getY());
        rot.addZ(coord.getZ());
    }

}
