
public class Main {
    public static void main(String[] args) {
        System.setProperty("java.util.Arrays.useLegacyMergeSort", "true"); // change sort mode so my custom comparators work
        Game.main(args);
    }
}