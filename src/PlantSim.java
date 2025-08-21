import javax.swing.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

public class PlantSim {
    public PlantSim() {
    }

    public static void main(String[] args)
    {
        // option 1: upload all files
        // 2: upload .pdb file
        // read in files
        Scanner userInput = new Scanner(System.in);
        System.out.println("How many plants would you like to try place?");
        int plantCount = userInput.nextInt();

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

        // === AGE MAP ===
        AgeMap ageMap = new AgeMap(dimX, dimY, gridSpacing, fileManager.getAgeGrid());

        // test printing a few ages
        ageMap.testAge();

        // visualize as grid (darker = older)
        GridVisualizer.showGrid(fileManager.getAgeGrid(), "Age Map");


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
        List<Species> speciesTemp = new ArrayList<>();
        SpeciesDictionary speciesDictionary = new SpeciesDictionary();
        Species boxwood = speciesDictionary.loadBoxwood();
        speciesList.add(boxwood);
        Species snowyMespilus = speciesDictionary.loadSnowyMespilus();
        speciesList.add(snowyMespilus);
        Species mountainPine = speciesDictionary.loadMountainPine();
        speciesList.add(mountainPine);
        Species silverFir = speciesDictionary.loadSilverFir();
        speciesList.add(silverFir);
        Species silverBirch = speciesDictionary.loadSilverBirch();
        speciesList.add(silverBirch);
        Species sissileOak = speciesDictionary.loadSissileOak();
        speciesList.add(sissileOak);
        Species europeanBeech = speciesDictionary.loadEuropeanBeech();
        speciesList.add(europeanBeech);

                // === TESTING PINK NOISE SAMPLER ===
        //convert dimX etc to METERS using gridSpacing
        float metersX = dimX*gridSpacing;
        float metersY = dimY*gridSpacing;
        
        PinkNoiseSampler sampler = new PinkNoiseSampler(metersX, metersY, 2.0f, 42L); 
        // dimX/dimY from file, 2.0f = 2m min separation, 42L = random seed
        List<PointSample> samples = sampler.generateSamples(plantCount); // try 1000 canopy trees

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
        float viabililty;
        float sumViabilites = 0.0f;
        float density = 0.0f;

        //setup species maps
        SpeciesMap boxwoodMap = new SpeciesMap(boxwood);
        SpeciesMap snowyMespilusMap = new SpeciesMap(snowyMespilus);
        SpeciesMap mountainPineMap = new SpeciesMap(mountainPine);
        SpeciesMap silverFirMap = new SpeciesMap(silverFir);
        SpeciesMap silverBirchMap = new SpeciesMap(silverBirch);
        SpeciesMap sissileOakMap = new SpeciesMap(sissileOak);
        SpeciesMap europeanBeechMap = new SpeciesMap(europeanBeech);

        //Per Sample Point placement
        int plantsPlacedCount = 0;
        for (int i = 0; i < samples.size(); i++){
            System.out.println("Sample point: " + i);
            float xPos = samples.get(i).getX();
            float yPos = samples.get(i).getY();
            int xCell = (int)xPos;
            int yCell = (int)yPos;
            int count = 0;

            density = 0.0f;
            sumViabilites = 0.0f;
            speciesTemp.clear();

            //Step 1: run viability function for all species
            for(Species species: speciesList){
                viabililty = calc.viabililty(species, xCell, yCell);
                species.setViabilityAtPoint(viabililty);
                
                if (viabililty > 0){
                    speciesTemp.add(species);
                    count++;
                    System.out.println(species.getName() + " viability: " + viabililty);
                    System.out.println("count:" + count);
                }
            }

            //sum of all viabilites
            //sumViabilites = (float)(boxwoodViability + snowyMespilusViability + mountainPineViability + silverBirchViability + silverFirViability + sissileOakViability + europeanBeechViability);
            Table[] rouletteTable = new Table[count];
            for (int y = 0; y < count; y++){
                float viabililtyCur = speciesTemp.get(y).getViabilityAtPoint();
                if (density < viabililtyCur){
                    density = viabililtyCur;
                }
                sumViabilites += viabililtyCur;
                rouletteTable[y] = new Table(speciesTemp.get(y), sumViabilites);
            }

            //density function, if larger than random then no plant at point
            System.out.println("Density: " + density);
            Random r = new Random();
            if(density > r.nextFloat()){
                System.out.println("No placement due to density");
                continue;
            }

            //
            //Step 2: run roulette wheel
            RouletteWheelSelector rw = new RouletteWheelSelector(sumViabilites);
            Species speciesSelected = rw.selectSpecies(rouletteTable, count);
            if (speciesSelected == null){
                continue;
            }
            //Step 3: need age, size etc, allometry etc...
            //filler cohort age as max bound:
    //FIX with AGE MAP
            float cohortAge = ageMap.getAge(xCell, yCell);
            //growth function intialise
            GrowthFunction growthCalc = new GrowthFunction();
            //setting allometry
            boolean isAllometryOpen;
            if(density > 0.8){
                isAllometryOpen = true;
            }else{
                isAllometryOpen = false;
            }
            //setup necessary parameters and add plant to the choosen species speciesMap
            //setting age
            float upperBound = cohortAge;
            if (upperBound > speciesSelected.getLifeSpan()){
                upperBound = speciesSelected.getLifeSpan();
            }
            float plantAge = r.nextFloat(upperBound) * speciesSelected.getViabilityAtPoint();
            System.out.println("Plant age: " + plantAge);
            //setting height
            float height = growthCalc.calculateSize(speciesSelected, plantAge, isAllometryOpen);
            System.out.println("height:" + height);
            //setting canopy radius
            float canopyRadius;
            if(isAllometryOpen){
                canopyRadius = height * speciesSelected.getRadiusMultiplierOpen();
            }else{
                canopyRadius = height * speciesSelected.getRadiusMultiplierClosed();
            }
            System.out.println("canopyRadius: " + canopyRadius);
            //placing plant
            plantsPlacedCount++;
            if (speciesSelected.getName() == "Boxwood"){
                boxwoodMap.setPlantAt(new Plant(plantsPlacedCount, xPos, yPos, plantAge, boxwood, canopyRadius, height, true, speciesSelected.getViabilityAtPoint(), isAllometryOpen));
            }else if (speciesSelected.getName() == "Snowy Mespilus"){
                snowyMespilusMap.setPlantAt(new Plant(plantsPlacedCount, xPos, yPos, plantAge, snowyMespilus, canopyRadius, height, true, speciesSelected.getViabilityAtPoint(), isAllometryOpen));
            }else if (speciesSelected.getName() == "Mountain Pine"){    
                mountainPineMap.setPlantAt(new Plant(plantsPlacedCount, xPos, yPos, plantAge, mountainPine, canopyRadius, height, true, speciesSelected.getViabilityAtPoint(), isAllometryOpen));
            }else if (speciesSelected.getName() == "Silver Fir"){  
                silverFirMap.setPlantAt(new Plant(plantsPlacedCount, xPos, yPos, plantAge, silverFir, canopyRadius, height, true, speciesSelected.getViabilityAtPoint(), isAllometryOpen));
            }else if (speciesSelected.getName() == "Silver Birch"){
                silverBirchMap.setPlantAt(new Plant(plantsPlacedCount, xPos, yPos, plantAge, silverBirch, canopyRadius, height, true, speciesSelected.getViabilityAtPoint(), isAllometryOpen));
            }else if (speciesSelected.getName() == "Sissile Oak"){
                sissileOakMap.setPlantAt(new Plant(plantsPlacedCount, xPos, yPos, plantAge, sissileOak, canopyRadius, height, true, speciesSelected.getViabilityAtPoint(), isAllometryOpen));
            }else if (speciesSelected.getName() == "European Beech"){
                europeanBeechMap.setPlantAt(new Plant(plantsPlacedCount, xPos, yPos, plantAge, europeanBeech, canopyRadius, height, true, speciesSelected.getViabilityAtPoint(), isAllometryOpen));
            }
            //else do nothing - no species choosen, no plant placed at this sample.
        //Point sample placement done.
        }
        System.out.println("Sampling done. Plants placed: " + plantsPlacedCount + " and samples points total: " + samples.size());
        //Setup forest
        Forest forest = new Forest(dimX, dimY);
        forest.addSpeciesMap(boxwoodMap);
        forest.addSpeciesMap(snowyMespilusMap);
        forest.addSpeciesMap(mountainPineMap);
        forest.addSpeciesMap(silverFirMap);
        forest.addSpeciesMap(silverBirchMap);
        forest.addSpeciesMap(sissileOakMap);
        forest.addSpeciesMap(europeanBeechMap);

        // overlay forest on terrain

        // export to 2D renderer
        ForestVisualizer.showForest(forest, dimX, dimY, 3);
        ForestOnTerrainVisualizer.showForestOnTerrain(forest, fileManager.getElevationGrid());


        
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
