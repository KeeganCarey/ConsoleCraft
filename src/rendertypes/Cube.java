package rendertypes;

import blocks.Block;
import types.*;
import types.comparators.IntegerComparator;

import javax.swing.*;
import java.util.*;

//todo : mayb make a dif thread handle adjacent block face culling

public class Cube {

    //same across all instances
    private static final double MIPMAP_DEPTH = 40; // depth at which rendering gets shortcutted
    private static final float FOCAL_LENGTH = 70; // fov stuff (focal length is distance of viewpoint from viewing plane)   (inversely proportional to fov)
   // private static final
    public static final float DEFAULT_SCALE = 15; // scale of cubes (this is technically half the scale)


    // faces defined in order
    public static final Face[] FACES = new Face[]{ // vertices for each face listed in order either clockwise or counterclockwise (i dont remember lol)
            new Face(0, 4, 6, 2),
            new Face(5, 1, 3, 7),
            new Face(1, 0, 2, 3),
            new Face(4, 5, 7, 6),
            new Face(1, 5, 4, 0),
            new Face(2, 6, 7, 3)
    };
    // lines defined in order
    public static final Line[] LINES = new Line[] { // vertices for each line (not used when in face rendering mode)
            new Line(1,0),
            new Line(2,3),
            new Line(2,0),
            new Line(1,3),
            new Line(5,4),
            new Line(6,7),
            new Line(6,4),
            new Line(7,5),
            new Line(2,6),
            new Line(3,7),
            new Line(0,4),
            new Line(1,5)
    };


    private final Coord[] vertices; // the vertices of the cube
    private final Pair[] vertices2D = new Pair[8]; // the 2D vertices of the cube
    private double depth; // how far (the cube itself is) in relation to the camera
    public final int DISPLAY_W;
    public final int DISPLAY_H;

    public Cube(Coord trans, double depth, int width, int height) {
        this.DISPLAY_W = width;
        this.DISPLAY_H = height;
        this.vertices = new Coord[]{ // default vertices
                new Coord(-DEFAULT_SCALE, -DEFAULT_SCALE, -DEFAULT_SCALE),
                new Coord(-DEFAULT_SCALE, -DEFAULT_SCALE, DEFAULT_SCALE),
                new Coord(-DEFAULT_SCALE, DEFAULT_SCALE, -DEFAULT_SCALE),
                new Coord(-DEFAULT_SCALE, DEFAULT_SCALE, DEFAULT_SCALE),
                new Coord(DEFAULT_SCALE, -DEFAULT_SCALE, -DEFAULT_SCALE),
                new Coord(DEFAULT_SCALE, -DEFAULT_SCALE, DEFAULT_SCALE),
                new Coord(DEFAULT_SCALE, DEFAULT_SCALE, -DEFAULT_SCALE),
                new Coord(DEFAULT_SCALE, DEFAULT_SCALE, DEFAULT_SCALE),
        };

        for (Coord vertex : vertices) {// translates it to its rendering position
            vertex.setX(vertex.getX() + (trans.getX()*DEFAULT_SCALE*2));
            vertex.setY(vertex.getY() - (trans.getY()*DEFAULT_SCALE*2));
            vertex.setZ(vertex.getZ() + (trans.getZ()*DEFAULT_SCALE*2));
        }
        this.depth = depth;
    }

    public double getDepth() {
        return depth;
    }
    public void setDepth(double depth) {this.depth = depth;}


    public HashSet<Pixel> calcDisplay(Block block, Coord decOffset, double sinY, double cosY, double sinX, double cosX, HashSet<Pixel> placedPixels) {
        HashSet<Pixel> pixels = new HashSet<>();// set of pixels that will make up the cube
        //its a hashset so that no duplicates will be plotted

        //if the cube is outside the mipmapping depth, skip full rendering and just plot one point
        if (depth > MIPMAP_DEPTH) {
            pixels.add(new Pixel(depth, block.getFaceTex(), projectCoord(vertices[0])));
            return pixels;
        }

        // projects every vertice into a 2D screenspace coord
        if (!calc2DVertices(decOffset, sinY, cosY, sinX, cosX)) {
            return pixels;
        }

        //skips full rendering of cubes that are smaller than one pixel
        int count = 0;
        for (int i = 1; i<vertices2D.length; i++) {
            if (vertices2D[0] == vertices2D[i]) {
                count++;
            }
        }
        if (count > 2) {
            pixels.add(new Pixel(depth, block.getFaceTex(), vertices2D[0]));
            return pixels;
        }


        //off screen culling
        boolean inBounds = false;
        for (Pair vertex : vertices2D) {
            if ((vertex.getX() <= DISPLAY_W/2 || vertex.getX() >= -DISPLAY_W/2 &&
                    vertex.getY() <= DISPLAY_H/2 || vertex.getY() >= -DISPLAY_H/2) ) { //&& !(placedPixels.contains(new Pixel(depth, block.getFaceTex(), vertices2D[0])))
                inBounds = true; //as long as at least one vertex is in frame its in bounds
                break;
            }
        }
        if (!inBounds) {
            return pixels;
        }

        //stops corner fighting with line drawing
        for (Pair vertex : vertices2D) {
            pixels.add(new Pixel(depth, block.getFaceTex(), vertex));
        }

        //stores the faces that can be seen by the camera
        ArrayList<Face> visibleFaces = new ArrayList<Face>();
        for (Face face : FACES) { // iterates through each face checking its vertices listing order
            double v1x = vertices2D[face.getP1()].getX();
            double v1y = vertices2D[face.getP1()].getY();
            double v2x = vertices2D[face.getP2()].getX();
            double v2y = vertices2D[face.getP2()].getY();
            double v3x = vertices2D[face.getP3()].getX();
            double v3y = vertices2D[face.getP3()].getY();
            //sums edges to find direction of curve
            //sum of all (x2 − x1)(y2 + y1)
            double curve = ((v2x - v1x) * (v2y + v1y)) + ((v3x - v2x) * (v3y + v2y)) + ((v1x - v3x) * (v1y + v3y));
            if (curve < 0) {// if the face is facing towards the camera
                visibleFaces.add(face);
            }
        }

        //send the visible faces to be calculated
        pixels.addAll(calcFaces(visibleFaces, block.getFaceTex(), block.getLineTex()));
        return pixels;
    }

