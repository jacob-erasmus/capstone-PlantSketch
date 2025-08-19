/*
 * This class is going to load in the files.
 * There is a method for each file type
 * William Marketos 17 Aug 2025
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOError;
import java.util.Scanner;



public class FileManager {


    private String currentFilePath;
    private Scanner scan;
    private String pwd;
    private float gridSpacing;
    private int gridSize;
    private int dimX;
    private int dimY;

    private float[][] temperatureGrid; // We are going to store the maps here and have getters to get them after running the program
    private float[][] moistureGrid;
    private float[][] sunlightGrid;
    private float[][] elevationGrid;
    
    public FileManager() {
        this.currentFilePath = "";
    }


    // used chatgpt for the general format and to understand reading in multiple files
    public void fileFinder()
    {
        System.out.println("Please enter the path for the directory containing your files");
        scan = new Scanner(System.in);
        pwd = scan.nextLine();
        File directory = new File(pwd);


        if (directory.isDirectory()) 
        {
            File[] files = directory.listFiles();

            if (files != null) 
            {
                for (File file : files) 
                {
                    System.out.println("Found file: " + file.getName());
                    // loading .elv file first for the grid spacing
                    if (file.isFile()) 
                    {
                        String name = file.getName();

                        if (name.endsWith(".elv")) 
                        {
                            System.out.println("Loading Elevation file: " + name);
                            // going to have to change terrain constructoe to take in grid[][]
                            // and to take in grid spacing, dimensions and yeah
                            elevationGrid = loadElv(file);
                            System.out.println(name + " Loaded :)");
                        } 
                    }
                }
                // loading .elv file first for the grid spacing
                for (File file : files) 
                {
                    if (file.isFile()) 
                    {
                        String name = file.getName();

                        if (name.endsWith(".txt")) 
                        {
                            if (name.contains("sun"))
                            {
                                System.out.println("Loading Sunlight File: " + name);
                                sunlightGrid = loadTxt(file);
                                // now edit constructors to allow accepting grid[][]
                                System.out.println(name + " Loaded :)");
                            }
                            
                            if (name.contains("temp"))
                            {
                                System.out.println("Loading Temperature File: " + name);
                                temperatureGrid = loadTxt(file);
                                // now edit constructors to allow accepting grid[][]
                                System.out.println(name + " Loaded :)");
                            }

                            if (name.contains("wet") || name.contains("water"))
                            {
                                System.out.println("Loading Moisture File: " + name);
                                moistureGrid = loadTxt(file);
                                // now edit constructors to allow accepting grid[][]
                                System.out.println(name + " Loaded :)");
                            }
                        } 
                        else 
                        {
                            System.out.println("Unknown file in directory. Ignoring it." + name);
                        }
                    }
                }
            }
        else 
        {
            System.out.println("Not a valid directory :(");
        }
    } 

    }

    public float[][] loadTxt(File file)
    {
        // the abiotics seem to have inconsistent top lines, with grid spacing often missing
        // so i am going to always read the .elv file first as that one is consistent
        // and then take the data from that. the grid spacing and dimensions will be the same for all the abiotics as well as the elevation

        try{
            Scanner fileScanner = new Scanner(file);
            String firstLine = fileScanner.nextLine();
            String[] parts = firstLine.split(" ");  // split by spaces

            // this is reading in the first line with all of the info
            int dimXtxt = Integer.parseInt(parts[0]);
            int dimYtxt = Integer.parseInt(parts[1]);

            // gonna ignore first line as inconsistent. taking data from .elv file as consistent
            String secondLine = fileScanner.nextLine();
            String[] data = secondLine.split(" ");

            float[][] grid = new float[dimXtxt][dimYtxt];
            float temporary = 0.0f;

            for(int x = 0; x < dimXtxt; x++)
            {
                for(int y = 0; y < dimYtxt; y++)
                {
                    for(int month = 0; month < 12; month++)
                    {
                        int index = (x * dimYtxt + y) * 12 + month;
                        temporary += Float.parseFloat(data[index]);
                        // Converting the string number for each month into an int
                        // month*y because there are 12 numbers for each point because of the months
                    }
                    grid[x][y] = temporary/12.0f; // the average for the year
                    temporary = 0.0f; // reset now because new point
                }
            }
            fileScanner.close();
            return grid;

        }
        catch(FileNotFoundException e)
        {
            System.out.println("Error reading in the file: " + e);
        }

        return null;

    }
    
    public float[][] loadElv(File file) 
    {
        try{

            Scanner fileScanner = new Scanner(file);
            String firstLine = fileScanner.nextLine();
            String[] parts = firstLine.split(" ");  // split by spaces

            // this is reading in the first line with all of the info
            float[] nums = new float[parts.length];
            for (int i = 0; i < parts.length; i++) 
            {
                nums[i] = Float.parseFloat(parts[i]); // convert to int
                // nums[0] = dim x, nums[1] = dim y, nums[2] = GRIDSPACING, nums[3] = latitude(?)
            }
            dimX = (int)nums[0];
            dimY = (int)nums[1];
            gridSpacing = nums[2];
            float latitude = nums[3];

            String secondLine = fileScanner.nextLine();
            String[] altitudes = secondLine.split(" ");

            float[][] grid = new float[dimX][dimY];

            for(int x = 0; x < dimX; x++)
            {
                for(int y = 0; y < dimY; y++)
                {
                    grid[x][y] = Float.parseFloat(altitudes[x * dimY + y]); // x*dimY + y because that is the 1D representatino of the 2D coordinate
                }
            }
            fileScanner.close();
            return grid;



        } 
        catch (FileNotFoundException e)
        {
            System.out.println("Error reading in the file: " + e);
        }
        return null;
    }
    
    public float[][] getTemperatureGrid()
    {
        return temperatureGrid;
    }

    public float[][] getMoistureGrid()
    {
        return moistureGrid;
    }

    public float[][] getElevationGrid()
    {
        return elevationGrid;
    }

    public float[][] getSunlightGrid()
    {
        return sunlightGrid;
    }

    public int getDimX()
    {
        return dimX;
    }

    public int getDimY()
    {
        return dimY;
    }

    public float getGridSpacing()
    {
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
}