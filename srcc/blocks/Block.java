package blocks;

public class Block {
    private final int ID;
    private final char FACE_TEX;
    private final char[] LINE_TEX;// in order   [ \, -, |, /]  (slope left, horizontal, vertical, slope right)



    public Block(int id, char faceTex) {
        this.ID = id;
        this.FACE_TEX = faceTex;
        this.LINE_TEX = new char[] {'\\', '-', '|', '/'};
    }
    public Block(int id, char faceTex, char[] linTex) {
        this.ID = id;
        this.FACE_TEX = faceTex;
        this.LINE_TEX = linTex;
    }

    public int getID() {
        return ID;
    }

    public char getFaceTex() {
        return FACE_TEX;
    }

    public char[] getLineTex() {
        return LINE_TEX;
    }
    public char getSlopeL() {
        return LINE_TEX[0];
    }
    public char getSlopeR() {
        return LINE_TEX[3];
    }
    public char getLineH() {
        return LINE_TEX[1];
    }
    public char getLineV() {
        return LINE_TEX[2];
    }
}
