import types.Coord;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.util.ArrayList;

public class World {
    // world is stored as an int array to save memory
    // each int in the array represents a different blockID
    private int[][][] dim1;
    private int[][][] renderableSet; //todo implement this to hide fully occluded blocks // also make checkIsRenderable method
    //todo basically cull the inside faces as if its all one big mesh
    private int[][][] renBox;

    public World(int maxSize, int maxHeight, int renDis) {
        this.dim1 = new int[maxSize][maxHeight][maxSize];
        this.renBox = new int[renDis][renDis][renDis];
    }


    public void extractRenderbox(Coord camPos) {
        int halfRenDis = renBox.length/2;
        int camX =(int)Math.round(camPos.getX());
        int camY =(int)Math.round(camPos.getY());
        int camZ =(int)Math.round(camPos.getZ());
        for (int x = camX-halfRenDis; x < camX+halfRenDis; x++) {
            for (int y = camY-halfRenDis; y < camY+halfRenDis; y++) {
                for (int z = camZ-halfRenDis; z < camZ+halfRenDis; z++) {
                    try {
                        renBox[x-(camX-halfRenDis)][y-(camY-halfRenDis)][z-(camZ-halfRenDis)] = dim1[x][y][z];
                    } catch (Exception ignored) {
                        assert renBox != null;
                        renBox[x-(camX-halfRenDis)][y-(camY-halfRenDis)][z-(camZ-halfRenDis)] = 0;
                    }

                }

            }
        }
    }
    public int[][][] getRenderBox() {
        return renBox;
    }

    public void breakBlock(Coord blockPos) {
        dim1[(int)blockPos.getX()][(int)blockPos.getY()][(int)blockPos.getZ()] = 0;
        //todo update surrrounding blocks
    }
    public void breakAction(ArrayList<Coord> viewBlocks) {
        for (Coord block : viewBlocks) {
            if (dim1[(int)block.getX()][(int)block.getY()][(int)block.getZ()] != 0) {
                breakBlock(block);
                break;
            }
        }

    }

    public void generateFromNoisemap(String path) {// really simple generation from noisemap idk what else to say
        try {
            BufferedImage img = ImageIO.read(new File(path));
            double pixval;
            for (int i = 0; i < dim1.length; i++) {
                for (int j = 0; j < dim1[0].length; j++) {
                    Color pixcol = new Color(img.getRGB(j, i));
                    pixval = (((pixcol.getRed() * 0.30) + (pixcol.getBlue() * 0.59) + (pixcol
                            .getGreen() * 0.11)));

                    int height = (int)((pixval/255) * dim1[0].length);
                    dim1[j][height][i] = 1;
                }
            }
        } catch(Exception e){
            e.printStackTrace();
        }
    }
}
