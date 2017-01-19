package Model;

/**
 * Created by SegFault on 18/01/2017.
 */
public class BlockValue {
    private int biomass, minerals;

    public BlockValue(int biomass_, int minerals_) {
        biomass = biomass_;
        minerals = minerals_;
    }

    public int getValue() {
        return biomass + minerals;
    }
}
