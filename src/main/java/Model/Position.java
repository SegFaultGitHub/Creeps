package Model;

import lombok.Getter;

/**
 * Created by SegFault on 18/01/2017.
 */
public class Position {
    @Getter
    private int x, y, z;

    public Position(int x_, int y_, int z_) {
        x = x_;
        y = y_;
        z = z_;
    }

    public int distance(Position pos2) {
        return Math.abs(pos2.x - x) + Math.abs(pos2.y - y) + Math.abs(pos2.z - z);
    }

    @Override
    public String toString() {
        return "Position{x=" + x + ", y=" + y + ", z=" + z + "}";
    }

    public void addX(int n) {
        x += n;
    }
    public void addY(int n) {
        y += n;
    }
    public void addZ(int n) {
        z += n;
    }
}