    public Pair projectCoord(Coord coord) {  //projects each coord using ratio of similar triangles
        double zDis = FOCAL_LENGTH + coord.getZ(); // distance from camera to z coord (absv)
        return new Pair((int) Math.round((FOCAL_LENGTH * coord.getX()) / zDis),
                (int) Math.round((FOCAL_LENGTH * coord.getY()) / zDis));
    }

    //todo: fix this with the \ and / stuff
    public ArrayList<Pixel> calcLine(Line line, char[] linTex) { // modified Brezenham line algorithm
        ArrayList<Pixel> lineCoords = new ArrayList<>();// pixels that make up the line

        //lintex array is a preset format from blocks that defines the line textures at certain slopes


        int x1 = vertices2D[line.getStart()].getX();// start x
        int y1 = vertices2D[line.getStart()].getY();// start y

        int x2 = vertices2D[line.getEnd()].getX();// end x
        int y2 = vertices2D[line.getEnd()].getY();// end y

        //double lineDepth = (vertices2D[line.getStart()].getDepth() + vertices2D[line.getEnd()].getDepth())/2;

        // delta of exact value and rounded value of the dependent variable
        int d = 0;

        int dx = Math.abs(x2 - x1);
        int dy = Math.abs(y2 - y1);

        int dx2 = 2 * dx; // slope scaling factors to
        int dy2 = 2 * dy; // avoid floating point

        int ix = x1 < x2 ? 1 : -1; // increment direction
        int iy = y1 < y2 ? 1 : -1;

        int x = x1;
        int y = y1;

        char lineChar = 'E';// init to something, could be anything
        boolean isSlope = false;

        if (dx > dy) {
            while (true) {
                lineCoords.add(new Pixel(depth, linTex[1], x, y));
                if (x == x2)
                    break;
                x += ix;
                d += dy2;
                if (d > dx) {
                    y += iy;
                    d -= dx2;
                }
            }
        } else {
            while (true) {
                if (!isSlope) {
                    lineChar = linTex[2];
                }
                lineCoords.add(new Pixel(depth, lineChar, x, y));
                if (y == y2)
                    break;

                y += iy;
                d += dx2;

                if (d > dy) {
                    lineChar = ix != 1 ? linTex[3] : linTex[0];
                    isSlope = true;
                    x += ix;
                    d -= dy2;
                }
            }
        }

        return lineCoords;
    }

    public boolean calc2DVertices(Coord decOffset, double sinY, double cosY, double sinX, double cosX) {
        double x, y, z;

        for (int i = 0; i < (depth > MIPMAP_DEPTH ? 1 : vertices.length); i++) {
            //Translates the decPos of the coord
            x = vertices[i].getX() + decOffset.getX();
            y = vertices[i].getY() - decOffset.getY();
            z = vertices[i].getZ() + decOffset.getZ();

            //rotates the coord
            Coord transformed = new Coord((x * cosY) + (z * sinY),
                    (y * cosX) - (((z * cosY) - (x * sinY)) * sinX),
                    (y * sinX) + (((z * cosY) - (x * sinY)) * cosX));

            if (transformed.getZ() < -DEFAULT_SCALE) {
                return false;
            }


            vertices2D[i] = projectCoord(transformed);
        }


        return true;
    }

