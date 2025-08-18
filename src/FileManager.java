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
    private int gridSpacing;
    private int gridSize;
    private int dimX;
    private int dimY;

    private float[][] temperatureMap; // We are going to store the maps here and have getters to get them after running the program
    private float[][] moistureMap;
    private float[][] sunlightMap;
    private float[][] elevationMap;
    
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
                    if (file.isFile()) 
                    {
                        String name = file.getName();

                        if (name.endsWith(".elv")) 
                        {
                            System.out.print("Loading Elevation file: " + name);
                            // going to have to change terrain constructoe to take in grid[][]
                            // and to take in grid spacing, dimensions and yeah
                            elevationMap = loadElv(file);
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
                                sunlightMap = loadTxt(file);
                                // now edit constructors to allow accepting grid[][]
                                System.out.println(name + " Loaded :)");
                            }
                            
                            if (name.contains("temp"))
                            {
                                System.out.println("Loading Temperature File: " + name);
                                temperatureMap = loadTxt(file);
                                // now edit constructors to allow accepting grid[][]
                                System.out.println(name + " Loaded :)");
                            }

                            if (name.contains("wet"))
                            {
                                System.out.println("Loading Moisture File: " + name);
                                moistureMap = loadTxt(file);
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
            int temporary = 0;

            for(int x = 0; x < dimXtxt; x++)
            {
                for(int y = 0; y < dimYtxt; y++)
                {
                    for(int month = 0; month < 12; month++)
                    {
                        temporary += Integer.parseInt(data[month*y]); // Converting the string number for each month into an int
                        // month*y because there are 12 numbers for each point because of the months
                    }
                    grid[x][y] = temporary/12; // the average for the year
                    temporary = 0; // reset now because new point
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
            int[] nums = new int[parts.length];
            for (int i = 0; i < parts.length; i++) 
            {
                nums[i] = Integer.parseInt(parts[i]); // convert to int
                // nums[0] = dim x, nums[1] = dim y, nums[2] = GRIDSPACING, nums[3] = latitude(?)
            }
            dimX = nums[0];
            dimY = nums[1];
            gridSpacing = nums[2];
            int latitude = nums[3];

            String secondLine = fileScanner.nextLine();
            String[] altitudes = secondLine.split(" ");

            float[][] grid = new float[dimX][dimY];

            for(int x = 0; x < dimX; x++)
            {
                for(int y = 0; y < dimY; y++)
                {
                    grid[x][y] = Integer.parseInt(altitudes[x*y]); // x*y because that is the 1D representatino of the 2D coordinate
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
    
    public float[][] getTemperatureMap()
    {
        return temperatureMap;
    }

    public float[][] getMoistureMap()
    {
        return moistureMap;
    }

    public float[][] getElevationMap()
    {
        return elevationMap;
    }

    public float[][] getSunlightMap()
    {
        return sunlightMap;
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