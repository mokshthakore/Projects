import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.InputMismatchException;
import java.util.Scanner;

/**
 * The ImageEditor class provides methods to read, process, and write PPM images.
 * Supported operations include invert colors, high contrast, and grayscale.
 * This class is used as a command-line utility to apply these operations.
 * 
 * Usage: java ImageEditor {-I|-H|-G} infile outfile
 * 
 * @author Moksh Thakore
 */
public class ImageEditor {

    /**
     * Main method to process a PPM file based on a specified operation.
     * Usage: java -cp bin ImageEditor {-I|-H|-G} infile outfile
     *
     * @param args command-line arguments: operation flag, input file, output file
     */
    public static void main(String[] args) {
        if (args.length != 3) {
            System.out.println("Usage: java -cp bin ImageEditor {-I|-H|-G} infile outfile");
            return;
        }

        String flag = args[0];
        String inputFileName = args[1];
        String outputFileName = args[2];

        // Check if the flag is valid
        if (!flag.equals("-I") && !flag.equals("-H") && !flag.equals("-G")) {
            System.out.println("Usage: java -cp bin ImageEditor {-I|-H|-G} infile outfile");
            return;
        }

        // Validate file extensions
        if (!inputFileName.endsWith(".ppm")) {
            System.out.println("Invalid input file extension");
            return;
        }

        if (!outputFileName.endsWith(".ppm")) {
            System.out.println("Invalid output file extension");
            return;
        }

        // Check input file accessibility
        File inputFile = new File(inputFileName);
        if (!inputFile.exists() || !inputFile.canRead()) {
            System.out.println("Unable to access input file: " + inputFileName);
            return;
        }

        // Confirm overwriting if output file exists
        File outputFile = new File(outputFileName);
        if (outputFile.exists()) {
            System.out.print(outputFileName + " exists - OK to overwrite(y,n)?: ");
            Scanner sc = new Scanner(System.in);
            String response = sc.nextLine();
            if (!response.toLowerCase().startsWith("y")) {
                sc.close();
                return;
            }
            sc.close();
        }

        int[][] pixels;
        try (Scanner in = new Scanner(inputFile)) {
            pixels = getPixelValues(in);
            if (pixels == null) {
                System.out.println("Invalid input file");
                return;
            }
        } catch (FileNotFoundException e) {
            System.out.println("Invalid input file");
            return;
        }

        try {
            switch (flag) {
                case "-I":
                    invert(pixels);
                    break;
                case "-H":
                    highContrast(pixels);
                    break;
                case "-G":
                    greyScale(pixels);
                    break;
                default:
                    throw new IllegalArgumentException("Unknown flag: " + flag);
            }
        } catch (IllegalArgumentException e) {
            System.out.println(e.getMessage());
            return;
        }

        try (PrintWriter out = new PrintWriter(outputFile)) {
            outputPPM(out, pixels);
        } catch (FileNotFoundException e) {
            System.out.println("Cannot create output file");
        }
    }

    /**
     * Reads and validates a PPM file, returning a 2D array of pixel RGB values.
     * 
     * @param in Scanner object for the input PPM file
     * @return 2D array of RGB pixel values, or null if the file is invalid
     */
    public static int[][] getPixelValues(Scanner in) {
        if (in == null) {
            throw new IllegalArgumentException("Null file");
        }

        try {
            String format = in.next();
            if (!format.equals("P3")) {
                return null;
            }

            if (!in.hasNextInt()) return null;
            int cols = in.nextInt();

            if (!in.hasNextInt()) return null;
            int rows = in.nextInt();

            if (cols <= 0 || rows <= 0) return null;

            if (!in.hasNextInt()) return null;
            int maxColor = in.nextInt();
            if (maxColor != 255) return null;

            int[][] pixels = new int[rows][cols * 3];
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < cols * 3; j++) {
                    if (!in.hasNextInt()) return null;
                    int value = in.nextInt();
                    if (value < 0 || value > 255) return null;
                    pixels[i][j] = value;
                }
            }
            return pixels;
        } catch (InputMismatchException e) {
            System.out.println("Error: Invalid input format in PPM file.");
            return null;
        }
    }

    /**
     * Inverts each RGB value in the pixel array.
     *
     * @param pixels 2D array of RGB pixel values
     */
    public static void invert(int[][] pixels) {
        validatePixelArray(pixels);
        for (int[] row : pixels) {
            for (int i = 0; i < row.length; i++) {
                row[i] = 255 - row[i];
            }
        }
    }

    /**
     * Converts each RGB value in the pixel array to high contrast.
     *
     * @param pixels 2D array of RGB pixel values
     */
    public static void highContrast(int[][] pixels) {
        validatePixelArray(pixels);
        for (int[] row : pixels) {
            for (int i = 0; i < row.length; i++) {
                row[i] = row[i] < 128 ? 0 : 255;
            }
        }
    }

    /**
     * Converts each RGB value in the pixel array to grayscale.
     *
     * @param pixels 2D array of RGB pixel values
     */
    public static void greyScale(int[][] pixels) {
        validatePixelArray(pixels);
        for (int[] row : pixels) {
            for (int i = 0; i < row.length; i += 3) {
                int average = (row[i] + row[i + 1] + row[i + 2]) / 3;
                row[i] = row[i + 1] = row[i + 2] = average;
            }
        }
    }

    /**
     * Writes the pixel data to an output file in PPM format.
     *
     * @param out PrintWriter object for the output PPM file
     * @param pixels 2D array of RGB pixel values
     */
    public static void outputPPM(PrintWriter out, int[][] pixels) {
        if (out == null) throw new IllegalArgumentException("Null file");
        validatePixelArray(pixels);

        int rows = pixels.length;
        int cols = pixels[0].length / 3;
        out.println("P3");
        out.println(cols + " " + rows);
        out.println("255");

        for (int[] row : pixels) {
            for (int j = 0; j < row.length; j++) {
                out.print(row[j]);
                if (j < row.length - 1) out.print(" ");
            }
            out.println();
        }
    }

    /**
     * Validates that the pixel array is not null, rectangular, and has values in multiples of 3.
     *
     * @param pixels 2D array of RGB pixel values
     */
    private static void validatePixelArray(int[][] pixels) {
        if (pixels == null) throw new IllegalArgumentException("Null array");
        int length = pixels[0].length;
        if (length % 3 != 0) throw new IllegalArgumentException("Invalid array");
        for (int[] row : pixels) {
            if (row.length != length) throw new IllegalArgumentException("Jagged array");
        }
    }
}