    public ArrayList<Pixel> calcFaces(ArrayList<Face> faces, char fTex, char[] linTex) {
        HashSet<Pixel> facePixels = new HashSet<>();
        HashSet<Line> lines = new HashSet<>();

        for (Face face : faces) {
            lines.add(new Line(face.getP1(), face.getP2()));
            lines.add(new Line(face.getP3(), face.getP4()));
            lines.add(new Line(face.getP2(), face.getP3()));
            lines.add(new Line(face.getP4(), face.getP1()));
        }
        int maxx = Integer.MIN_VALUE;
        int minx = Integer.MAX_VALUE;
        int miny = Integer.MAX_VALUE;
        int maxy = Integer.MIN_VALUE;

        for (Line line : lines) {
            if (vertices2D[line.getStart()].getX() > maxx) {
                maxx = vertices2D[line.getStart()].getX();
            } else if (vertices2D[line.getStart()].getX() < minx) {
                minx = vertices2D[line.getStart()].getX();
            }
            if (vertices2D[line.getStart()].getY() > maxy) {
                maxy = vertices2D[line.getStart()].getY();
            } else if (vertices2D[line.getStart()].getY() < miny) {
                miny = vertices2D[line.getStart()].getY();
            }
            if (vertices2D[line.getEnd()].getX() > maxx) {
                maxx = vertices2D[line.getEnd()].getX();
            } else if (vertices2D[line.getEnd()].getX() < minx) {
                minx = vertices2D[line.getEnd()].getX();
            }
            if (vertices2D[line.getEnd()].getY() > maxy) {
                maxy = vertices2D[line.getEnd()].getY();
            } else if (vertices2D[line.getEnd()].getY() < miny) {
                miny = vertices2D[line.getEnd()].getY();
            }
            facePixels.addAll(calcLine(line, linTex));
        }
        int[][] scanSet = new int[(maxx - minx + 1)][(maxy - miny + 1)];

        for (Pixel pixel : facePixels) {
            scanSet[pixel.getX() - minx][pixel.getY() - miny] = 1;// placeholder to represent presence of something
        }

        int min = 0;
        int max = 0;
        for (int j = 0; j < scanSet[0].length; j++) {
            for (int i = 0; i < scanSet.length; i++) {
                if (scanSet[i][j] == 1) {
                    min = i;
                    break;
                }
            }
            for (int i = 0; i < scanSet.length; i++) {
                if (scanSet[scanSet.length - i - 1][j] == 1) {
                    max = scanSet.length - i - 1;
                    break;
                }
            }
            if (max - min > 1) {
                for (int n = min; n <= max; n++){
                    if (scanSet[n][j] != 1) {
                        scanSet[n][j] = 3;
                    }
                    facePixels.add(new Pixel(depth, fTex, new Pair(n+minx, j+miny)));

                }
            }

        }
        return new ArrayList<>(facePixels);
    }
    public ArrayList<Pixel> calcFace(Face face, char fTex, char[] linTex) {// face is calculated by drawing lines and then using a custom scanline algorithm
        HashSet<Pixel> faceCoords = new HashSet<>();

        faceCoords.addAll(calcLine(new Line(face.getP1(), face.getP2()), linTex));
        faceCoords.addAll(calcLine(new Line(face.getP3(), face.getP4()), linTex));
        faceCoords.addAll(calcLine(new Line(face.getP2(), face.getP3()), linTex));
        faceCoords.addAll(calcLine(new Line(face.getP4(), face.getP1()), linTex));


        int minX = Math.min(Math.min(vertices2D[face.getP1()].getX() ,vertices2D[face.getP2()].getX()),
                            Math.min(vertices2D[face.getP3()].getX() ,vertices2D[face.getP4()].getX()));
        int minY = Math.min(Math.min(vertices2D[face.getP1()].getY() ,vertices2D[face.getP2()].getY()),
                            Math.min(vertices2D[face.getP3()].getY() ,vertices2D[face.getP4()].getY()));

        int maxX = Math.max(Math.max(vertices2D[face.getP1()].getX() ,vertices2D[face.getP2()].getX()),
                            Math.max(vertices2D[face.getP3()].getX() ,vertices2D[face.getP4()].getX()));
        int maxY = Math.max(Math.max(vertices2D[face.getP1()].getY() ,vertices2D[face.getP2()].getY()),
                            Math.max(vertices2D[face.getP3()].getY() ,vertices2D[face.getP4()].getY()));

        int[][] scanSet = new int[maxX-minX+1][maxY-minY+1];

        for (Pixel linePair : faceCoords) {
            scanSet[linePair.getX()-minX][linePair.getY()-minY] = 1;
        }

        ArrayList<Integer> lineSet = new ArrayList<>();
        for (int j = 0; j<scanSet[0].length; j++) {
            lineSet.clear();
            for (int i = 0; i<scanSet.length; i++) {
                if (scanSet[i][j] == 1) {
                    lineSet.add(i);
                }
            }

            if (lineSet.size() > 1) {
                int min = lineSet.stream().min(new IntegerComparator()).get();
                int max = lineSet.stream().max(new IntegerComparator()).get();

                if (max - min > 1) {
                    for (int n = min; n <= max; n++){
                        faceCoords.add(new Pixel(depth, fTex, new Pair(n+minX, j+minY)));
                    }
                }
            }
        }

        return new ArrayList<>(faceCoords);// returns the coords
    }
}

