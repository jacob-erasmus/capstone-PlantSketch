/*
 * This class is going to load in the files.
 * There is a method for each file type
 * William Marketos 17 Aug 2025
 */
package plantsketch;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.util.List;
import java.util.Scanner;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FileManager {

    private String currentFilePath;
    private Scanner scan;
    private String pwd;
    private float gridSpacing;
    private int gridSize;
    private int dimX;
    private int dimY;

    private float[][] temperatureGrid; // We are going to store the maps here and have getters to get them after// running the program
    private float[][] moistureGrid;
    private float[][] sunlightGrid;
    private float[][] elevationGrid;
    private float[][] ageGrid;
    private List<Plant> plants;

    public FileManager() {
        this.currentFilePath = "";
    }

    // used chatgpt for the general format and to understand reading in multiple
    // files
    public void fileFinder() {
        scan = new Scanner(System.in);
        System.out.println("Enter directory path containing your ecosystem files:");
        pwd = scan.nextLine();
        File directory = new File(pwd);

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                // First load .elv (terrain must come first for dimensions & spacing)
                for (File file : files) {
                    if (file.isFile() && file.getName().endsWith(".elv")) {
                        System.out.println("Loading Elevation file: " + file.getName());
                        elevationGrid = loadElv(file);
                        // assume loadElv sets gridSpacing, dimX, dimY internally
                    }
                }

                // Then load other files
                for (File file : files) {
                    if (file.isFile()) {
                        String name = file.getName().toLowerCase();

                        if (name.endsWith(".txt")) {
                            if (name.contains("sun")) {
                                sunlightGrid = loadTxt(file);
                            } else if (name.contains("temp")) {
                                temperatureGrid = loadTxt(file);
                            } else if (name.contains("wet") || name.contains("water")) {
                                moistureGrid = loadTxt(file);
                            }
                        } else if (name.endsWith(".png") && (name.contains("age"))) {
                            ageGrid = loadPng(file);
                        } else if (name.endsWith(".pdb")) {
                            System.out.println("Loading Plant Database: " + file.getName());
                            plants = loadPDB(file);
                        }
                    }
                }
            } else {
                System.out.println("Not a valid directory :(");
            }
        }
    }

    public float[][] loadTxt(File file) {
        // the abiotics seem to have inconsistent top lines, with grid spacing often
        // missing
        // so i am going to always read the .elv file first as that one is consistent
        // and then take the data from that. the grid spacing and dimensions will be the
        // same for all the abiotics as well as the elevation

        try {
            Scanner fileScanner = new Scanner(file);
            String firstLine = fileScanner.nextLine();
            String[] parts = firstLine.split(" "); // split by spaces

            // this is reading in the first line with all of the info
            int dimXtxt = Integer.parseInt(parts[0]);
            int dimYtxt = Integer.parseInt(parts[1]);

            // gonna ignore first line as inconsistent. taking data from .elv file as
            // consistent
            String secondLine = fileScanner.nextLine();
            String[] data = secondLine.split(" ");

            float[][] grid = new float[dimXtxt][dimYtxt];
            float temporary = 0.0f;

            for (int x = 0; x < dimXtxt; x++) {
                for (int y = 0; y < dimYtxt; y++) {
                    for (int month = 0; month < 12; month++) {
                        int index = (x * dimYtxt + y) * 12 + month;
                        temporary += Float.parseFloat(data[index]);
                        // Converting the string number for each month into an int
                        // month*y because there are 12 numbers for each point because of the months
                    }
                    grid[x][y] = temporary / 12.0f; // the average for the year
                    temporary = 0.0f; // reset now because new point
                }
            }
            fileScanner.close();
            return grid;

        } catch (FileNotFoundException e) {
            System.out.println("Error reading in the file: " + e);
        }

        return null;

    }

    public float[][] loadElv(File file) {
        try {

            Scanner fileScanner = new Scanner(file);
            String firstLine = fileScanner.nextLine();
            String[] parts = firstLine.split(" "); // split by spaces

            // this is reading in the first line with all of the info
            float[] nums = new float[parts.length];
            for (int i = 0; i < parts.length; i++) {
                nums[i] = Float.parseFloat(parts[i]); // convert to int
                // nums[0] = dim x, nums[1] = dim y, nums[2] = GRIDSPACING, nums[3] =
                // latitude(?)
            }
            dimX = (int) nums[0];
            dimY = (int) nums[1];
            gridSpacing = nums[2];
            float latitude = nums[3];

            String secondLine = fileScanner.nextLine();
            String[] altitudes = secondLine.split(" ");

            float[][] grid = new float[dimX][dimY];

            for (int x = 0; x < dimX; x++) {
                for (int y = 0; y < dimY; y++) {
                    grid[x][y] = Float.parseFloat(altitudes[x * dimY + y]); // x*dimY + y because that is the 1D
                                                                            // representatino of the 2D coordinate
                }
            }
            fileScanner.close();
            return grid;

        } catch (FileNotFoundException e) {
            System.out.println("Error reading in the file: " + e);
        }
        return null;
    }

    public float[][] loadPng(File file) {
        try {
            BufferedImage img = ImageIO.read(file);
            int width = img.getWidth();
            int height = img.getHeight();

            // check consistency with elevation grid dimensions
            if (this.dimX != 0 && this.dimY != 0) {
                if (width != this.dimX || height != this.dimY) {
                    System.out.println("Oh no!: PNG dimensions (" + width + "x" + height +
                            ") do not match .elv dimensions (" + dimX + "x" + dimY + ")");
                }
            } else {
                // if no .elv loaded yet, fall back to image dims
                this.dimX = width;
                this.dimY = height;
            }

            float[][] grid = new float[width][height];

            // normalize red [0–255] → age [0–600]
            for (int x = 0; x < width; x++) {
                for (int y = 0; y < height; y++) {
                    int rgb = img.getRGB(x, y);
                    int red = (rgb >> 16) & 0xFF;
                    float age = (red / 255.0f) * 650.0f;
                    grid[x][y] = age;
                }
            }

            return grid;

        } catch (IOException e) {
            System.out.println("Error reading PNG: " + e.getMessage());
            return null;
        }
    }

    public float[][] getTemperatureGrid() {
        return temperatureGrid;
    }

    public float[][] getMoistureGrid() {
        return moistureGrid;
    }

    public float[][] getElevationGrid() {
        return elevationGrid;
    }

    public float[][] getSunlightGrid() {
        return sunlightGrid;
    }

    public float[][] getAgeGrid() {
        return ageGrid;
    }

    public int getDimX() {
        return dimX;
    }

    public int getDimY() {
        return dimY;
    }

    public float getGridSpacing() {
        return gridSpacing;
    }

    // gotta do these sometime ig.

    public void loadXlsx(File file) {
        // Method stub
    }

    public void loadPdb(File file) {
        // Method stub
    }

    public boolean saveSimulation(Forest forest, String filePath) {
        // Method stub
        return false;
    }

    public Forest loadSimulation(String filePath) {
        // Method stub
        return null;
    }

    public void setCurrentFilePath(String filePath) {
        // Method stub
    }

    public List<Plant> getPlants() {
        return plants;
    }

    public String getPwd()
    {
        return pwd;
    }


    // ADD THIS in FileManager (same package: plantsketch)
    public void fileFinder(String directoryPath) {
        File directory = new File(directoryPath);
        pwd = directoryPath;
        if (!directory.isDirectory()) {
            System.out.println("Not a valid directory :(");
            return;
        }
        File[] files = directory.listFiles();
        if (files == null)
            return;

        // 1st pass: load .elv first
        for (File file : files) {
            if (!file.isFile())
                continue;
            String name = file.getName();
            if (name.endsWith(".elv")) {
               // System.out.println("Loading Elevation file: " + name);
                elevationGrid = loadElv(file);
               // System.out.println(name + " Loaded :)");
            }
        }
        // 2nd pass: others
        for (File file : files) {
            if (!file.isFile())
                continue;
            String name = file.getName();
            if (name.endsWith(".txt")) {
                if (name.contains("sun")) {
                   // System.out.println("Loading Sunlight File: " + name);
                    sunlightGrid = loadTxt(file);
                    // System.out.println(name + " Loaded :)");
                } else if (name.contains("temp")) {
                  // System.out.println("Loading Temperature File: " + name);
                    temperatureGrid = loadTxt(file);
                   // System.out.println(name + " Loaded :)");
                } else if (name.contains("wet") || name.contains("water")) {
                   // System.out.println("Loading Moisture File: " + name);
                    moistureGrid = loadTxt(file);
                   // System.out.println(name + " Loaded :)");
                }
            } else if (name.endsWith(".png") && (name.toLowerCase().contains("age"))) {
               // System.out.println("Loading Age File: " + name);
                this.ageGrid = loadPng(file);
               // System.out.println(name + " Loaded :)");
            } else if (name.endsWith(".pdb"))
            {
                plants = loadPDB(file);
            }
        }
    }

