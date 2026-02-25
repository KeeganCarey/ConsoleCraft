import blocks.Block;
import blocks.Stone;
import rendertypes.Cube;
import types.Coord;
import types.Pair;
import types.Pixel;
import types.comparators.CubeDepthComparator;
import types.comparators.PixelDepthComparator;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;

public class Renderer {
    public final int DISPLAY_W;
    public final int DISPLAY_H;

    private final char bg; // the background
    private char[][] frame;


    private Block[] blocks = new Block[2];
    private Cube[][][] renderBox;



    public Renderer(int width, int height, char background, int renDis) {
        this.DISPLAY_W = width;
        this.DISPLAY_H = height;
        this.bg = background;
        this.frame = new char[width][height];
        this.renderBox = new Cube[renDis][renDis][renDis];
    }
    public void init() {
        //initialize different block types and their textures
        //blocks[0] is air so no init
        blocks[1] = new Stone(1, ' ');

        clearFrame();
        Coord origin = new Coord(renderBox.length/2.0, renderBox.length/2.0 ,(renderBox.length/2.0));
        for(int x =0; x<renderBox.length; x++) {
            for(int y =0; y<renderBox.length; y++) {
                for(int z =0; z<renderBox.length; z++) {
                    renderBox[x][y][z] = new Cube(new Coord(
                                                    x-(renderBox.length/2.0),
                                                     y-(renderBox.length/2.0),
                                                      z-(renderBox.length/2.0)),
                            origin.distanceTo(new Coord(x, y, z)), DISPLAY_W, DISPLAY_H);
                }
            }
        }
    }

    private void clearFrame() {
        for (int y = 0; y< frame[0].length; y++) {
            for (int x = 0; x< frame.length; x++) {
                frame[x][y] = bg;
            }
        }
    }

    public void renderFrame(int[][][] box,Camera cam) {


        ArrayList<Cube> cubes = new ArrayList<>();
        for(int x =0; x<renderBox.length; x++) {
            for(int y =0; y<renderBox.length; y++) {
                for(int z =0; z<renderBox.length; z++) {
                    if (box[x][y][z] != 0) {
                        cubes.add(renderBox[x][y][z]);
                    }
                }
            }
        }
        cubes.sort(new CubeDepthComparator());


        double sinX = Math.sin(Math.toRadians(cam.getRot().getX()));
        double cosX = Math.cos(Math.toRadians(cam.getRot().getX()));
        double sinY = Math.sin(Math.toRadians(cam.getRot().getY()));
        double cosY = Math.cos(Math.toRadians(cam.getRot().getY()));


        HashSet<Pixel> pixels = new HashSet<>();
        Coord decOff = new Coord((Math.round(cam.getPos().getX())-cam.getPos().getX())*Cube.DEFAULT_SCALE*2,
                                 (Math.round(cam.getPos().getY())-cam.getPos().getY())*Cube.DEFAULT_SCALE*2,
                                 (Math.round(cam.getPos().getZ())-cam.getPos().getZ())*Cube.DEFAULT_SCALE*2);

        clearFrame();
        for (Cube cube: cubes) {
            pixels.addAll(cube.calcDisplay(blocks[1], decOff, sinY, cosY, sinX, cosX, pixels));
        }


        for (Pixel pixel : pixels) {
            try {
                frame[pixel.getX()+(DISPLAY_W/2)][pixel.getY()+(DISPLAY_H/2)] = pixel.getValue();
            } catch (Exception ignored) {
            }
        }

    }


    public String frameToString() { // concatenates string from frame array
        StringBuilder mrFrameMaker = new StringBuilder();
        for (int y = 0; y< frame[0].length; y++) {
            mrFrameMaker.append("\n");
            for (int x = 0; x< frame.length; x++) {
                mrFrameMaker.append(frame[x][y] + "  ");
            }
        }
        return mrFrameMaker.toString();
    }

}
