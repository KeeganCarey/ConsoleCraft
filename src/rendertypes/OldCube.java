package rendertypes;

import blocks.Block;

import types.*;

import java.text.FieldPosition;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;

//todo : mayb make a dif thread handle adjacent block face culling

public class OldCube {

    //same across all instances
    private static final double MIPMAP_DEPTH = 40; // depth at which rendering gets shortcutted
    private static final float FOCAL_LENGTH = 70; // fov stuff (focal length is distance of viewpoint from viewing plane)   (inversely proportional to fov)

    public static final float DEFAULT_SCALE = 5; // scale of cubes (this is technically half the scale)

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
    public static final Line[] LINES = new Line[] { // vertices for each line
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


    private Coord[] vertices; // the vertices of the cube
    private Pair[] vertices2D = new Pair[8]; // the 2D vertices of the cube
    private double depth; // how far (the cube itself is) in relation to the camera




    public OldCube(double depth) {
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
        this.depth = depth;
    }

    public double getDepth() {
        return depth;
    }
    public void setDepth(double depth) {this.depth = depth;}

    public void calc2DVertices() {// iterates through each vertex and projects it to 2D
        for (int i = 0; i < vertices.length; i++) {
            vertices2D[i] = projectCoord(vertices[i]);
        }
    }

    public Pair projectCoord(Coord coord) {  //projects each coord using ratio of similar triangles
        double zDis = FOCAL_LENGTH + coord.getZ(); // distance from camera to z coord (absv)
        return new Pair((int) Math.round((FOCAL_LENGTH * coord.getX()) / zDis),
                (int) Math.round((FOCAL_LENGTH * coord.getY()) / zDis));
    }

    public boolean newCalc2DVertices(Coord decOffset, double sinY, double cosY, double sinX, double cosX) {
        double x, y, z;


        int projVertices = depth > MIPMAP_DEPTH ? 1 : vertices.length;

        for (int i = 0; i < projVertices; i++) {
            x = vertices[i].getX() + decOffset.getX();
            y = vertices[i].getY() - decOffset.getY();
            z = vertices[i].getZ() + decOffset.getZ();

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





    //todo rewrite for this implementation
    /*public boolean isInDisplay() {
        for (Pair vertex2D : vertices2D) {
            if (!(vertex2D.getX() <= MAX_X && vertex2D.getX() >= MIN_X && vertex2D.getY() <= MAX_Y && vertex2D.getY() >= MIN_Y)) {
                return false;
            }
        }
        return true;
    }*/

    public boolean isBehind() { // returns true if any vertices are reasonably behind viewing plane
        for (Coord vertex : vertices) {
            if ((vertex.getZ() < -DEFAULT_SCALE)) {
                return true;
            }
        }
        return false;
    }



    public HashSet<Pixel> calcDisplay(Block block, Coord decOffset, double sinY, double cosY, double sinX, double cosX) {
        HashSet<Pixel> pixels = new HashSet<>();// set of pixels that will make up the cube
        //its a hashset so that no duplicates will be plotted

        /*Coord[] temp = new Coord[] {new Coord(), new Coord(), new Coord(), new Coord(), new Coord(), new Coord(), new Coord(), new Coord()};


        for (int i = 0; i<vertices.length; i++) {
            temp[i].setCoord(vertices[i]);
        }


        translate(decOffset.getX()*DEFAULT_SCALE*2, decOffset.getY()*DEFAULT_SCALE*2, decOffset.getZ()*DEFAULT_SCALE*2); // translates the cube to fit cam decimal pos
        rotate(sinY, cosY, sinX, cosX);// rotates the cube


        //check if is behind viewport
        if (isBehind()) {
            for (int i = 0; i<vertices.length; i++) {
                vertices[i] = temp[i];
            }
            return pixels; // returns empty if its behind viewingplane
        }*/


        /*if (depth > MIPMAP_DEPTH) { // just projects one pix if past mipmap depth but within render distance
            pixels.add(new Pixel(depth, block.getFaceTex(), projectCoord(vertices[0])));

            return pixels;
        }*/

        // turns vertices to 2D
        if (!newCalc2DVertices(decOffset, sinY, cosY, sinX, cosX)) {
            return pixels;
        }
        if (depth > MIPMAP_DEPTH) {
            pixels.add(new Pixel(depth, block.getFaceTex(), vertices2D[0]));

            return pixels;
        }



        ArrayList<Face> onScreenFaces = new ArrayList<>();// stores faces to be rendered (faces not in this are culled)
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
            if (curve < 0) {
                onScreenFaces.add(face);
            }
        }

        //Depracated
        //ArrayList<Line> onScreenLines = findOnScreenLines(onScreenFaces);// on screen lines can be found from the on screen faces
        //pixels.addAll(calcLines(onScreenLines, block.getLineTex())); // depracated as the lines can be rendered from the face itself


        pixels.addAll(calcFaces(onScreenFaces, block.getFaceTex(), block.getLineTex()));// calculates the face


        /*for (int i = 0; i<vertices.length; i++) {
            vertices[i] = temp[i];
        }*/

        return pixels;
    }

    public ArrayList<Line> findOnScreenLines(ArrayList<Face> onScreenFaces) {
        ArrayList<Line> onScreenLines = new ArrayList<>();

        for (Line line : LINES) {
            int start = line.getStart();
            int end = line.getEnd();

            for (Face face : onScreenFaces) {
                int v1 = face.getP1();
                int v2 = face.getP2();
                int v3 = face.getP3();
                int v4 = face.getP4();

                if ((start == v1 || start == v2 || start == v3 || start == v4) && (end == v1 || end == v2 || end == v3 || end == v4)) {
                    onScreenLines.add(line);
                    break;
                }
            }
        }

        return onScreenLines;
    }


    public void translate(double x, double y, double z) {
        for (Coord vertex : vertices) {
            vertex.setX(vertex.getX() + x);
            vertex.setY(vertex.getY() - y); // fr some reason it was goin opposite so i made it - here
            vertex.setZ(vertex.getZ() + z);
        }
    }



    public void rotate(double sinY, double cosY, double sinX, double cosX) {
        double x;
        double y;
        double z;

        //X = x cos 0 - Y sin 0
        //Y = x sin0 + Y cos 0

        if (sinY != 0) {

            for (Coord vertex : vertices) {

                x = vertex.getX();
                z = vertex.getZ();

                vertex.setX((x * cosY) + (z * sinY));
                vertex.setZ((z * cosY) - (x * sinY));
            }
        }

        if (sinX != 0) {

            for (Coord vertex : vertices) {
                y = vertex.getY();
                z = vertex.getZ();

                vertex.setY((y * cosX) - (z * sinX));
                vertex.setZ((y * sinX) + (z * cosX));
            }


        }



    }



    public ArrayList<Pixel> calcFace(Face face, char fTex, char[] linTex) {// face is calculated by drawing lines between sets of parallel lines on the face
        HashSet<Pixel> lineCoords = new HashSet<>();

        //renders and saves one set of parallel lines from the face
        ArrayList<Pixel> lineStarts = new ArrayList<>(calcLine(new Line(face.getP1(), face.getP2()), linTex));// 1-2
        ArrayList<Pixel> lineEnds = new ArrayList<>(calcLine(new Line(face.getP3(), face.getP4()), linTex)); // 3-4
        lineCoords.addAll(lineStarts);
        lineCoords.addAll(lineEnds);

        //renders the other lines
        lineCoords.addAll(calcLine(new Line(face.getP2(), face.getP3()), linTex));
        lineCoords.addAll(calcLine(new Line(face.getP4(), face.getP1()), linTex));


        //todo: implement scanlines
        int length = Math.min(lineEnds.size(), lineStarts.size());
        /*for (int i = 0; i < length; i++) {

            lineCoords.addAll(calcFaceLine(lineStarts.get(i), lineEnds.get(Math.abs(lineEnds.size() - i - 1)), fTex));
            lineCoords.addAll(calcFaceLine(lineEnds.get(i), lineStarts.get(Math.abs(lineStarts.size() - i - 1)), fTex));
        }*/


        //connects each point on the parrallel lines with a line using the face texture //todo: this is slow so I need to fix this
        for (Pixel lineEnd : lineEnds) {
            for (Pixel lineStart : lineStarts) {
                lineCoords.addAll(calcFaceLine(lineStart, lineEnd, fTex));
            }
        }

        return new ArrayList<>(lineCoords);// returns the coords
    }

    public ArrayList<Pixel> calcFaceLine(Pixel start, Pixel end, char tex) {
        ArrayList<Pixel> lineCoords = new ArrayList<>();

        int x1 = start.getX();
        int y1 = start.getY();

        int x2 = end.getX();
        int y2 = end.getY();

        //double lineDepth = (start.getDepth() + end.getDepth())/2;
        double lineDepth = depth;

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

        if (dx >= dy) {
            while (true) {
                lineCoords.add(new Pixel(depth, tex, x, y));
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
                lineCoords.add(new Pixel(depth, tex, x, y));
                if (y == y2)
                    break;
                y += iy;
                d += dx2;
                if (d > dy) {
                    x += ix;
                    d -= dy2;
                }
            }
        }

        return lineCoords;
    }


    //todo: fix this with the \ and / stuff
    public ArrayList<Pixel> calcLine(Line line, char[] linTex) {
        ArrayList<Pixel> lineCoords = new ArrayList<>();

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

    public ArrayList<Pixel> calcFaces(ArrayList<Face> onScreen, char fTex, char[] linTex) {
        ArrayList<Pixel> faceCoords = new ArrayList<>();
        for (Face face : onScreen) {
            faceCoords.addAll(calcFace(face, fTex, linTex));
        }
        return faceCoords;
    }


    public ArrayList<Pixel> calcLines(ArrayList<Line> onScreen, char[] linTex) { // this is for wireframe rendering so its not in use rn
        ArrayList<Pixel> wireframeCoords = new ArrayList<>();

        for (Line line : onScreen) {
            wireframeCoords.addAll(calcLine(line, linTex));
        }
        return wireframeCoords;
    }


}

