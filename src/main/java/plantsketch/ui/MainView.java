package plantsketch.ui;

import plantsketch.*; // reuse your domain classes

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.BorderPane;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

public class MainView extends BorderPane {

    // Top controls
    private final TextField folderField = new TextField();
    private final Button browseBtn = new Button("Browse…");
    private final Spinner<Integer> plantCount = new Spinner<>(1, 50_000, 500, 100);
    private final Button runBtn = new Button("Run");

    // Center tabs
    private final TabPane tabs = new TabPane();

    public MainView() {
        setPadding(new Insets(8));

        var bar = new ToolBar(
                new Label("Data folder:"), folderField, browseBtn,
                new Separator(),
                new Label("Plant count:"), plantCount,
                new Separator(),
                runBtn);
        folderField.setPrefColumnCount(36);
        setTop(bar);
        setCenter(tabs);

        browseBtn.setOnAction(e -> chooseFolder(getScene() == null ? null : getScene().getWindow()));
        runBtn.setOnAction(e -> runSimulation());
    }

    private void chooseFolder(Window owner) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select data folder");
        File f = dc.showDialog(owner);
        if (f != null) {
            folderField.setText(f.getAbsolutePath());
        }
    }

    private void runSimulation() {
        runBtn.setDisable(true);
        tabs.getTabs().clear();

        try {
            String path = folderField.getText().trim();
            if (path.isEmpty()) {
                alert("Please choose a data folder.");
                return;
            }

            // 1) Load data (using a new overload we add to FileManager below)
            FileManager fm = new FileManager();
            fm.fileFinder(path);

            int dimX = fm.getDimX();
            int dimY = fm.getDimY();
            float gridSpacing = fm.getGridSpacing();

            // 2) Show grids
            if (fm.getAgeGrid() != null)
                tabs.getTabs().add(makeTab("Age", new GridHeatMapView(fm.getAgeGrid())));
            if (fm.getMoistureGrid() != null)
                tabs.getTabs().add(makeTab("Moisture", new GridHeatMapView(fm.getMoistureGrid())));
            if (fm.getTemperatureGrid() != null)
                tabs.getTabs().add(makeTab("Temperature", new GridHeatMapView(fm.getTemperatureGrid())));
            if (fm.getSunlightGrid() != null)
                tabs.getTabs().add(makeTab("Sunlight", new GridHeatMapView(fm.getSunlightGrid())));
            if (fm.getElevationGrid() != null)
                tabs.getTabs().add(makeTab("Elevation", new GridHeatMapView(fm.getElevationGrid())));

            // 3) Build domain objects
            TemperatureMap temperatureMap = new TemperatureMap(dimX, dimY, gridSpacing, fm.getTemperatureGrid());
            MoistureMap moistureMap = new MoistureMap(dimX, dimY, gridSpacing, fm.getMoistureGrid());
            SunlightMap sunlightMap = new SunlightMap(dimX, dimY, gridSpacing, fm.getSunlightGrid());
            AbioticFactors abiotic = new AbioticFactors(moistureMap, temperatureMap, sunlightMap);
            Terrain terrain = new Terrain(dimX, dimY, gridSpacing, abiotic, fm.getElevationGrid());
            AgeMap ageMap = new AgeMap(dimX, dimY, gridSpacing, fm.getAgeGrid());

            // 4) Pink noise samples
            int n = plantCount.getValue();
            float metersX = dimX * gridSpacing;
            float metersY = dimY * gridSpacing;
            PinkNoiseSampler sampler = new PinkNoiseSampler(metersX, metersY, 2.0f, 42L);
            List<PointSample> samples = sampler.generateSamples(n);
            tabs.getTabs().add(makeTab("Pink Noise", new PinkNoiseView(samples, dimX, dimY)));

            // 5) Species + viability + placement (same logic as your main)
            SpeciesDictionary dict = new SpeciesDictionary();
            List<Species> allSpecies = List.of(
                    dict.loadBoxwood(),
                    dict.loadSnowyMespilus(),
                    dict.loadMountainPine(),
                    dict.loadSilverFir(),
                    dict.loadSilverBirch(),
                    dict.loadSissileOak(),
                    dict.loadEuropeanBeech());

            SpeciesMap mapBoxwood = new SpeciesMap(allSpecies.get(0));
            SpeciesMap mapSnowy = new SpeciesMap(allSpecies.get(1));
            SpeciesMap mapPine = new SpeciesMap(allSpecies.get(2));
            SpeciesMap mapFir = new SpeciesMap(allSpecies.get(3));
            SpeciesMap mapBirch = new SpeciesMap(allSpecies.get(4));
            SpeciesMap mapSissile = new SpeciesMap(allSpecies.get(5));
            SpeciesMap mapBeech = new SpeciesMap(allSpecies.get(6));

            ViabilityCalculator calc = new ViabilityCalculator(terrain, abiotic);
            Random r = new Random();
            int placed = 0;
            List<Species> candidates = new ArrayList<>();

            for (PointSample s : samples) {
                int xCell = (int) (s.getX()/gridSpacing);
                int yCell = (int) (s.getY()/gridSpacing);

                float density = 0f;
                float sumV = 0f;
                candidates.clear();

                for (Species sp : allSpecies) {
                    float v = calc.viabililty(sp, xCell, yCell);
                    sp.setViabilityAtPoint(v);
                    if (v > 0) {
                        candidates.add(sp);
                        density = Math.max(density, v);
                        sumV += v;
                    }
                }
                if (candidates.isEmpty())
                    continue;

                if (density > r.nextFloat())
                    continue;

                Table[] wheel = new Table[candidates.size()];
                float cumulativeWeights = 0f;
                for (int i = 0; i < candidates.size(); i++) {
                    cumulativeWeights += candidates.get(i).getViabilityAtPoint();
                    wheel[i] = new Table(candidates.get(i), cumulativeWeights);
                }
                Species chosen = new RouletteWheelSelector(cumulativeWeights).selectSpecies(wheel, wheel.length);
                if (chosen == null)
                    continue;

                float cohortAge = ageMap.getAge(xCell, yCell);
                float cap = Math.min(cohortAge, chosen.getLifeSpan());
                float plantAge = r.nextFloat() * cap * chosen.getViabilityAtPoint();

                boolean isOpen = density > 0.8f;
                float height = new GrowthFunction().calculateSize(chosen, plantAge, isOpen);
                float canopy = height
                        * (isOpen ? chosen.getRadiusMultiplierOpen() : chosen.getRadiusMultiplierClosed());

                Plant p = new Plant(++placed, s.getX(), s.getY(), plantAge, chosen, canopy, height, true,
                        chosen.getViabilityAtPoint(), isOpen);

                switch (chosen.getName()) {
                    case "Boxwood" -> mapBoxwood.setPlantAt(p);
                    case "Snowy Mespilus" -> mapSnowy.setPlantAt(p);
                    case "Mountain Pine" -> mapPine.setPlantAt(p);
                    case "Silver Fir" -> mapFir.setPlantAt(p);
                    case "Silver Birch" -> mapBirch.setPlantAt(p);
                    case "Sissile Oak" -> mapSissile.setPlantAt(p);
                    case "European Beech" -> mapBeech.setPlantAt(p);
                }
            }

            Forest forest = new Forest(dimX, dimY);
            forest.addSpeciesMap(mapBoxwood);
            forest.addSpeciesMap(mapSnowy);
            forest.addSpeciesMap(mapPine);
            forest.addSpeciesMap(mapFir);
            forest.addSpeciesMap(mapBirch);
            forest.addSpeciesMap(mapSissile);
            forest.addSpeciesMap(mapBeech);

            tabs.getTabs().add(makeTab("Forest", new ForestView(forest, dimX, dimY, 3)));
            tabs.getTabs().add(makeTab("Forest + Elevation", new ForestOnTerrainView(forest, fm.getElevationGrid())));

        } catch (Exception ex) {
            ex.printStackTrace();
            alert("Run failed: " + ex.getMessage());
        } finally {
            runBtn.setDisable(false);
        }
    }

    private Tab makeTab(String name, javafx.scene.Node content) {
        Tab t = new Tab(name, new ScrollPane(content));
        t.setClosable(false);
        return t;
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
