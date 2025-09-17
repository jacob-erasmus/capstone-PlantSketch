package plantsketch.ui;

import plantsketch.*; // your domain classes

import javafx.geometry.Insets;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Main screen:
 * - When showToolbar == true: shows the original top toolbar (manual run mode)
 * - When showToolbar == false: no toolbar; wizard drives it; we stream logs to the bottom console.
 * - Includes "Render another environment" button that navigates back to the wizard via a callback.
 */
public class MainView extends BorderPane {

    // Top controls (shown only if showToolbar == true)
    private final TextField folderField = new TextField();
    private final Button browseBtn = new Button("Browse…");
    private final Spinner<Integer> plantCount = new Spinner<>(1, 50_000, 500, 100);
    private final Button runBtn = new Button("Run");

    // Center visuals
    private final TabPane tabs = new TabPane();

    // Bottom: live log console + actions
    private final TextArea logArea = new TextArea();
    private final Button renderAnotherBtn = new Button("Render another environment");
    private boolean consoleHooked = false;

    private final boolean showToolbar;
    private final Runnable onRenderAnotherEnvironment; // navigator callback

    public MainView() {
        this(true, null);
    }

    public MainView(boolean showToolbar) {
        this(showToolbar, null);
    }

    public MainView(boolean showToolbar, Runnable onRenderAnotherEnvironment) {
        this.showToolbar = showToolbar;
        this.onRenderAnotherEnvironment = onRenderAnotherEnvironment;

        setPadding(new Insets(8));

        if (showToolbar) {
            var bar = new ToolBar(
                    new Label("Data folder:"), folderField, browseBtn,
                    new Separator(),
                    new Label("Plant count:"), plantCount,
                    new Separator(),
                    runBtn);
            folderField.setPrefColumnCount(36);
            setTop(bar);
            browseBtn.setOnAction(e -> chooseFolder(getScene() == null ? null : getScene().getWindow()));
            runBtn.setOnAction(e -> runSimulation());
        }

        // Log area
        logArea.setEditable(false);
        logArea.setWrapText(false);
        logArea.setStyle("-fx-font-family: Consolas, 'Courier New', monospace; -fx-font-size: 12px;");

        // Bottom panel: header with "Log" label + spacer + "Render another environment" button
        var logHeader = new HBox();
        logHeader.setSpacing(8);
        logHeader.setPadding(new Insets(4, 0, 4, 0));
        var logLabel = new Label("Log");
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        renderAnotherBtn.setOnAction(e -> {
            if (onRenderAnotherEnvironment != null) {
                onRenderAnotherEnvironment.run();
            } else {
                alert("Navigator not configured.");
            }
        });
        logHeader.getChildren().addAll(logLabel, spacer, renderAnotherBtn);

        var logBox = new VBox(logHeader, logArea);

        // Center + bottom as a vertical split: tabs (top) and log console (bottom)
        var split = new SplitPane();
        split.setOrientation(javafx.geometry.Orientation.VERTICAL);
        split.getItems().addAll(tabs, logBox);
        split.setDividerPositions(0.78); // more space for visuals
        setCenter(split);

        // Redirect System.out/err once
        hookConsoleIfNeeded();
    }

    /** Run using the wizard selections. Sets the implicit fields and reuses pipeline. */
    public void runSimulation(Path dataRoot, String envFolder, int sampleCount) {
        if (dataRoot == null || envFolder == null || sampleCount <= 0) {
            alert("Invalid configuration received from wizard.");
            return;
        }
        Path envPath = dataRoot.resolve(envFolder);

        // Even if toolbar is hidden, reuse the same pipeline fields
        folderField.setText(envPath.toString());
        if (plantCount.getValueFactory() != null) {
            plantCount.getValueFactory().setValue(sampleCount);
        }

        // Clear visuals/logs and show a confirmation banner
        tabs.getTabs().clear();
        clearLog();
        log("✅ Configuration");
        log("Data root: " + dataRoot);
        log("Environment: " + envFolder);
        log("Samples: " + sampleCount);
        log("--------------------------------------------------");

        runSimulation(); // call the existing no-arg pipeline
    }

