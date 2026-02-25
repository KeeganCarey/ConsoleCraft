import types.Coord;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.ArrayList;

public class GameUI extends JFrame implements KeyListener {

    JTextArea disp = new JTextArea(); // this is what displays the frame string
    JPanel panel = new JPanel();




    private final Camera cam; // the players camera (pos and rot)
    private int speedMult = 1;
    private ArrayList<Coord> viewBlocks = new ArrayList<>();
    private boolean breakAction;


    public GameUI(String name, int fontSize) {
        super(name);
        cam = new Camera(new Coord(50, 60, 50), new Coord()); // starting location and rotation (empty Coord constructor is 0, 0, 0)
        //rot is stored as rotation around an axis so (rotX, rotY, rotZ)

        //disp.setColumns(600);//set dimensions
        //disp.setRows(400);//set dimensions


        disp.setFont(new Font("MONOSPACED", Font.PLAIN, fontSize)); //set size and font(monospaced makes all characters == size so its easier to render) (default 5 size)
        disp.setDoubleBuffered(true);

        disp.setForeground(new Color(255, 255, 255)); //white text
        disp.setBackground(new Color(0, 0, 0)); // black background
        disp.setEditable(false);// its just for display so I dont want editing

        disp.addKeyListener(this);//keylistener
        disp.setFocusable(true);//helps with keylistener

        panel.add(disp);//add it in a panel for formatting
        add(panel);

        disp.setVisible(true);
        setVisible(true);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }



    public void setDisp(String frame) {
        disp.setText(frame); // applies framestring to display
    }

    public Camera getCam() {
        return cam;
   } // for getting the camera

    public boolean isBreakAction() {return breakAction;}
    public ArrayList<Coord> getViewBlocks() {breakAction = false; return viewBlocks;}




    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        Coord vector = new Coord(); // vector for movement
        // movement is stored as a vector that can then be rotated based on cam rot
        // this allows movement to be relative to the facind direction of the player
        // ex: this means that facing left, the forward key would still move forward from the players perspective


        if (e.getKeyCode() == KeyEvent.VK_W) {
            vector.addZ(0.2*speedMult);
        }
        if (e.getKeyCode() == KeyEvent.VK_S) {
            vector.addZ(-0.2*speedMult);
        }
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            vector.addY(0.2*speedMult);
        }
        if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            vector.addY(-0.2*speedMult);
        }
        if (e.getKeyCode() == KeyEvent.VK_A) {
            vector.addX(-0.2*speedMult);
        }
        if (e.getKeyCode() == KeyEvent.VK_D) {
            vector.addX(0.2*speedMult);
        }


        if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            cam.getRot().addY(1.0*speedMult);
        }
        if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            cam.getRot().addY(-1.0*speedMult);
        }
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            cam.getRot().addX(-1.0*speedMult);
        }
        if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            cam.getRot().addX(1.0*speedMult);
        }

        //this rotates the vector based on yaw (rotY)
        double x = vector.getX();
        double z = vector.getZ();
        double sinY = -Math.sin(Math.toRadians(cam.getRot().getY())); // idk why but this needs to be inverted with a -
        //now that i think about it, it probably has something to do with diff coord systems between renderboxes and the world
        double cosY = Math.cos(Math.toRadians(cam.getRot().getY()));

        vector.setX((x * cosY) + (z * sinY));//rotates the vector so that it goes forward relative to the cameras facing direction
        vector.setZ((z * cosY) - (x * sinY));

        cam.applyVector(vector);// adds the vector to the camera



        if (e.getKeyCode() == KeyEvent.VK_B) {
            breakAction = true;
            Coord rayVec = new Coord();
            double sinX = Math.sin(Math.toRadians(cam.getRot().getX()));
            double cosX = Math.cos(Math.toRadians(cam.getRot().getX()));


            rayVec.setX((1 * sinY));//rotates the vector so that it goes forward relative to the cameras facing direction
            rayVec.setZ((1 * cosY));

            double rayX = rayVec.getX();
            rayVec.setX((rayX * cosX));
            rayVec.setY((rayX * sinX));

            double newX, newY, newZ;
            newX = cam.getPos().getX();
            newY = cam.getPos().getY();
            newZ = cam.getPos().getZ();

            for (int i = 0; i < 7; i++) {
                newX += rayVec.getX();
                newY += rayVec.getY();
                newZ += rayVec.getZ();

                viewBlocks.add(new Coord(Math.floor(newX), Math.floor(newY), Math.floor(newZ)));
            }


        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_CONTROL) {
            speedMult = speedMult == 1 ? 4:1;
        }

        if (e.getKeyCode() == KeyEvent.VK_H) {

        }
    }




}
