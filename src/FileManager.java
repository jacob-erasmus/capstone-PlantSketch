/*
 * This class is going to load in the files.
 * There is a method for each file type
 * William Marketos 17 Aug 2025
 */

 import java.io.File;
 import java.util.Scanner;



public class FileManager {


    private String currentFilePath;
    private Scanner scan;
    private String pwd;
    
    public FileManager() {
        this.currentFilePath = "";
        this.scan = new Scanner(System.in);
    }


    // used chatgpt for the general format and to understand reading in multiple files
    public void fileFinder()
    {
        System.out.println("Please enter the path for the directory containing your files");
        pwd = scan.nextLine();
        File directory = new File(pwd);

        if (directory.isDirectory()) {
            File[] files = directory.listFiles();

            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        String name = file.getName();

                        if (name.endsWith(".txt")) 
                        {
                            System.out.println("Loading Abiotics file: " + name);
                            loadTxt(file);
                            System.out.println(name + " Loaded :)");
                        } 

                        else if (name.endsWith(".elv")) 
                        {
                            System.out.println("Loading Elevation file: " + name);
                            loadElv(file);
                            System.out.println(name + " Loaded :)");
                        } 

                        else if (name.endsWith(".xlsx")) 
                        {
                            System.out.println("Loading Plant Parameters file: " + name);
                            loadXlsx(file);
                            System.out.println(name + " Loaded :)");
                        } 

                        else 
                        {
                            System.out.println("Unknown file in directory. Ignoring it." + name);
                        }
                    }
                }
            }
        } else {
            System.out.println("Not a valid directory :(");
        }

    }

    
    public void loadTxt(File file) 
    {
        String name = file.getName();
        String[] parts = name.split("[-_]");
        // parts = ["D4", "1024", "temp.txt"]

        int gridSize = Integer.parseInt(parts[1]); // "1024" → int

        if (name.contains("sun"))
        {
            System.out.println("Sunlight file");
            

        }    
    }

    
    public void loadElv(File file) {
        // Method stub
    }
    
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