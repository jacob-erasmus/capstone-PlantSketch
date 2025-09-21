package plantsketch.ui;

import plantsketch.*; // domain types
import java.nio.file.Path;
import java.io.File;
import java.util.Objects;

import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.DirectoryChooser;
import javafx.stage.Window;


/**
 * Main screen:
 * - When showToolbar == true: shows the original top toolbar (manual run).
 * - When showToolbar == false: wizard drives configuration; only tabs + console shown.
 * - Streams logs to a bottom console.
 * - Renders ONLY: Pink Noise, Forest, Forest + Elevation (faster).
 * - Provides a "Render another environment" button that navigates back via a callback.
 */
public class MainView extends BorderPane {

    /* ---------- Top (optional) ---------- */
    private final boolean showToolbar;
    private final TextField folderField = new TextField();
    private final Spinner<Integer> plantCount = new Spinner<>(1, 50_000, 2_000, 100);
    private final Button browseBtn = new Button("Browse…");
    private final Button runBtn = new Button("Run");
    private final Runnable onBack;

    /* ---------- Center ---------- */
    private final TabPane tabs = new TabPane();

    /* ---------- Bottom (console + actions) ---------- */
    private final ConsolePane console = new ConsolePane();
    private final Button renderAnotherBtn = new Button("Render another environment");
    private final Runnable onRenderAnotherEnvironment;

    /* ---------- Constructors ---------- */
    public MainView() { this(true, null, null); }

    public MainView(boolean showToolbar) { this(showToolbar, null, null); }

    public MainView(boolean showToolbar, Runnable onRenderAnotherEnvironment, Runnable onBack) {
        this.showToolbar = showToolbar;
        this.onRenderAnotherEnvironment = onRenderAnotherEnvironment;
        this.onBack = onBack;

        setPadding(new Insets(8));

        if (showToolbar) {
            var bar = buildToolbar();
            setTop(bar);
        }

        // center: tabs (top) + console (bottom)
        var logHeader = buildLogHeader();
        var logBox = new VBox(logHeader, console.getNode());

        var split = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);
        split.getItems().addAll(tabs, logBox);
        split.setDividerPositions(0.78);
        setCenter(split);

        // stream stdout/stderr into the console pane
        console.hookSystemStreams();
    }

    /* ---------- Public API used by MainApp (wizard handoff) ---------- */

    /**
     * Run using values collected by the wizard.
     * Sets internal fields (even when toolbar is hidden) and kicks the pipeline.
     */
    public void runSimulation(Path dataRoot, String envFolder, int sampleCount) {
        Objects.requireNonNull(dataRoot, "dataRoot");
        Objects.requireNonNull(envFolder, "envFolder");
        if (sampleCount <= 0) {
            alert("Invalid sample count.");
            return;
        }

        Path envPath = dataRoot.resolve(envFolder);
        folderField.setText(envPath.toString());
        if (plantCount.getValueFactory() != null) {
            plantCount.getValueFactory().setValue(sampleCount);
        }

        // Clear + confirm
        tabs.getTabs().clear();
        console.clear();
        console.log("✅ Configuration");
        console.log("Data root: " + dataRoot);
        console.log("Environment: " + envFolder);
        console.log("Samples: " + sampleCount);
        console.log("--------------------------------------------------");

        // Execute
        executeSimulation();
    }

    /* ---------- UI construction ---------- */

    private ToolBar buildToolbar() {
        folderField.setPrefColumnCount(36);
        browseBtn.setOnAction(e -> chooseFolder(getScene() == null ? null : getScene().getWindow()));
        runBtn.setOnAction(e -> executeSimulation());

        return new ToolBar(
            new Label("Data folder:"), folderField, browseBtn,
            new Separator(),
            new Label("Plant count:"), plantCount,
            new Separator(),
            runBtn
        );
    }


    private HBox buildLogHeader() {
        var header = new HBox();
        header.setSpacing(8);
        header.setPadding(new Insets(4, 0, 4, 0));
        var label = new Label("Log");
        var spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        renderAnotherBtn.setOnAction(e -> {
            if (onRenderAnotherEnvironment != null) {
                onRenderAnotherEnvironment.run();
            } else {
                alert("Navigator not configured.");
            }
        });

        header.getChildren().addAll(label, spacer, renderAnotherBtn);
        return header;
    }

    private void chooseFolder(Window owner) {
        DirectoryChooser dc = new DirectoryChooser();
        dc.setTitle("Select data folder");
        File f = dc.showDialog(owner);
        if (f != null) {
            folderField.setText(f.getAbsolutePath());
        }
    }

    // 


    /* ---------- Execution pipeline (UI shell) ---------- */

    private void executeSimulation() {
        if (showToolbar) runBtn.setDisable(true);
        tabs.getTabs().clear();

        try {
            final String path = folderField.getText().trim();
            if (path.isEmpty()) {
                alert("Please choose a data folder.");
                return;
            }

            final int n = plantCount.getValue();
            console.log("Loading data from: " + path);

            // Delegate the heavy lifting to a pure runner
            SimulationRunner runner = new SimulationRunner(console::log); // pass a logger
            SimulationResult result = runner.run(path, n);

            // Tabs: Pink Noise, Forest, Forest + Elevation
            tabs.getTabs().add(makeTab("Pink Noise",
                    new ScrollPane(new PinkNoiseView(result.samples(), result.dimX(), result.dimY(), result.gridSpacing()))));

            tabs.getTabs().add(makeTab("Forest",
                    new ScrollPane(new ForestView(result.forest(), result.dimX(), result.dimY(), result.gridSpacing()))));

            tabs.getTabs().add(makeTab("Forest + Elevation",
                    new ScrollPane(new ForestOnTerrainView(result.forest(), result.elevationGrid(), result.gridSpacing()))));

            console.log("\u2714 Run complete. Plants placed: " + result.forest().getAllPlants().size());

        } catch (Exception ex) {
            ex.printStackTrace();
            console.log("\u274C Run failed: " + ex.getMessage());
            alert("Run failed: " + ex.getMessage());
        } finally {
            if (showToolbar) runBtn.setDisable(false);
        }
    }

    private Tab makeTab(String name, Node content) {
        Tab t = new Tab(name, content);
        t.setClosable(false);
        return t;
    }

    private void alert(String msg) {
        new Alert(Alert.AlertType.INFORMATION, msg).showAndWait();
    }
}
