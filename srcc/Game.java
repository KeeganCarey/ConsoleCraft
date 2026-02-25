

public class Game {

    //dim preset for JFrame(Monospaced, plain, size5)
    public static final int JFRAME_W_PRESET = 214;
    public static final int JFRAME_H_PRESET = 144; // use fontsize 5


    //dim preset for big JFrame (larger screen and fov)
    public static final int BJFRAME_W_PRESET = 320;
    public static final int BJFRAME_H_PRESET = 252; // use fontsize 3


    //dim preset for Console (size 4, lineheight 1, 126kb buffer)
    public static final int CONSOLE_W_PRESET = 250; //old 300
    public static final int CONSOLE_H_PRESET = 120; //old 145

    public static final int RENDER_DISTANCE = 100;
    public static void main(String[] args) {


        GameUI ui = new GameUI("ConsoleCraftlol", 5); // The JFrame for the game itself
        //it handles input events and is able to display the rendered frames
        //since it handles input events, the camera class resides within (Camera stores the pos and rotation)


        //todo: block texturing is implemented, i just need to connect it but im too lazy rn
        Renderer ren = new Renderer(JFRAME_W_PRESET, JFRAME_H_PRESET, ' ', RENDER_DISTANCE);//this class handles the rendering for the game
        //It creates a set of cubes that will be rendered based on a set of world-data passed in from World class
        //These cubes are then rendered in order nearest to camera first and then the coords of the rendered cubes are added to the frame array
        //the frame array is then converted into a string with the frameToString() function which can then be used to output the frame
        //since it outputs as a string, the frame may be used in JTextArea or printed directly to console

        World dim1 = new World(1000, 100, RENDER_DISTANCE);// this class stores the world
        //the world it stored in a 3D array of numbers. Each number corresponds to a block type which the renderer uses to render textures
        //the world is stored like this to save RAM so that only rendered blocks have a full block class loaded (within the Renderer class)
        //a renderbox is returned based on the position of the player and the render distance. This renderbox is the renderable set in the form of blocktypes (the numbers)
        //the renderer can then use these numbers to tell each block what to render in the appearence of

        //unimplemented
        //world has a renderable set array which I plan to implement later
        //It will create a world set that only includes blocks that have an exposed face
        //this will then be used to create the renderBox and help with performance with large amounts of blocks
        //the renderable set can then be updated each time a block interaction is preformed (removal or addition of a block)


        dim1.generateFromNoisemap("C:\\Users\\fired\\Desktop\\ConsoleCraft\\src\\noisemaps\\terrain_noise_1.jpg"); //todo: CHANGE THIS PATH WHEN RUNNING ON DIF COMPUTER
        //the world is also able to generate from a noisemap
        //it just corresponds the brightness of the pixel to the height(y) to the coord of the pixel to the x and y

        ren.init();// fills the rendering array with rendering cubes


        for (int i = 0; i<= 1000000; i++) {// i set it to end after 1m frames as a failsafe


            dim1.extractRenderbox(ui.getCam().getPos());//calculates the renderbox within the world class
            ren.renderFrame(dim1.getRenderBox(), ui.getCam());//renders the frame with the renderbox and Cam location (pos & rot)

            if (ui.isBreakAction()) {
                dim1.breakAction(ui.getViewBlocks());
            }

            //literally just switch which one is commented to switch render modes lol
            //ui.setDisp(ren.frameToString());// sets the ui display to the rendered frame as a string
            System.out.println(ren.frameToString()); // sets the console display to the rendered frame as a string

        }
    }
}