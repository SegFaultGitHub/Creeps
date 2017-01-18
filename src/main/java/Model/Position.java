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

    @Override
    public String toString() {
        return "Position{x=" + x + ", y=" + y + ", z=" + z + "}";
    }
}
