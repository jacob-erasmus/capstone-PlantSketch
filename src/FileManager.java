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
    
    public FileManager() {
        this.currentFilePath = "";
        this.scan = new Scanner(System.in);
    }


    // used chatgpt for the general format and to understand reading in multiple files
    public void fileFinder()
    {
        System.out.println("Please enter the path for the directory containing your files");
        String pwd = scan.nextLine();
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
                            loadTxt(pwd + "/" + name);
                            System.out.println(name + " Loaded :)");
                        } 

                        else if (name.endsWith(".clim")) 
                        {
                            System.out.println("Loading Climate file: " + name);
                            loadClim(pwd + "/" + name);
                            System.out.println(name + " Loaded :)");
                        } 

                        else if (name.endsWith(".elv")) 
                        {
                            System.out.println("Loading Elevation file: " + name);
                            loadElv(pwd + "/" + name);
                            System.out.println(name + " Loaded :)");
                        } 

                        /*else if (name.endsWith(".xlsx")) 
                        {
                            System.out.println("Loading Plant Parameters file: " + name);
                            loadXlsx(pwd + "/" + name);
                            System.out.println(name + " Loaded :)");
                        } */ //I am pretty sure this is fixed

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

    
    public void loadTxt(String filePath) {
        // Method stub
    }
    
    public void loadClim(String filePath) {
        // Method stub
    }
    
    public void loadElv(String filePath) {
        // Method stub
    }
    
    public void loadXlsx(String filePath) {
        // Method stub
    }
    
    public void loadPdb(String filePath) {
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