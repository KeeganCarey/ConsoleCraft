package types.comparators;

import types.Pixel;

import java.util.Comparator;

public class PixelDepthComparator implements Comparator<Pixel> {
    public int compare(Pixel objpixel1, Pixel objpixel2) {
        return (int)(objpixel1.getDepth() - objpixel2.getDepth());
    }
}
