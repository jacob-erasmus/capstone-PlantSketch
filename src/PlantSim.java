import javax.swing.*;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class PlantSim {
    private Terrain terrain;
    private Forest forest;
    private Renderer2D renderer;
    private UIController uiController;
    private FileManager fileManager;
    private boolean isRunning;
    private SunlightMap sunlightMap;
    private MoistureMap moistureMap;
    private TemperatureMap temperatureMap;
    
    
    public PlantSim() {
        this.terrain = null;
        this.forest = null;
        this.renderer = null;
        this.uiController = null;
        this.fileManager = null;
        this.isRunning = false;
    }
    
    public PlantSim(Terrain terrain, Forest forest, Renderer2D renderer, UIController uiController, FileManager fileManager) {
        this.terrain = terrain;
        this.forest = forest;
        this.renderer = renderer;
        this.uiController = uiController;
        this.fileManager = fileManager;
        this.isRunning = false;
    }
    

    public static void main(String[] args)
    {
        // option 1: upload all files
        // 2: upload .pdb file
        // read in files
        FileManager fileManager = new FileManager();
        fileManager.fileFinder();
        int dimX = fileManager.getDimX();
        int dimY = fileManager.getDimY();
        float gridSpacing = fileManager.getGridSpacing();

        // create the necessary objects that were read in from the files
        TemperatureMap temperatureMap = new TemperatureMap(dimX, dimY, gridSpacing, fileManager.getTemperatureGrid());
        MoistureMap moistureMap = new MoistureMap(dimX, dimY, gridSpacing, fileManager.getMoistureGrid());
        SunlightMap sunligntMap = new SunlightMap(dimX, dimY, gridSpacing, fileManager.getSunlightGrid());
        AbioticFactors abioticFactors = new AbioticFactors(moistureMap, temperatureMap, sunligntMap);
        Terrain terrain = new Terrain(dimX, dimY, gridSpacing, abioticFactors, fileManager.getElevationGrid());

        //vizualizer for the abiotic factors
        GridVisualizer.showGrid(fileManager.getMoistureGrid(), "Moisture Map");
        GridVisualizer.showGrid(fileManager.getTemperatureGrid(), "Temperature Map");
        GridVisualizer.showGrid(fileManager.getSunlightGrid(), "Sunlight Map");
        GridVisualizer.showGrid(fileManager.getElevationGrid(), "Elevation Map");

        moistureMap.testMoisture();
        temperatureMap.testTemperature();
        sunligntMap.testSunlight();
        terrain.testTerrain();
        // test completed. files read in successfully.

        // load in data for different species
        List<Species> speciesList = new ArrayList<>();
        SpeciesDictionary speciesDictionary = new SpeciesDictionary();
        Species boxwood = speciesDictionary.loadBoxwood();
        Species snowyMespilus = speciesDictionary.loadSnowyMespilus();
        Species mountainPine = speciesDictionary.loadMountainPine();
        Species silverFir = speciesDictionary.loadSilverFir();
        Species silverBirch = speciesDictionary.loadSilverBirch();
        Species sissileOak = speciesDictionary.loadSissileOak();
        Species europeanBeech = speciesDictionary.loadEuropeanBeech();


                // === TESTING PINK NOISE SAMPLER ===
        PinkNoiseSampler sampler = new PinkNoiseSampler(dimX, dimY, 2.0f, 42L); 
        // dimX/dimY from file, 2.0f = 2m min separation, 42L = random seed
        List<PointSample> samples = sampler.generateSamples(1000); // try 1000 canopy trees

        // Show results in Swing window
        SwingUtilities.invokeLater(() -> {
        JFrame frame = new JFrame("Pink Noise Visualization");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.add(new PinkNoiseVisualizer(samples, dimX, dimY));
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        });

        //viabilityCalculator setup
        ViabilityCalculator calc = new ViabilityCalculator(terrain, abioticFactors);
        double boxwoodViability;
        double snowyMespilusViability;
        double mountainPineViability;
        double silverFirViability;
        double silverBirchViability;
        double sissileOakViability;
        double europeanBeechViability;
        float sumViabilites;

        //setup species maps
        SpeciesMap boxwodMap = new SpeciesMap(boxwood);
        SpeciesMap snowyMespilusMap = new SpeciesMap(snowyMespilus);
        SpeciesMap mountainPineMap = new SpeciesMap(mountainPine);
        SpeciesMap silverFirMap = new SpeciesMap(silverFir);
        SpeciesMap silverBirchMap = new SpeciesMap(silverBirch);
        SpeciesMap sissileOakMap = new SpeciesMap(sissileOak);
        SpeciesMap europeanBeechMap = new SpeciesMap(europeanBeech);

        //Per Sample Point placement
        for (int i = 0; i < samples.size(); i++){
            int xPos = (int)samples.get(i).getX();
            int yPos = (int)samples.get(i).getY();

            //Step 1: run viability function for all species
            boxwoodViability = calc.viabililty(boxwood, xPos, yPos);
            boxwood.setViabilityAtPoint(boxwoodViability);
            if (boxwoodViability > 0){
                speciesList.add(boxwood);
            }
            snowyMespilusViability = calc.viabililty(snowyMespilus, xPos, yPos);
            snowyMespilus.setViabilityAtPoint(snowyMespilusViability);
            if (snowyMespilusViability > 0){
                speciesList.add(boxwood);
            }
            mountainPineViability = calc.viabililty(mountainPine, xPos, yPos);
            mountainPine.setViabilityAtPoint(mountainPineViability);
            if (mountainPineViability > 0){
                speciesList.add(boxwood);
            }
            silverFirViability = calc.viabililty(silverFir, xPos, yPos);
            silverFir.setViabilityAtPoint(silverFirViability);
            if (silverFirViability > 0){
                speciesList.add(boxwood);
            }
            silverBirchViability = calc.viabililty(silverBirch, xPos, yPos);
            silverBirch.setViabilityAtPoint(silverBirchViability);
            if (silverBirchViability > 0){
                speciesList.add(boxwood);
            }
            sissileOakViability = calc.viabililty(sissileOak, xPos, yPos);
            sissileOak.setViabilityAtPoint(sissileOakViability);
            if (sissileOakViability > 0){
                speciesList.add(boxwood);
            }
            europeanBeechViability = calc.viabililty(europeanBeech, xPos, yPos);
            europeanBeech.setViabilityAtPoint(europeanBeechViability);
            if (europeanBeechViability > 0){
                speciesList.add(boxwood);
            }
            //sum of all viabilites (also known as density)
            sumViabilites = (float)(boxwoodViability + snowyMespilusViability + mountainPineViability + silverBirchViability + silverFirViability + sissileOakViability + europeanBeechViability);
            
            //Step 2: run roulette wheel
            RouletteWheelSelector rw = new RouletteWheelSelector(sumViabilites);
            String speciesSelected = rw.selectSpecies(speciesList);

            //Step 3: need age, size etc, allometry etc...
            //filler cohort age as max bound:
            Random r = new Random();
            float cohortAge = 80;
            //growth function intialise
            GrowthFunction growthCalc = new GrowthFunction();
            //setting allometry
            boolean isAllometryOpen;
            if(sumViabilites > 0.5){
                isAllometryOpen = true;
            }else{
                isAllometryOpen = false;
            }
            //setup necessary parameters and add plant to the choosen species speciesMap
            if (speciesSelected == "Boxwood"){
                //setting age
                float upperBound = cohortAge;
                if (cohortAge > boxwood.getLifeSpan()){
                    upperBound = boxwood.getLifeSpan();
                }
                float plantAge = r.nextFloat(upperBound+1) * (float)boxwoodViability;
                //setting height
                float height = growthCalc.calculateSize(boxwood, plantAge, isAllometryOpen);
                //setting canopy radius
                float canopyRadius;
                if(isAllometryOpen){
                    canopyRadius = height * boxwood.getRadiusMultiplierOpen();
                }else{
                    canopyRadius = height * boxwood.getRadiusMultiplierClosed();
                }
                boxwodMap.setPlantAt(new Plant(i, xPos, yPos, plantAge, boxwood, canopyRadius, height, true, (float)boxwoodViability, isAllometryOpen), xPos, yPos);
            }else if (speciesSelected == "Snowy Mespilus"){
                //setting age
                float upperBound = cohortAge;
                if (cohortAge > snowyMespilus.getLifeSpan()){
                    upperBound = snowyMespilus.getLifeSpan();
                }
                float plantAge = r.nextFloat(upperBound+1) * (float)snowyMespilusViability;
                //setting height
                float height = growthCalc.calculateSize(snowyMespilus, plantAge, isAllometryOpen);
                //setting canopy radius
                float canopyRadius;
                if(isAllometryOpen){
                    canopyRadius = height * snowyMespilus.getRadiusMultiplierOpen();
                }else{
                    canopyRadius = height * snowyMespilus.getRadiusMultiplierClosed();
                }
                snowyMespilusMap.setPlantAt(new Plant(i, xPos, yPos, plantAge, snowyMespilus, canopyRadius, height, true, (float)snowyMespilusViability, isAllometryOpen), xPos, yPos);
            }else if (speciesSelected == "Mountain Pine"){
                //setting age
                float upperBound = cohortAge;
                if (cohortAge > mountainPine.getLifeSpan()){
                    upperBound = mountainPine.getLifeSpan();
                }
                float plantAge = r.nextFloat(upperBound+1) * (float)mountainPineViability;
                //setting height
                float height = growthCalc.calculateSize(mountainPine, plantAge, isAllometryOpen);
                //setting canopy radius
                float canopyRadius;
                if(isAllometryOpen){
                    canopyRadius = height * mountainPine.getRadiusMultiplierOpen();
                }else{
                    canopyRadius = height * mountainPine.getRadiusMultiplierClosed();
                }    
                mountainPineMap.setPlantAt(new Plant(i, xPos, yPos, plantAge, mountainPine, canopyRadius, height, true, (float)mountainPineViability, isAllometryOpen), xPos, yPos);
            }else if (speciesSelected == "Silver Fir"){
                //setting age
                float upperBound = cohortAge;
                if (cohortAge > silverFir.getLifeSpan()){
                    upperBound = silverFir.getLifeSpan();
                }
                float plantAge = r.nextFloat(upperBound+1) * (float)silverFirViability;
                //setting height
                float height = growthCalc.calculateSize(silverFir, plantAge, isAllometryOpen);
                //setting canopy radius
                float canopyRadius;
                if(isAllometryOpen){
                    canopyRadius = height * silverFir.getRadiusMultiplierOpen();
                }else{
                    canopyRadius = height * silverFir.getRadiusMultiplierClosed();
                }    
                silverFirMap.setPlantAt(new Plant(i, xPos, yPos, plantAge, silverFir, canopyRadius, height, true, (float)silverFirViability, isAllometryOpen), xPos, yPos);
            }else if (speciesSelected == "Silver Birch"){
                //setting age
                float upperBound = cohortAge;
                if (cohortAge > silverBirch.getLifeSpan()){
                    upperBound = silverBirch.getLifeSpan();
                }
                float plantAge = r.nextFloat(upperBound+1) * (float)silverBirchViability;
                //setting height
                float height = growthCalc.calculateSize(silverBirch, plantAge, isAllometryOpen);
                //setting canopy radius
                float canopyRadius;
                if(isAllometryOpen){
                    canopyRadius = height * silverBirch.getRadiusMultiplierOpen();
                }else{
                    canopyRadius = height * silverBirch.getRadiusMultiplierClosed();
                }
                silverBirchMap.setPlantAt(new Plant(i, xPos, yPos, plantAge, silverBirch, canopyRadius, height, true, (float)silverBirchViability, isAllometryOpen), xPos, yPos);
            }else if (speciesSelected == "Sissile Oak"){
                //setting age
                float upperBound = cohortAge;
                if (cohortAge > sissileOak.getLifeSpan()){
                    upperBound = sissileOak.getLifeSpan();
                }
                float plantAge = r.nextFloat(upperBound+1) * (float)sissileOakViability;
                //setting height
                float height = growthCalc.calculateSize(sissileOak, plantAge, isAllometryOpen);
                //setting canopy radius
                float canopyRadius;
                if(isAllometryOpen){
                    canopyRadius = height * sissileOak.getRadiusMultiplierOpen();
                }else{
                    canopyRadius = height * sissileOak.getRadiusMultiplierClosed();
                }
                sissileOakMap.setPlantAt(new Plant(i, xPos, yPos, plantAge, sissileOak, canopyRadius, height, true, (float)sissileOakViability, isAllometryOpen), xPos, yPos);
            }else if (speciesSelected == "European Beech"){
                //setting age
                float upperBound = cohortAge;
                if (cohortAge > europeanBeech.getLifeSpan()){
                    upperBound = europeanBeech.getLifeSpan();
                }
                float plantAge = r.nextFloat(upperBound+1) * (float)europeanBeechViability;
                //setting height
                float height = growthCalc.calculateSize(europeanBeech, plantAge, isAllometryOpen);
                //setting canopy radius
                float canopyRadius;
                if(isAllometryOpen){
                    canopyRadius = height * europeanBeech.getRadiusMultiplierOpen();
                }else{
                    canopyRadius = height * europeanBeech.getRadiusMultiplierClosed();
                }
                europeanBeechMap.setPlantAt(new Plant(i, xPos, yPos, plantAge, europeanBeech, canopyRadius, height, true, (float)europeanBeechViability, isAllometryOpen), xPos, yPos);
            }
            //else do nothing - no species choosen, no plant placed at this sample.
        //Point sample placement done.
        }
        
        //Setup forest
        Forest forest = new Forest(dimX, dimY);
        forest.addSpeciesMap(boxwodMap);
        forest.addSpeciesMap(snowyMespilusMap);
        forest.addSpeciesMap(mountainPineMap);
        forest.addSpeciesMap(silverFirMap);
        forest.addSpeciesMap(silverBirchMap);
        forest.addSpeciesMap(sissileOakMap);
        forest.addSpeciesMap(europeanBeechMap);

        // overlay forest on terrain

        // export to 2D renderer

        
    }

    public void startSimulation() {
        // Method stub
    }
    
    public void pauseSimulation() {
        // Method stub
    }
    
    public void resetSimulation() {
        // Method stub
    }
    
    public void applyGlobalChanges() {
        // Method stub
    }
    
    public void save(String folderPath) {
        // Method stub
    }
    
    public void load(String folderPath) {
        // Method stub
    }
    
}