    private void chooseFolder(Window owner) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select data folder");
        File f = dc.showDialog(owner);
        if (f != null) {
            folderField.setText(f.getAbsolutePath());
        }
    }

    /**
     * Pipeline: load, then render ONLY:
     *  - Pink Noise
     *  - Forest
     *  - Forest + Elevation
     *
     * Previously shown heatmaps (Age/Moisture/Temperature/Sunlight/Elevation) are removed for speed.
     */
    private void runSimulation() {
        if (showToolbar) runBtn.setDisable(true);
        tabs.getTabs().clear();

        hookConsoleIfNeeded(); // ensure logs stream

        try {
            String path = folderField.getText().trim();
            if (path.isEmpty()) {
                alert("Please choose a data folder.");
                return;
            }

            // 1) Load data (streams FileManager's printlns into the log)
            log("Loading data from: " + path);
            FileManager fm = new FileManager();
            fm.fileFinder(path);

            int dimX = fm.getDimX();
            int dimY = fm.getDimY();
            float gridSpacing = fm.getGridSpacing();

            // 2) Build domain objects (no intermediate heatmap tabs)
            TemperatureMap temperatureMap = new TemperatureMap(dimX, dimY, gridSpacing, fm.getTemperatureGrid());
            MoistureMap moistureMap = new MoistureMap(dimX, dimY, gridSpacing, fm.getMoistureGrid());
            SunlightMap sunlightMap = new SunlightMap(dimX, dimY, gridSpacing, fm.getSunlightGrid());
            AbioticFactors abiotic = new AbioticFactors(moistureMap, temperatureMap, sunlightMap);
            Terrain terrain = new Terrain(dimX, dimY, gridSpacing, abiotic, fm.getElevationGrid());
            AgeMap ageMap = new AgeMap(dimX, dimY, gridSpacing, fm.getAgeGrid());

            // 3) Pink noise samples (tab: Pink Noise)
            int n = plantCount.getValue();
            float metersX = dimX * gridSpacing;
            float metersY = dimY * gridSpacing;
            PinkNoiseSampler sampler = new PinkNoiseSampler(metersX, metersY, 2.0f, 42L);
            List<PointSample> samples = sampler.generateSamples(n);
            tabs.getTabs().add(makeTab("Pink Noise", new PinkNoiseView(samples, dimX, dimY, gridSpacing)));

            // 4) Species + viability → build Forest
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
            SpeciesMap mapSnowy   = new SpeciesMap(allSpecies.get(1));
            SpeciesMap mapPine    = new SpeciesMap(allSpecies.get(2));
            SpeciesMap mapFir     = new SpeciesMap(allSpecies.get(3));
            SpeciesMap mapBirch   = new SpeciesMap(allSpecies.get(4));
            SpeciesMap mapSissile = new SpeciesMap(allSpecies.get(5));
            SpeciesMap mapBeech   = new SpeciesMap(allSpecies.get(6));

            ViabilityCalculator calc = new ViabilityCalculator(terrain, abiotic);
            Random r = new Random();
            int placed = 0;
            List<Species> candidates = new ArrayList<>();

            for (PointSample s : samples) {
                int xCell = (int) (s.getX() / gridSpacing);
                int yCell = (int) (s.getY() / gridSpacing);

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
                if (candidates.isEmpty()) continue;
                if (density > r.nextFloat()) continue;

                Table[] wheel = new Table[candidates.size()];
                float cumulativeWeights = 0f;
                for (int i = 0; i < candidates.size(); i++) {
                    cumulativeWeights += candidates.get(i).getViabilityAtPoint();
                    wheel[i] = new Table(candidates.get(i), cumulativeWeights);
                }
                Species chosen = new RouletteWheelSelector(cumulativeWeights).selectSpecies(wheel, wheel.length);
                if (chosen == null) continue;

                float cohortAge = ageMap.getAge(xCell, yCell);
                float cap = Math.min(cohortAge, chosen.getLifeSpan());
                float plantAge = r.nextFloat() * cap * chosen.getViabilityAtPoint();

                boolean isOpen = density > 0.8f;
                float height = new GrowthFunction().calculateSize(chosen, plantAge, isOpen);
                float canopy = height * (isOpen ? chosen.getRadiusMultiplierOpen() : chosen.getRadiusMultiplierClosed());

                Plant p = new Plant(++placed, s.getX(), s.getY(), plantAge, chosen, canopy, height, true,
                        chosen.getViabilityAtPoint(), isOpen);

                switch (chosen.getName()) {
                    case "Boxwood"         -> mapBoxwood.setPlantAt(p);
                    case "Snowy Mespilus"  -> mapSnowy.setPlantAt(p);
                    case "Mountain Pine"   -> mapPine.setPlantAt(p);
                    case "Silver Fir"      -> mapFir.setPlantAt(p);
                    case "Silver Birch"    -> mapBirch.setPlantAt(p);
                    case "Sissile Oak"     -> mapSissile.setPlantAt(p);
                    case "European Beech"  -> mapBeech.setPlantAt(p);
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

            // 5) Only these two renderings:
            tabs.getTabs().add(makeTab("Forest", new ForestView(forest, dimX, dimY, gridSpacing)));
            tabs.getTabs().add(makeTab("Forest + Elevation", new ForestOnTerrainView(forest, fm.getElevationGrid(), gridSpacing)));

            log("✅ Run complete. Plants placed: " + forest.getAllPlants().size());

        } catch (Exception ex) {
            ex.printStackTrace();
            log("❌ Run failed: " + ex.getMessage());
            alert("Run failed: " + ex.getMessage());
        } finally {
            if (showToolbar) runBtn.setDisable(false);
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

    /* ----------------------- Console wiring ----------------------- */

    private void hookConsoleIfNeeded() {
        if (consoleHooked) return;
        consoleHooked = true;

        PrintStream outPs = new PrintStream(new TextAreaOutputStream(logArea), true, StandardCharsets.UTF_8);
        System.setOut(outPs);

        PrintStream errPs = new PrintStream(new TextAreaOutputStream(logArea), true, StandardCharsets.UTF_8);
        System.setErr(errPs);
    }

    private void log(String s) {
        if (s == null) return;
        logArea.appendText(s + System.lineSeparator());
    }

    private void clearLog() {
        logArea.clear();
    }

    /** OutputStream that appends to the TextArea on the JavaFX Application Thread. */
    private static class TextAreaOutputStream extends OutputStream {
        private final TextArea area;
        private final StringBuilder buffer = new StringBuilder();

        TextAreaOutputStream(TextArea area) {
            this.area = area;
        }

        @Override
        public void write(int b) {
            char c = (char) (b & 0xFF);
            buffer.append(c);
            if (c == '\n') {
                flushBuffer();
            }
        }

        @Override
        public void write(byte[] b, int off, int len) {
            String s = new String(b, off, len, StandardCharsets.UTF_8);
            buffer.append(s);
            int idx;
            while ((idx = buffer.indexOf("\n")) >= 0) {
                String line = buffer.substring(0, idx + 1);
                appendToArea(line);
                buffer.delete(0, idx + 1);
            }
        }

        @Override
        public void flush() {
            flushBuffer();
        }

        private void flushBuffer() {
            if (buffer.length() > 0) {
                String s = buffer.toString();
                appendToArea(s);
                buffer.setLength(0);
            }
        }

        private void appendToArea(String s) {
            if (s == null || s.isEmpty()) return;
            if (javafx.application.Platform.isFxApplicationThread()) {
                area.appendText(s);
            } else {
                javafx.application.Platform.runLater(() -> area.appendText(s));
            }
        }
    }
}