public List<Plant> loadPDB(File file) {
    plants = new java.util.ArrayList<>();

    try (Scanner fileScanner = new Scanner(file)) {
        // First line: version
        String versionLine = fileScanner.nextLine().trim();
        if (!versionLine.startsWith("3.0")) {
            System.out.println("Warning: unexpected PDB version " + versionLine);
        }

        // Second line: dimensions
        if (!fileScanner.hasNextLine()) return plants;
        String[] dimParts = fileScanner.nextLine().trim().split("\\s+");
        if (dimParts.length >= 2) {
            this.dimX = Integer.parseInt(dimParts[0]);
            this.dimY = Integer.parseInt(dimParts[1]);
        }

        // Third line: unused zero
        if (fileScanner.hasNextLine()) {
            fileScanner.nextLine();
        }

        // Fourth line: number of trees
        int numTrees = 0;
        if (fileScanner.hasNextLine()) {
            numTrees = Integer.parseInt(fileScanner.nextLine().trim());
        }

        // Tree entries
        for (int i = 0; i < numTrees && fileScanner.hasNextLine(); i++) {
            String line = fileScanner.nextLine().trim();
            if (line.equals("0")) break; // terminating zero

            String[] parts = line.split("\\s+");
            if (parts.length < 8) {
                System.out.println("Skipping malformed tree entry: " + line);
                continue;
            }

            int treeID = Integer.parseInt(parts[0]);
            String speciesName = parts[1]; // 4-char species name
            float posX = Float.parseFloat(parts[2]);
            float posY = Float.parseFloat(parts[3]);
            float height = Float.parseFloat(parts[4]);
            float canopyRadius = Float.parseFloat(parts[5]);
            // parts[6] and parts[7] are the two zeros (ignored)

            Plant p = new Plant(treeID, speciesName, posX, posY,0, null, canopyRadius, height, false, 0, false);
            plants.add(p);
        }

        // consume terminating zero if not already read
        if (fileScanner.hasNextLine()) {
            String end = fileScanner.nextLine().trim();
            if (!end.equals("0")) {
                System.out.println("Expected terminating zero but found: " + end);
            }
        }

    } catch (FileNotFoundException e) {
        System.out.println("Error reading PDB file: " + e.getMessage());
    }

    this.plants = plants;
    return plants;
}


}