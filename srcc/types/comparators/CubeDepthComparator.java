package types.comparators;

import rendertypes.Cube;

import java.util.Comparator;

public class CubeDepthComparator implements Comparator<Cube> {
    public int compare(Cube cube1, Cube cube2) {
        return (int) (100*(cube1.getDepth()*1 - cube2.getDepth()));
        //return (cube1.getDepth() - cube2.getDepth()) >0 ? 1 : (cube1.getDepth() - cube2.getDepth()) <0 ? -1 : 0;
        // reformatted to work better with doubles
    }
}
