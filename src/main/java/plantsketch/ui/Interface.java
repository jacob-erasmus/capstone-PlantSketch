
package plantsketch.ui;

import plantsketch.*;
import plantsketch.util.PerformanceTimer;
import javafx.geometry.Insets;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;

/**
 * The main user interface controller for the PlantSketch ecological simulation application.
 * This is the heart of the UI system - it manages everything the user sees and interacts with.
 *
 * Key responsibilities:
 * - Creates and manages all visualization tabs (forest views, environmental maps, etc.)
 * - Handles user interactions (brush tools, species selection, parameter editing)
 * - Coordinates between the simulation engine and the visual components
 * - Manages undo/redo functionality with save states
 * - Supports both Test Mode (2x2 grid) and Run Mode (full datasets)
 *
 * Architecture:
 * - Uses JavaFX BorderPane layout: toolbar at top, tabs in center, parameter panel on right
 * - Maintains multiple view objects for different data visualizations
 * - Implements a state management system for undo/redo operations
 * - Provides interactive editing tools (brush removal, age modification, species filtering)
 *
 * The interface adapts based on the simulation mode:
 * - Test Mode: Shows editable 2x2 parameter grids for experimentation
 * - Run Mode: Shows real environmental data with brush tools and species management
 */
public class Interface extends BorderPane {

    // ==================== STATE MANAGEMENT ====================
    // Undo/redo system: maintains a history of simulation states
    private ArrayList<SimulationResult> saveStatesArray = new ArrayList<>();  // Stack of previous simulation states
    private int saveState;                                                    // Current position in the state history
    private boolean isUndo;                                                   // Flag to prevent save state creation during undo/redo
    private int maxSaveStates = 10;                                          // Maximum number of states to keep in memory

    // ==================== CORE COMPONENTS ====================
    private final Runnable onBack;                     // Callback to return to main menu
    private SimulationEngine simulationEngine;         // The brain of the simulation - handles all ecological calculations
    private SimulationResult currentResult;            // The current state of the simulation (forest, environment, etc.)
    private boolean isTestGrid;                        // True = Test Mode (2x2 grid), False = Run Mode (full datasets)
    private int sampleCount;                           // Number of plant samples to generate

    // ==================== INTERACTION MODES ====================
    // These flags track which interactive tool is currently active
    private boolean brushRemovalMode = false;         // True when user can click-drag to remove plants
    private boolean brushAgeMode = false;             // True when user can click-drag to modify plant ages

    // ==================== VISUALIZATION COMPONENTS ====================
    // Multiple view objects - each shows the forest overlaid on different environmental data
    private ForestOnMapView forestElevationView;      // Forest + elevation background
    private ForestOnMapView forestTemperatureView;    // Forest + temperature background
    private ForestOnMapView forestSunlightView;       // Forest + sunlight background
    private ForestOnMapView forestMoistureView;       // Forest + moisture background
    private ForestOnMapView forestAgeView;            // Forest + age background
    private ForestView forestView;                     // Forest-only view (no environmental background)

    // ==================== SPECIES MANAGEMENT ====================
    Supplier<Set<String>> getSelectedSpecies;         // Function that returns currently selected species names
    GridPane speciesPanelEditor;                       // UI panel for editing species parameters
    // All the ecological parameters that can be edited for each species
    String[] speciesParameters = {"sunlightC", "sunlightR", "moistureC", "moistureR",
                                  "temperatureC", "temperatureR", "slopeC", "slopeR",
                                  "maxHeightOpen", "maxHeightClosed", "q", "lifeSpan"};
    TextField[] textFields;                            // Input fields for species parameter editing

    // ==================== MAIN UI COMPONENTS ====================
    private final TabPane tabs = new TabPane();                                        // Container for all visualization tabs
    private final ConsolePane console = new ConsolePane();                             // Debug/log output area
    private final Label statusLabel = new Label();                                     // Bottom status bar (shows grid values in Test Mode)
    private final CheckBox regeneratePinkNoise = new CheckBox("Re-generate pink noise?"); // Option to create new random environmental variation

    // ==================== EDITING TOOLS ====================
    // Test Mode: Direct editing of 2x2 environmental grids
    private final Map<String, GridEditor> gridEditors = new HashMap<>();              // 2x2 grid editors for environmental parameters
    // Run Mode: Slider-based environmental modifications (unused currently)
    private final Map<String, Slider> sliders = new HashMap<>();                      // Environmental adjustment sliders
    
//*********** CONSTRUCTOR ****************\\

    /**
     * Creates the main interface for the PlantSketch simulation.
     * This sets up the entire UI system and initializes the simulation engine.
     *
     * @param onBack Callback function to return to the main menu (passed from MainApp)
     * @param mode The simulation mode string (for display in toolbar)
     * @param isTestGrid True for Test Mode (2x2 grids), False for Run Mode (full datasets)
     * @param sampleCount Number of plant samples to generate in the simulation
     */
    public Interface(Runnable onBack, String mode, boolean isTestGrid, int sampleCount) {
        // Store configuration parameters
        this.onBack = onBack;
        this.sampleCount = sampleCount;
        this.isTestGrid = isTestGrid;

        // Initialize the simulation engine - this is the core computational component
        // Pass console::log as a logging function so the engine can write to our console
        this.simulationEngine = new SimulationEngine(console::log, isTestGrid, sampleCount);

        // Initialize state management system
        isUndo = false;    // Not currently in an undo/redo operation
        saveState = 0;     // Start at the first state position

        // Build the entire user interface layout
        setupUI(mode);

        // Redirect System.out and System.err to our console pane for debugging
        console.hookSystemStreams();
    }
    

//*********** GENERAL METHODS ****************\\


    private void setupUI(String mode) {
        // Top toolbar
        setTop(buildToolbar(mode));
        
        // Center - split between tabs and console
        var logHeader = buildLogHeader();

        // this is the lhs thing with all the windows, visual panes and terminal
        var logBox = new VBox(logHeader, console.getNode());
 
        var split = new SplitPane();
        split.setOrientation(Orientation.VERTICAL);
        // the forest and stuff
        split.getItems().addAll(tabs, logBox);
        split.setDividerPositions(0.666);
        
        // Main content - tabs on left, parameter panel on right
        HBox mainContent = new HBox(10);


    // buildParameterPanel is different between both
        if (isTestGrid) mainContent.getChildren().addAll(split, buildParameterPanelTest());
        else mainContent.getChildren().addAll(split, buildParameterPanelRun());
        HBox.setHgrow(split, Priority.ALWAYS);
        
        setCenter(mainContent);
        
        // Status bar at bottom
        setBottom(buildStatusBar());
        
        setPadding(new Insets(8));
    }
    
    private ToolBar buildToolbar(String mode) {
        Button backBtn = new Button("Back to Mode Selection");
        backBtn.setOnAction(e -> {
            if (onBack != null) onBack.run();
        });

        Button perfBtn = new Button("Performance Stats");
        perfBtn.setOnAction(e -> {
            PerformanceTimer.printStats();
        });

        Label title = new Label("Test Mode: " + mode);
        title.setFont(Font.font("System", FontWeight.BOLD, 16));

        return new ToolBar(backBtn, new Separator(), perfBtn, new Separator(), title);
    }
    
    private HBox buildLogHeader() {
        HBox header = new HBox();
        header.setSpacing(8);
        header.setPadding(new Insets(4, 0, 4, 0));
        Label label = new Label("Console Log");
        header.getChildren().add(label);
        return header;
    }
    
    private HBox buildStatusBar() {
        HBox statusBar = new HBox(10);
        statusBar.setPadding(new Insets(5));
        statusBar.setStyle("-fx-background-color: #f0f0f0;");
        statusBar.setAlignment(Pos.CENTER_LEFT);
        
        statusLabel.setFont(Font.font("Monospace", 11));
        statusBar.getChildren().add(statusLabel);
        
        return statusBar;
    }

    /**
     * Creates all visualization tabs that display different environmental data layers.
     * This is the main tab creation method that gets called whenever simulation results change.
     * Each tab shows the forest overlaid on a different type of environmental data.
     */
    public void createTabs()
    {
        // Remove all existing tabs first (this prevents duplicate tabs when refreshing)
        tabs.getTabs().clear();

        // Create tabs for each environmental layer - these all use the same ForestOnMapView component
        // but with different background data layers to show how environmental conditions affect plant growth
        createForestOnMapTab(currentResult.terrain().getElevationGrid(), "Environment (Elevation)");
        createForestOnMapTab(currentResult.abiotics().getMoistureMap().getGrid(), "Moisture");
        createForestOnMapTab(currentResult.abiotics().getSunlightMap().getGrid(), "Sunlight");
        createForestOnMapTab(currentResult.abiotics().getTemperatureMap().getGrid(), "Temperature");
        createForestOnMapTab(simulationEngine.getAgeGrid(), "Age");

        // These two tabs use different visualization components (not ForestOnMapView)
        createPinkNoiseTab();  // Shows the random sampling pattern used for plant placement
        createForestTab();     // Shows just the forest without environmental background
    }
    

    /**
     * Creates a tab that displays the forest overlaid on environmental data (elevation, moisture, etc.).
     * Each environmental layer gets its own tab with identical zoom controls but different background data.
     * This method handles the repetitive setup that's needed for each environmental visualization.
     */
    private void createForestOnMapTab(float[][] map, String mapType){
        // Main container for this tab - VBox stacks zoom controls on top, map view below
        VBox mapContainer = new VBox(5); // 5px spacing between elements

        // Create zoom control bar that sits at the top of each environmental tab
        HBox zoomControls = new HBox(10); // Horizontal layout with 10px spacing
        zoomControls.setAlignment(Pos.CENTER_LEFT);  // Align controls to left side
        zoomControls.setPadding(new Insets(5));      // 5px padding around the controls

        // Create zoom buttons and label - same design across all environmental tabs
        Button zoomInBtn = new Button("+");      // Increases zoom by 20%
        Button zoomOutBtn = new Button("-");     // Decreases zoom by 20%
        Button defaultBtn = new Button("Default"); // Resets to 1:1 scale
        Label zoomLabel = new Label("100%");      // Shows current zoom percentage

        // Make buttons consistent size for better visual alignment
        zoomInBtn.setPrefSize(30, 30);   // Square buttons for +/-
        zoomOutBtn.setPrefSize(30, 30);
        defaultBtn.setPrefSize(100, 30); // Wider button for "Default" text

        // Add all zoom controls to the horizontal container in logical order
        zoomControls.getChildren().addAll(zoomOutBtn, zoomInBtn, defaultBtn, zoomLabel);

        // Create the main visualization component based on which environmental layer was requested
        // Each case creates the same type of view but stores it in a different instance variable
        // so we can update each view independently when the simulation changes
        ScrollPane mapPane = null;
        switch (mapType) {
                case "Environment (Elevation)":
                    // Create elevation view - shows forest overlaid on terrain height data
                    this.forestElevationView = new ForestOnMapView(currentResult.forest(), map, currentResult.gridSpacing());
                    mapPane = new ScrollPane(forestElevationView); // Wrap in scroll pane for panning large maps

                    // Track zoom level for this specific view (each environmental tab has independent zoom)
                    // Using array because lambda expressions need "effectively final" variables
                    final double[] zoomLevel = {1.0}; // Start at 100% zoom

                    // Zoom In: Increase size by 20% each click, capped at 3000px to prevent memory issues
                    zoomInBtn.setOnAction(e -> {
                        if(forestElevationView.getHeight() != 3000){ // Check if we're at maximum zoom
                            zoomLevel[0] = zoomLevel[0] * 1.2;          // Increase zoom by 20%
                            forestElevationView.zoomIn();               // Tell the view to zoom in
                            zoomLabel.setText(String.format("%.0f%%", zoomLevel[0] * 100)); // Update label
                        }else{
                            zoomLabel.setText("Max Zoom");              // Show we've hit the limit
                        }
                    });

                    // Zoom Out: Decrease size by 20% each click, capped at 256px to maintain readability
                    zoomOutBtn.setOnAction(e -> {
                        if(forestElevationView.getHeight() != 256){  // Check if we're at minimum zoom
                            zoomLevel[0] = zoomLevel[0] / 1.2;          // Decrease zoom by 20%
                            forestElevationView.zoomOut();              // Tell the view to zoom out
                            zoomLabel.setText(String.format("%.0f%%", zoomLevel[0] * 100)); // Update label
                        }else{
                            zoomLabel.setText("Min Zoom");              // Show we've hit the limit
                        }
                    });

                    // Reset to Default: Return to 1:1 scale (100% zoom)
                    defaultBtn.setOnAction(e -> {
                        zoomLevel[0] = 1.0;                          // Reset zoom tracking
                        forestElevationView.resetToDefault();        // Tell view to reset scale
                        zoomLabel.setText("100%");                   // Update label to show default
                    });

                    // Add zoom controls and map to the container (controls on top, map below)
                    mapContainer.getChildren().addAll(zoomControls, mapPane);
                    break;

                case "Moisture":
                    // Create moisture view - shows forest overlaid on soil moisture data
                    // This helps visualize how water availability affects plant distribution
                    this.forestMoistureView = new ForestOnMapView(currentResult.forest(), map, currentResult.gridSpacing());
                    mapPane = new ScrollPane(forestMoistureView);

                    // Independent zoom tracking for moisture tab (separate from elevation tab)
                    final double[] zoomMLevel = {1.0};

                    // Same zoom behavior as elevation tab, but operates on moisture view instance
                    zoomInBtn.setOnAction(e -> {
                        if(forestMoistureView.getHeight() != 3000){   // 3000px zoom limit
                            zoomMLevel[0] = zoomMLevel[0] * 1.2;        // 20% zoom increase
                            forestMoistureView.zoomIn();
                            zoomLabel.setText(String.format("%.0f%%", zoomMLevel[0] * 100));
                        }else{
                            zoomLabel.setText("Max Zoom");
                        }
                    });
                    zoomOutBtn.setOnAction(e -> {
                        if(forestMoistureView.getHeight() != 256){    // 256px minimum size
                            zoomMLevel[0] = zoomMLevel[0] / 1.2;        // 20% zoom decrease
                            forestMoistureView.zoomOut();
                            zoomLabel.setText(String.format("%.0f%%", zoomMLevel[0] * 100));
                        }else{
                            zoomLabel.setText("Min Zoom");
                        }
                    });
                    defaultBtn.setOnAction(e -> {
                        zoomMLevel[0] = 1.0;                          // Reset to 100%
                        forestMoistureView.resetToDefault();
                        zoomLabel.setText("100%");
                    });

                    mapContainer.getChildren().addAll(zoomControls, mapPane);
                    break;
                case "Sunlight":
                    // Create sunlight view - shows forest overlaid on light availability data
                    // Critical for understanding photosynthesis potential in different areas
                    this.forestSunlightView = new ForestOnMapView(currentResult.forest(), map, currentResult.gridSpacing());
                    mapPane = new ScrollPane(forestSunlightView);

                    // Independent zoom tracking for sunlight tab
                    final double[] zoomSLevel = {1.0};
                    zoomInBtn.setOnAction(e -> { 
                        //limit
                        if(forestSunlightView.getHeight() != 3000){
                            zoomSLevel[0] = zoomSLevel[0] * 1.2;
                            forestSunlightView.zoomIn();
                            zoomLabel.setText(String.format("%.0f%%", zoomSLevel[0] * 100));
                        }else{
                            zoomLabel.setText("Max Zoom");
                        }
                    });
                    zoomOutBtn.setOnAction(e -> {
                        if(forestSunlightView.getHeight() != 256){
                            zoomSLevel[0] = zoomSLevel[0] / 1.2;
                            forestSunlightView.zoomOut();
                            zoomLabel.setText(String.format("%.0f%%", zoomSLevel[0] * 100));
                        }else{
                            zoomLabel.setText("Min Zoom");
                        }
                    });
                    defaultBtn.setOnAction(e -> {
                        zoomSLevel[0] = 1.0;
                        forestSunlightView.resetToDefault();
                        zoomLabel.setText("100%");
                    });
                    
                    mapContainer.getChildren().addAll(zoomControls, mapPane);
                    break;
                case "Temperature":
                    // Create temperature view - shows forest overlaid on thermal data
                    // Temperature affects plant metabolism and species viability ranges
                    this.forestTemperatureView = new ForestOnMapView(currentResult.forest(), map, currentResult.gridSpacing());
                    mapPane = new ScrollPane(forestTemperatureView);

                    // Independent zoom tracking for temperature tab
                    final double[] zoomTLevel = {1.0};
                    zoomInBtn.setOnAction(e -> { 
                        //limit
                        if(forestTemperatureView.getHeight() != 3000){
                            zoomTLevel[0] = zoomTLevel[0] * 1.2;
                            forestTemperatureView.zoomIn();
                            zoomLabel.setText(String.format("%.0f%%", zoomTLevel[0] * 100));
                        }else{
                            zoomLabel.setText("Max Zoom");
                        }
                    });
                    zoomOutBtn.setOnAction(e -> {
                        if(forestTemperatureView.getHeight() != 256){
                            zoomTLevel[0] = zoomTLevel[0] / 1.2;
                            forestTemperatureView.zoomOut();
                            zoomLabel.setText(String.format("%.0f%%", zoomTLevel[0] * 100));
                        }else{
                            zoomLabel.setText("Min Zoom");
                        }
                    });
                    defaultBtn.setOnAction(e -> {
                        zoomTLevel[0] = 1.0;
                        forestTemperatureView.resetToDefault();
                        zoomLabel.setText("100%");
                    });

                    mapContainer.getChildren().addAll(zoomControls, mapPane);
                    break;
                case "Age":
                    // Create age view - shows forest overlaid on plant age distribution data
                    // Useful for understanding forest succession and growth patterns over time
                    this.forestAgeView = new ForestOnMapView(currentResult.forest(), map, currentResult.gridSpacing());
                    mapPane = new ScrollPane(forestAgeView);

                    // Independent zoom tracking for age tab
                    final double[] zoomALevel = {1.0};
                    zoomInBtn.setOnAction(e -> {
                        //limit
                        if(forestAgeView.getHeight() != 3000){
                            zoomALevel[0] = zoomALevel[0] * 1.2;
                            forestAgeView.zoomIn();
                            zoomLabel.setText(String.format("%.0f%%", zoomALevel[0] * 100));
                        }else{
                            zoomLabel.setText("Max Zoom");
                        }
                    });
                    zoomOutBtn.setOnAction(e -> {
                        if(forestAgeView.getHeight() != 256){
                            zoomALevel[0] = zoomALevel[0] / 1.2;
                            forestAgeView.zoomOut();
                            zoomLabel.setText(String.format("%.0f%%", zoomALevel[0] * 100));
                        }else{
                            zoomLabel.setText("Min Zoom");
                        }
                    });
                    defaultBtn.setOnAction(e -> {
                        zoomALevel[0] = 1.0;
                        forestAgeView.resetToDefault();
                        zoomLabel.setText("100%");
                    });

                    mapContainer.getChildren().addAll(zoomControls, mapPane);
                    break;
            }

        // Configure scroll behavior for all environmental map tabs
        // These settings ensure that large maps can be panned and scrolled properly
        mapPane.setFitToHeight(false);  // Don't auto-scale height to fit container
        mapPane.setFitToWidth(false);   // Don't auto-scale width to fit container
        mapPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Always show horizontal scrollbar
        mapPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS); // Always show vertical scrollbar

        // Allow the map pane to expand to fill available vertical space in the tab
        VBox.setVgrow(mapPane, Priority.ALWAYS);

        // Create the actual tab and add it to the tab container
        tabs.getTabs().add(makeTab(mapType, mapContainer));
    }

    /**
     * Creates the Pink Noise visualization tab.
     * This tab shows the random sampling pattern used to place plants in the simulation.
     * Pink noise creates more natural-looking random distributions than pure white noise.
     * This helps users understand how plants were initially distributed before growth simulation.
     */
    private void createPinkNoiseTab(){
        // Container setup - same pattern as environmental tabs
        VBox pinkNoiseContainer = new VBox(5);
        HBox zoomControls = new HBox(10);
        zoomControls.setAlignment(Pos.CENTER_LEFT);
        zoomControls.setPadding(new Insets(5));

        // Create zoom controls (identical to environmental tabs for consistent UI)
        Button zoomInBtn = new Button("+");
        Button zoomOutBtn = new Button("-");
        Button defaultBtn = new Button("Default");
        Label zoomLabel = new Label("100%");

        // Standard button sizing
        zoomInBtn.setPrefSize(30, 30);
        zoomOutBtn.setPrefSize(30, 30);
        defaultBtn.setPrefSize(100, 30);

        zoomControls.getChildren().addAll(zoomOutBtn, zoomInBtn, defaultBtn, zoomLabel);

        // Create pink noise visualization - shows the random sampling pattern used for plant placement
        // Uses the sample points from the simulation to show where plants were initially placed
        PinkNoiseView pinkNoiseView = new PinkNoiseView(currentResult.samples(), currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing());
        ScrollPane pinkNoisePane = new ScrollPane(pinkNoiseView);

        // Same scroll configuration as environmental tabs
        pinkNoisePane.setFitToHeight(false);
        pinkNoisePane.setFitToWidth(false);
        pinkNoisePane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        pinkNoisePane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // Track zoom level for this specific view
        final double[] zoomLevel = {1.0};
        
        // Zoom behavior - identical to environmental tabs since this follows same interaction pattern
        zoomInBtn.setOnAction(e -> {
            if(pinkNoiseView.getHeight() != 3000){   // Same 3000px limit as other visualizations
                zoomLevel[0] = zoomLevel[0] * 1.2;    // 20% increase per click
                pinkNoiseView.zoomIn();
                zoomLabel.setText(String.format("%.0f%%", zoomLevel[0] * 100));
            }else{
                zoomLabel.setText("Max Zoom");
            }
        });

        zoomOutBtn.setOnAction(e -> {
            if(pinkNoiseView.getHeight() != 256){    // Same 256px minimum as other visualizations
                zoomLevel[0] = zoomLevel[0] / 1.2;    // 20% decrease per click
                pinkNoiseView.zoomOut();
                zoomLabel.setText(String.format("%.0f%%", zoomLevel[0] * 100));
            }else{
                zoomLabel.setText("Min Zoom");
            }
        });

        defaultBtn.setOnAction(e -> {
            zoomLevel[0] = 1.0;                      // Reset to 100% scale
            pinkNoiseView.resetToDefault();
            zoomLabel.setText("100%");
        });

        // Assemble the tab components and add to tab container
        pinkNoiseContainer.getChildren().addAll(zoomControls, pinkNoisePane);
        VBox.setVgrow(pinkNoisePane, Priority.ALWAYS);  // Allow pane to expand vertically
        tabs.getTabs().add(makeTab("Pink Noise", pinkNoiseContainer));
    }
    
    /**
     * Creates the Forest-only visualization tab.
     * This tab shows just the forest without any environmental background data.
     * It's simpler and faster to render than the environmental overlay tabs,
     * making it ideal for focusing purely on forest structure and species distribution.
     */
    private void createForestTab(){
        // Same container setup pattern as other tabs
        VBox forestContainer = new VBox(5);
        HBox zoomControls = new HBox(10);
        zoomControls.setAlignment(Pos.CENTER_LEFT);
        zoomControls.setPadding(new Insets(5));

        // Standard zoom control buttons
        Button zoomInBtn = new Button("+");
        Button zoomOutBtn = new Button("-");
        Button defaultBtn = new Button("Default");
        Label zoomLabel = new Label("100%");

        // Consistent button sizing across all tabs
        zoomInBtn.setPrefSize(30, 30);
        zoomOutBtn.setPrefSize(30, 30);
        defaultBtn.setPrefSize(100, 30);

        zoomControls.getChildren().addAll(zoomOutBtn, zoomInBtn, defaultBtn, zoomLabel);

        // Create forest-only view - shows plants as colored circles on black background
        // This is much simpler than ForestOnMapView since there's no environmental background to render
        forestView = new ForestView(currentResult.forest(), currentResult.dimX(), currentResult.dimY(), currentResult.gridSpacing());
        ScrollPane forestViewPane = new ScrollPane(forestView);

        // Standard scroll pane configuration
        forestViewPane.setFitToHeight(false);
        forestViewPane.setFitToWidth(false);
        forestViewPane.setHbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);
        forestViewPane.setVbarPolicy(ScrollPane.ScrollBarPolicy.ALWAYS);

        // Zoom level tracking for forest view
        final double[] zoomLevel = {1.0};

        // Forest tab zoom handlers - identical behavior to other tabs
        zoomInBtn.setOnAction(e -> {
            if(forestView.getHeight() != 3000){      // Standard zoom limit
                zoomLevel[0] = zoomLevel[0] * 1.2;    // 20% increase
                forestView.zoomIn();
                zoomLabel.setText(String.format("%.0f%%", zoomLevel[0] * 100));
            }else{
                zoomLabel.setText("Max Zoom");
            }
        });

        zoomOutBtn.setOnAction(e -> {
            if(forestView.getHeight() != 256){       // Standard minimum size
                zoomLevel[0] = zoomLevel[0] / 1.2;    // 20% decrease
                forestView.zoomOut();
                zoomLabel.setText(String.format("%.0f%%", zoomLevel[0] * 100));
            }else{
                zoomLabel.setText("Min Zoom");
            }
        });

        defaultBtn.setOnAction(e -> {
            zoomLevel[0] = 1.0;                      // Reset to 100%
            forestView.resetToDefault();
            zoomLabel.setText("100%");
        });

        // Assemble and add the forest tab
        forestContainer.getChildren().addAll(zoomControls, forestViewPane);
        VBox.setVgrow(forestViewPane, Priority.ALWAYS);  // Allow vertical expansion
        tabs.getTabs().add(makeTab("Forest", forestContainer));
    }

    /**
     * Updates all visualization tabs when simulation results change.
     * This is called after each simulation run or when switching between saved states.
     * It recreates all the visualization tabs with new data while preserving user settings.
     */
    private void updateVisualization() {
        // Clear all existing tabs to prevent duplicates when refreshing
        tabs.getTabs().clear();

        // Recreate all visualization tabs with updated simulation data
        createTabs();

        // Update bottom status panel (only needed in Test Mode for environmental parameter display)
        if(isTestGrid) updateStatusDisplay();

        // Log the total plant count to console for debugging and user feedback
        console.log("Plants: " + currentResult.forest().getAllPlants().size());
    }


    /**
     * Initializes the simulation with a specific mode selected from the startup screen.
     * This method handles the transition from mode selection to actually running the simulation.
     * Different modes provide different environmental configurations for testing or real data.
     */
    public void initializeWithMode(String mode) {
        // Clear console from previous sessions
        console.clear();

        // Test Mode: Uses 2x2 grid with randomized or preset environmental conditions
        if (isTestGrid)
        {
            switch (mode) {
                case "random":
                    executeSimulation(0, false, null);  // 0 = random environmental parameters
                    break;
                case "preset1":
                    executeSimulation(1, false, null);  // 1 = first predefined environment
                    break;
                case "preset2":
                    executeSimulation(2, false, null);  // 2 = second predefined environment
                    break;
            }
        }
        // Run Mode: Uses real environmental data from preset datasets or custom folders
        else
        {
            switch (mode){
                case "preset1":
                    executeSimulation(1, false, null);  // D1-256: 256x256 grid dataset
                    break;
                case "preset2":
                    executeSimulation(2, false, null);  // D2-512: 512x512 grid dataset
                    break;
                case "preset3":
                    executeSimulation(3, false, null);  // D3-1024: 1024x1024 grid dataset
                case "preset4":
                    executeSimulation(4, false, null);  // D4-1024: Alternative 1024x1024 dataset
                case "chooseFolder":
                    // Initialize engine for custom folder loading
                    simulationEngine = new SimulationEngine(console::log, isTestGrid, sampleCount);
                    console.log("Custom folder mode - waiting for folder data...");
                    executeSimulation(5, false, null);  // 5 = custom folder mode
            }
        }
    }

    /**
     * Executes a simulation with the specified configuration.
     * This is the core method that actually runs the ecological simulation and updates the UI.
     *
     * @param choice The simulation preset (0=random, 1-4=presets, 5=custom folder)
     * @param isResimulation Whether this is a re-run of existing configuration
     * @param fullPath Custom folder path (used when choice=5)
     */
    private void executeSimulation(int choice, boolean isResimulation, String fullPath) {
        // Start performance monitoring for this simulation run
        PerformanceTimer.start("execute_simulation");

        // Clear any existing visualization tabs before creating new ones
        tabs.getTabs().clear();

        try {
            // Only save state for undo/redo if this isn't part of an undo operation
            if (!isUndo)
            {
                // Clean up redo history when user makes a new change (creates a new "branch")
                // If we're not at the latest state, remove all states after current position
                if(saveStatesArray.size() -1 > saveState)
                {
                    // Remove all future states since user has made a change
                    for (int i = saveState; i < saveStatesArray.size(); i++)
                    {
                        saveStatesArray.remove(i);
                    }
                }

                // Handle different types of simulation execution
                if(isResimulation)
                {
                    // This is a re-run with modified parameters (not initial simulation)
                    saveState++;  // Advance to next state position
                    console.log("Re-simulating with new parameters...");

                    if(isTestGrid)
                    {
                        // Read user-modified environmental parameters from UI
                        boolean wasChange = readGridEditors();

                        // Only re-run simulation if there were actual changes or pink noise regeneration requested
                        if (regeneratePinkNoise.isSelected()) {
                            currentResult = simulationEngine.runChange(regeneratePinkNoise.isSelected(), false);
                        } else if (wasChange) {
                            currentResult = simulationEngine.runChange(regeneratePinkNoise.isSelected(), false);
                        }
                    }
                }
                else
                {
                    // This is an initial simulation run with a specific preset configuration
                    currentResult = simulationEngine.run(choice, fullPath);
                }

                // Save the simulation result for undo/redo functionality
                saveStatesArray.add(currentResult);

                // Limit memory usage by capping the number of saved states
                if (saveStatesArray.size() > maxSaveStates) // maximum 30 save states
                {
                    saveStatesArray.remove(1); // Keep original forest but remove oldest iteration
                    saveState--; // Adjust index to stay within bounds
                }
            }

            // Handle special case for undo/redo in Run Mode
            if (isUndo && !isTestGrid && isResimulation){
                // Re-run simulation with previous state for undo functionality
                currentResult = simulationEngine.runChange(false, true);
            }

            // Update UI components with new simulation results
            if (isTestGrid)
            {
                // Test Mode: Update parameter editors and status display
                updateGridEditors();     // Sync UI controls with current environmental values
                updateStatusDisplay();   // Update bottom status panel
            }

            // Update species management panel with current forest state
            updateSpeciesPanelEditor();

            // Log species distribution to console for user feedback
            int numSpecies = 0;
            for (SpeciesMap sm : currentResult.forest().getOverallSpeciesMap())
            {
                // Show count and name for each species in the simulation
                console.log(" - " + sm.getPlants().size() + "  " + sm.getSpecies().getName() );
                if (sm.getPlants().size() > 0) numSpecies++;  // Count only species with living plants
            }

            console.log("Number of species: " + numSpecies);

            // Reset undo flag and refresh all visualization tabs
            isUndo = false;
            updateVisualization();

        } catch (Exception ex)
        {
            // Handle simulation errors gracefully
            ex.printStackTrace();
            console.log("✗ Simulation failed: " + ex.getMessage());
        } finally {
            // Always stop performance timer, even if simulation failed
            PerformanceTimer.end("execute_simulation");
        }
    }

    /**
     * Helper method to create a non-closable tab with specified name and content.
     * All visualization tabs are non-closable to maintain a consistent UI structure.
     */
    private Tab makeTab(String name, Node content) {
        Tab t = new Tab(name, content);
        t.setClosable(false);  // Prevent users from accidentally closing visualization tabs
        return t;
    }

    /**
     * Undoes the last simulation change by reverting to the previous saved state.
     * This allows users to step backwards through their simulation history.
     */
    private void undo()
    {
        // Can only undo if we have multiple states and aren't at the beginning
        if(saveStatesArray.size() > 1 && saveState > 0)
        {
            // Mark this as an undo operation to prevent creating new save states
            isUndo = true;

            // Move back one step in the history
            saveState--;
            System.out.println("Save State: " + saveState);

            // Restore the previous simulation state
            currentResult = saveStatesArray.get(saveState);
            this.simulationEngine.loadSaveState(currentResult);

            // Re-execute to update UI (choice parameter ignored for undo operations)
            executeSimulation(0, true, null);
        }

    }

    /*
        so the way undo and redo are going to work is this:
            say you work for 6 save states
            then undo 2
            you can now redo those two undos and get back to 6 save states
            BUT
            as soon as you make a new change it will make a new 'branch' and you lose all the old save states that you undid and you cannot redo them

     */
    private void redo()
    {
        if(saveStatesArray.size() > 1 && saveState +1 < saveStatesArray.size())
        {
            isUndo = true;

            saveState++;
            System.out.println("Save State: " + saveState);
            currentResult = saveStatesArray.get(saveState);
            this.simulationEngine.loadSaveState(currentResult);
            executeSimulation(0, true, null); // choice doesnt matter because it
        }
    }

    // makes the species panel editors
    private GridPane createSpeciesPanelEditor(Species species)
    {

        speciesPanelEditor = new GridPane();
        speciesPanelEditor.setHgap(10);
        speciesPanelEditor.setVgap(8);

        float[] parameterValues = {species.getSunlightC(), species.getSunlightR(), species.getMoistureC(), species.getMoistureR(), 
            species.getTemperatureC(), species.getTemperatureR(), species.getSlopeC(), species.getSlopeR(), species.getMaxHeightOpen(),
            species.getMaxHeightClosed(), species.getQ(), species.getLifeSpan()};

        textFields = new TextField[speciesParameters.length];

        // how tf are we supposed to directly change viability? Maybe add that to the brush or something?

        int row = 1;
        for (int i = 0; i < speciesParameters.length; i++) {
            int index = i;  // final copy for lambda

            Label titleLabel = new Label(speciesParameters[i]);
            titleLabel.setPrefWidth(200);
            speciesPanelEditor.add(titleLabel, 0, index);

            TextField parameterValue = new TextField();
            parameterValue.setPromptText(String.valueOf(parameterValues[index]));
            parameterValue.setPrefWidth(200);

// POTENTIALLY NOT WORKING
            parameterValue.textProperty().addListener((obs, oldText, newText) -> {
                if (!newText.trim().isEmpty()) {
                    try {
                        float value = Float.parseFloat(newText.trim());

                        // use index instead of i
                        if (index == 0) species.setSunlightC(value);
                        else if (index == 1) species.setSunlightR(value);
                        else if (index == 2) species.setMoistureC(value);
                        else if (index == 3) species.setMoistureR(value);
                        else if (index == 4) species.setTemperatureC(value);
                        else if (index == 5) species.setTemperatureR(value);
                        else if (index == 6) species.setSlopeC(value);
                        else if (index == 7) species.setSlopeR(value);
                        else if (index == 8) species.setMaxHeightOpen(value);
                        else if (index == 9) species.setMaxHeightClosed(value);
                        else if (index == 10) species.setQ(value);
                        else if (index == 11) species.setLifeSpan(value);

                    } catch (NumberFormatException ex) {
                        System.out.println("Invalid float: " + newText);
                    }
                }
            });

            speciesPanelEditor.add(parameterValue, 1, index);
            textFields[i] = parameterValue;
        }


        return speciesPanelEditor;

    }

// NOT WORKING
    public GridPane updateSpeciesPanelEditor()
    {
        for (int i = 0; i < simulationEngine.getSpeciesList().size(); i++)
        {
            float[] parameterValues = {simulationEngine.getSpeciesList().get(i).getSunlightC(), simulationEngine.getSpeciesList().get(i).getSunlightR(), simulationEngine.getSpeciesList().get(i).getMoistureC(), simulationEngine.getSpeciesList().get(i).getMoistureR(),
            simulationEngine.getSpeciesList().get(i).getTemperatureC(), simulationEngine.getSpeciesList().get(i).getTemperatureR(), simulationEngine.getSpeciesList().get(i).getSlopeC(), simulationEngine.getSpeciesList().get(i).getSlopeR(), simulationEngine.getSpeciesList().get(i).getMaxHeightOpen(),
            simulationEngine.getSpeciesList().get(i).getMaxHeightClosed(), simulationEngine.getSpeciesList().get(i).getQ(), simulationEngine.getSpeciesList().get(i).getLifeSpan()};

            for (int j = 0; j < speciesParameters.length; j++) {
            textFields[j].setPromptText(String.valueOf(parameterValues[j]));
        }
        }


        

        return null;
    }

    public Button updateSpeciesParametersButton()
    {
        Button updateSpeciesParametersButton = new Button ("Update species parameters");

        updateSpeciesParametersButton.setOnAction(e -> {
            
            for (Species species : simulationEngine.getSpeciesList())
            {
                createSpeciesPanelEditor(species);
            }
            
            executeSimulation(0, true, null);
        }
            );
            
        return updateSpeciesParametersButton;
    }


//*********** RUN VIEW METHODS ****************\\
    

    public void initializeWithCustomFolder(Path dataRoot, String envFolder) {
        try {
            String fullPath = dataRoot.resolve(envFolder).toString();
            console.log("Initializing with custom folder: " + fullPath);

            executeSimulation(5, false, fullPath);
            
        } catch (Exception e) {
            console.log("Failed to load custom folder: " + e.getMessage());
            e.printStackTrace();
        }
}

    private VBox buildParameterPanelRun(){


        // putting it all together
        Accordion accordion = new Accordion();

        Accordion innerAccordion = new Accordion();

        accordion.getPanes().add(createAbioticsPanel());

        for (Species species : simulationEngine.getSpeciesList())
        {

            GridPane speciesParametersPanel = createSpeciesPanelEditor(species);

            TitledPane speciesHeader = new TitledPane(species.getName(), speciesParametersPanel);
            speciesHeader.setExpanded(false);

            innerAccordion.getPanes().add(speciesHeader);
        };

        TitledPane overallSpecies = new TitledPane("Species Editing", innerAccordion);
        overallSpecies.setExpanded(false);

        accordion.getPanes().addAll(createSpeciesPanel(), overallSpecies);  

        VBox container = new VBox(accordion,updateSpeciesParametersButton(), createStatesPanel());
        return container;

    }

    // undo and save stuff (states)
    private ScrollPane createStatesPanel()
    {

        HBox stateButtons = createStateButtons();

        // undo and save menu
        VBox stateContent = new VBox(10);
        stateContent.getChildren().addAll
        (
            stateButtons);

        ScrollPane statesPane = new ScrollPane();
        statesPane.setFitToWidth(true);
        statesPane.setFitToHeight(true);
        statesPane.setContent(stateContent);

        return statesPane;
    }

    private TitledPane createSpeciesPanel()
    {
        // species stuff:
        final Map<String, CheckBox> speciesCheck = new HashMap<>();

        CheckBox boxwood = new CheckBox("Boxwood");
        boxwood.setStyle("-fx-text-fill: red;");
        speciesCheck.put("Boxwood", boxwood);

        CheckBox snowyMespilus = new CheckBox("Snowy Mespilus");
        snowyMespilus.setStyle("-fx-text-fill: blue;");
        speciesCheck.put("Snowy Mespilus", snowyMespilus);

        CheckBox mountainPine = new CheckBox("Mountain Pine");
        mountainPine.setStyle("-fx-text-fill: green;");
        speciesCheck.put("Mountain Pine", mountainPine);

        CheckBox silverFir = new CheckBox("Silver Fir");
        silverFir.setStyle("-fx-text-fill: purple;");
        speciesCheck.put("Silver Fir", silverFir);

        CheckBox silverBirch = new CheckBox("Silver Birch");
        silverBirch.setStyle("-fx-text-fill: pink;");
        speciesCheck.put("Silver Birch", silverBirch);

        CheckBox sissileOak = new CheckBox("Sissile Oak");
        sissileOak.setStyle("-fx-text-fill: orange;");
        speciesCheck.put("Sissile Oak", sissileOak);

        CheckBox europeanBeech = new CheckBox("European Beech");
        europeanBeech.setStyle("-fx-text-fill: brown;");
        speciesCheck.put("European Beech", europeanBeech);

        Button simulateBtn = new Button("Selected Species Only");
        simulateBtn.setOnAction(e -> {
            saveState++;
            saveStatesArray.add(currentResult);
                if (saveStatesArray.size() > 20) // maximum 20 save states
                {
                    saveStatesArray.remove(1); // keeps the original forest but removes the first iteration on top of that
                    saveState--; // the index now caps out at 20

                }
            removeSpecies(currentResult, speciesCheck);});
        simulateBtn.setPrefWidth(250);

        GridPane gridPane = new GridPane();
        int col = 0, row = 0;
        
        for (CheckBox boxes : speciesCheck.values()) {
            boxes.setSelected(true);
            gridPane.add(boxes, col, row);
            col++;
            if (col > 1) { // wrap after 2 columns
                col = 0;
                row++;
            }
        }
        // define the supplier that always reflects current checkbox states
        this.getSelectedSpecies = () -> speciesCheck.entrySet().stream()
            .filter(entry -> entry.getValue().isSelected())
            .map(Map.Entry::getKey)
            .collect(Collectors.toSet());
        // species window
        VBox speciesContent = new VBox(10);
        speciesContent.getChildren().addAll
        (
            gridPane,
            new Separator(),
            simulateBtn,
            new Separator());

        ScrollPane speciesPane = new ScrollPane();
        speciesPane.setFitToWidth(true);
        speciesPane.setFitToHeight(true);
        speciesPane.setContent(speciesContent);

        TitledPane speciesHeader = new TitledPane("Species Selecting", speciesPane);
        speciesHeader.setExpanded(false);

        return speciesHeader;
    }

    private TitledPane createAbioticsPanel()
    {
         // abiotic stuff:
        Label temp = new Label("Temperature: Set Amount to Add or Subtract (Min = "+ simulationEngine.getMinTemp() +"  ; Max = "+ simulationEngine.getMaxTemp() +")");
        Slider tempSlider = new Slider(-15, 15, 0);
        tempSlider.setShowTickLabels(true);
        tempSlider.setShowTickMarks(true);
        tempSlider.setBlockIncrement(0.5);
        tempSlider.setMajorTickUnit(0.5);
        tempSlider.setSnapToTicks(true);
        tempSlider.setPrefWidth(400);
        
        Label age = new Label("Age: Set Amount to Add or Subtract (Min = "+ simulationEngine.getMinAge() +"  ; Max = "+ simulationEngine.getMaxAge() +")");
        Slider ageSlider = new Slider(-650, 650, 0);
        ageSlider.setShowTickLabels(true);
        ageSlider.setShowTickMarks(true);
        ageSlider.setBlockIncrement(25);
        ageSlider.setMajorTickUnit(25);
        ageSlider.setSnapToTicks(true);
        ageSlider.setPrefWidth(250);

        Label sun = new Label("Sunlight: Set Amount to Add or Subtract (Min = "+ simulationEngine.getMinSun() +"  ; Max = "+ simulationEngine.getMaxSun() +")");
        Slider sunSlider = new Slider(-13, 13, 0);
        sunSlider.setShowTickLabels(true);
        sunSlider.setShowTickMarks(true);
        sunSlider.setBlockIncrement(1);
        sunSlider.setMajorTickUnit(0.5);
        sunSlider.setSnapToTicks(true);
        sunSlider.setPrefWidth(250);

        Label moist = new Label("Moisture: Set Amount to Add or Subtract (Min = "+ simulationEngine.getMinMoist() +"  ; Max = "+ simulationEngine.getMaxMoist() +")");
        Slider moistSlider = new Slider(-54, 54, 0);
        moistSlider.setShowTickLabels(true);
        moistSlider.setShowTickMarks(true);
        moistSlider.setBlockIncrement(1);
        moistSlider.setMajorTickUnit(1);
        moistSlider.setSnapToTicks(true);
        moistSlider.setPrefWidth(250);

        Label elevation = new Label("Elevation: Set Amount to Add or Subtract (Min = "+ simulationEngine.getMinElev() +"  ; Max = "+ simulationEngine.getMaxElev() +")");
        Slider elevationSlider = new Slider(-200, 200, 0);
        elevationSlider.setShowTickLabels(true);
        elevationSlider.setShowTickMarks(true);
        elevationSlider.setBlockIncrement(10);
        elevationSlider.setMajorTickUnit(10);
        elevationSlider.setSnapToTicks(true);
        elevationSlider.setPrefWidth(250);

        // brush stuff
        Label brushSize = new Label("Brush Size");
        Slider brushSizeSlider = new Slider(10, 90, 50);
        brushSizeSlider.setShowTickLabels(true);
        brushSizeSlider.setShowTickMarks(true);
        brushSizeSlider.setMajorTickUnit(20);
        brushSizeSlider.setBlockIncrement(20);
        brushSizeSlider.setPrefWidth(250);
        brushSizeSlider.setSnapToTicks(true);
        brushSizeSlider.setMinorTickCount(0);
        brushSizeSlider.showTickLabelsProperty();
        
        //listen for changes
        brushSizeSlider.valueProperty().addListener((obs, oldVal, newVal) -> {
            if (brushRemovalMode){
                switch(tabs.getSelectionModel().getSelectedItem().getText()){
                    case("Environment (Elevation)"):
                        updateBrushCursor(forestElevationView, newVal.doubleValue());
                        break;
                    case("Temperature"):
                        updateBrushCursor(forestTemperatureView, newVal.doubleValue());
                        break;
                    case("Sunlight"):
                        updateBrushCursor(forestSunlightView, newVal.doubleValue());
                        break;
                    case("Moisture"):
                        updateBrushCursor(forestMoistureView, newVal.doubleValue());
                        break;
                    case("Age"):
                        updateBrushCursor(forestAgeView, newVal.doubleValue());
                        break;
                }
                
            }
        });

        //listen for tab change, bringover brush changes and set brush off.
        tabs.getSelectionModel().selectedItemProperty().addListener((obs, oldTab, newTab) -> {
            if (newTab != null) {
                brushRemovalMode = false;
                brushAgeMode = false;
                saveState++;
                saveStatesArray.add(currentResult);
                if (saveStatesArray.size() > 20) // maximum 20 save states
                {
                    saveStatesArray.remove(1); // keeps the original forest but removes the first iteration on top of that
                    saveState--; // the index now caps out at 20

                }
                console.log("\nNumber of Remaining Plants: " + currentResult.forest().getAllPlants().size());
                switch(newTab.getText()){
                    case("Environment (Elevation)"):
                            forestElevationView.disableBrushMode();
                            forestElevationView.setCursor(Cursor.DEFAULT);  
                            forestElevationView.draw();
                            break;
                        case("Temperature"):
                            forestTemperatureView.disableBrushMode();
                            forestTemperatureView.setCursor(Cursor.DEFAULT);  
                            forestTemperatureView.draw();
                            break;
                        case("Sunlight"):
                            forestSunlightView.disableBrushMode();
                            forestSunlightView.setCursor(Cursor.DEFAULT);  
                            forestSunlightView.draw();
                            break;
                        case("Moisture"):
                            forestMoistureView.disableBrushMode();
                            forestMoistureView.setCursor(Cursor.DEFAULT);
                            forestMoistureView.draw();
                            break;
                        case("Age"):
                            forestAgeView.disableBrushMode();
                            forestAgeView.setCursor(Cursor.DEFAULT);
                            forestAgeView.draw();
                            break;
                        case("Pink Noise"):
                            System.out.println("Brush functions aren't applicabe for Pink Noise tab <-> FOR VISUAL PURPOSES ONLY");
                            break;
                        case("Forest"):
                            forestView.draw();
                            System.out.println("Brush functions can't be applied directly on Forest tab, only carried over <-> FOR VISUAL PURPOSES ONLY");
                            break;
                }
            }
        });

        Button enableBrushRemovalBtn = new Button("Toggle Brush Removal Mode");
        enableBrushRemovalBtn.setOnAction(e -> 
            {
            if (!brushRemovalMode){
                saveState++;
                saveStatesArray.add(currentResult);
                if (saveStatesArray.size() > 20) // maximum 20 save states
                {
                    saveStatesArray.remove(1); // keeps the original forest but removes the first iteration on top of that
                    saveState--; // the index now caps out at 20
                }
            }
            switch(tabs.getSelectionModel().getSelectedItem().getText()){
                    case("Environment (Elevation)"):
                        forestElevationView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushRemoval(forestElevationView, brushSizeSlider);
                        break;
                    case("Temperature"):
                        forestTemperatureView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushRemoval(forestTemperatureView, brushSizeSlider);
                        break;
                    case("Sunlight"):
                        forestSunlightView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushRemoval(forestSunlightView, brushSizeSlider);
                        break;
                    case("Moisture"):
                        forestMoistureView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushRemoval(forestMoistureView, brushSizeSlider);
                        break;
                    case("Age"):
                        forestAgeView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushRemoval(forestAgeView, brushSizeSlider);
                        break;
                    case("Pink Noise"):
                        System.out.println("Brush functions are disabled for Pink Noise tab <-> FOR VISUAL PURPOSES ONLY");
                        break;
                    case("Forest"):
                        System.out.println("Brush functions are disabled for Forest tab <-> FOR VISUAL PURPOSES ONLY");
                        break;
            }    });
        enableBrushRemovalBtn.setPrefWidth(250);

        Button enableAgeChangeBtn = new Button("Toggle Brush Age Mode");
        enableAgeChangeBtn.setOnAction(e -> 
            {
            if (!brushAgeMode){
                saveState++;
                saveStatesArray.add(currentResult);
                if (saveStatesArray.size() > 20) // maximum 20 save states
                {
                    saveStatesArray.remove(1); // keeps the original forest but removes the first iteration on top of that
                    saveState--; // the index now caps out at 20
                }
            }
            switch(tabs.getSelectionModel().getSelectedItem().getText()){
                    case("Environment (Elevation)"):
                        forestElevationView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushAge(forestElevationView, brushSizeSlider, ageSlider);
                        break;
                    case("Temperature"):
                        forestTemperatureView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushAge(forestTemperatureView, brushSizeSlider, ageSlider);
                        break;
                    case("Sunlight"):
                        forestSunlightView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushAge(forestSunlightView, brushSizeSlider, ageSlider);
                        break;
                    case("Moisture"):
                        forestMoistureView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushAge(forestMoistureView, brushSizeSlider, ageSlider);
                        break;
                    case("Age"):
                        forestAgeView.setSelectedSpeciesSupplier(getSelectedSpecies);
                        brushAge(forestAgeView, brushSizeSlider, ageSlider);
                        break;
                    case("Pink Noise"):
                        System.out.println("Brush functions are disabled for Pink Noise tab <-> FOR VISUAL PURPOSES ONLY");
                        break;
                    case("Forest"):
                        System.out.println("Brush functions are disabled for Forest tab <-> FOR VISUAL PURPOSES ONLY");
                        break;
            }    });
        enableAgeChangeBtn.setPrefWidth(250);
        // menu for abiotics
        VBox abioticContent = new VBox(10);
        abioticContent.getChildren().addAll
        (
            elevation,
            elevationSlider,
            new Separator(),
            temp,
            tempSlider,
            new Separator(),
            age,
            ageSlider,
            new Separator(),
            sun,
            sunSlider,
            new Separator(),
            moist,
            moistSlider,
            new Separator(),
            brushSize,
            brushSizeSlider,
            enableBrushRemovalBtn,
            enableAgeChangeBtn,
            new Separator());

        ScrollPane slidersPane = new ScrollPane();
        slidersPane.setFitToWidth(true);
        slidersPane.setFitToHeight(true);
        slidersPane.setContent(abioticContent);

        TitledPane slidersHeader = new TitledPane("Abiotics Editing & Brush", slidersPane);
        slidersHeader.setExpanded(false);

        return slidersHeader;
    }

    private void brushRemoval(ForestOnMapView mapView, Slider brushSizeSlider){
        brushRemovalMode = !brushRemovalMode; // toggle on/off
        if (brushRemovalMode){
            mapView.enableBrushRemovalMode(() -> brushSizeSlider.getValue());
            updateBrushCursor(mapView, brushSizeSlider.getValue());
        }else{
            mapView.disableBrushMode();
            mapView.setCursor(Cursor.DEFAULT);  
            console.log("\nNumber of Remaining Plants: " + currentResult.forest().getAllPlants().size());
        }
        
    }

    private void brushAge(ForestOnMapView mapView, Slider brushSlider, Slider ageSlider){
        brushAgeMode = !brushAgeMode;
        if(brushAgeMode){
            mapView.enableBrushAgeMode((() -> brushSlider.getValue()), (() -> ageSlider.getValue()), simulationEngine);
            updateBrushCursor(mapView, brushSlider.getValue());
        }else{
            mapView.disableBrushMode();
            mapView.setCursor(Cursor.DEFAULT);  
            console.log("\nNumber of Remaining Plants: " + currentResult.forest().getAllPlants().size());
        }
    }
    private void updateBrushCursor(ForestOnMapView mapView, double brushSize){
//image as cursor - some glitches so commented out for now.
//Will fix/improve.
        //Image brushImage = new Image(getClass().getResource("/101064.png").toExternalForm());
        //double scale = brushSize;
        //Image scaledImg = new Image(brushImage.getUrl(), brushImage.getWidth() * scale, brushImage.getHeight() * scale, true, true);
        //forestElevationView.setCursor(new ImageCursor(scaledImg, scaledImg.getWidth()/2, scaledImg.getHeight()/2));
//basic crosshair cursor but no visual resize shown but functionally yes. 
        mapView.setCursor(Cursor.CROSSHAIR);

        
    }
    
    private void removeSpecies(SimulationResult result, Map<String, CheckBox> speciesCheck){
        long startTime = System.nanoTime();
        for (CheckBox boxes : speciesCheck.values()) {
            if(boxes.isSelected()!=true && result.forest().removedSpecies.containsKey(boxes.getText()) != true){
                result.forest().removeSpecies(boxes.getText());
            }else if(boxes.isSelected()){
                if(result.forest().removedSpecies.containsKey(boxes.getText())){
                    result.forest().addSpeciesMapByName(boxes.getText());

                    //moved saved state to the button call
                }          
            }
        }
        refreshForestViews();
        console.log("Species Filter Remove and Visualise Elapsed Time: " + (System.nanoTime() - startTime) + " nanoseconds.");
    }

    private void refreshForestViews() {
        PerformanceTimer.start("refresh_forest_views");

        // Only redraw forest-related views, not environment maps
        if (forestElevationView != null) forestElevationView.draw();
        if (forestTemperatureView != null) forestTemperatureView.draw();
        if (forestSunlightView != null) forestSunlightView.draw();
        if (forestMoistureView != null) forestMoistureView.draw();
        if (forestAgeView != null) forestAgeView.draw();
        if (forestView != null) forestView.draw();

        PerformanceTimer.end("refresh_forest_views");
    }

    public float readSlider(Slider slider)
    {
        return (float) slider.getValue();
    }
    
    private HBox createStateButtons()
    {
        Button undoButton = new Button("Undo");
        undoButton.setOnAction(e -> undo());
        undoButton.setPrefWidth(60);

        Button redoButton = new Button("Redo");
        redoButton.setOnAction(e -> redo());
        redoButton.setPrefWidth(60);

        TextField fileNameField = new TextField();
        fileNameField.setPromptText("Enter file name:");
        fileNameField.setPrefWidth(120);

        Button saveButton = new Button("Create .pdb file");
        saveButton.setOnAction(e -> {
            String name = fileNameField.getText().trim();
            if (!name.isEmpty()) 
            {
                new EcoVizOutput(currentResult).createFile( "src/outputPdbSaves/"+ name+".pdb");
            }});

        saveButton.setPrefWidth(100);

        Button[] stateButtons = {undoButton, redoButton, saveButton};

        HBox stateButtonRow = new HBox(5); // spacing of 5px between buttons
        stateButtonRow.getChildren().addAll(stateButtons);
        stateButtonRow.getChildren().add(fileNameField);

        return stateButtonRow;
    }


//*********** TEST VIEW METHODS****************\\
    

    // create test view abitic grids
    private ScrollPane createTestAbioticGrids()
    {
        VBox parameterPanel = new VBox(10);
        parameterPanel.setPadding(new Insets(10));
        parameterPanel.setPrefWidth(500);
        parameterPanel.setStyle("-fx-border-color: #ca9292ff; -fx-border-width: 1;");
        
        ScrollPane scrollPane = new ScrollPane();
        scrollPane.setContent(parameterPanel);
        scrollPane.setFitToWidth(true);
        scrollPane.setPrefHeight(700);
        
        // This creates the empty grids without the values inside
        gridEditors.put("Temperature", new GridEditor("Temperature (°C)", 
            simulationEngine.getMinTemp(), simulationEngine.getMaxTemp()));
        gridEditors.put("Age", new GridEditor("Age (years)", 
            simulationEngine.getMinAge(), simulationEngine.getMaxAge()));
        gridEditors.put("Moisture", new GridEditor("Moisture (%)", 
            simulationEngine.getMinMoist(), simulationEngine.getMaxMoist()));
        gridEditors.put("Sunlight", new GridEditor("Sunlight (hours)", 
            simulationEngine.getMinSun(), simulationEngine.getMaxSun()));
        gridEditors.put("Elevation", new GridEditor("Elevation (m)", 
            simulationEngine.getMinElev(), simulationEngine.getMaxElev()));
        gridEditors.put("Slope", new GridEditor("Slope (degrees)", 
            simulationEngine.getMinSlope(), simulationEngine.getMaxSlope()));
        
        GridPane gridPane = new GridPane();
        gridPane.setHgap(15);  // space between columns
        gridPane.setVgap(10);  // space between rows
        gridPane.setPadding(new Insets(10));

        // making two columns next to eachother of grid editors
       int col = 0, row = 0;
        for (GridEditor editor : gridEditors.values()) {
            gridPane.add(editor, col, row);

            col++;
            if (col > 1) { // wrap after 2 columns
                col = 0;
                row++;
            }
        }

        Button simulateBtn = new Button("Simulate with new parameters");
        simulateBtn.setPrefWidth(250);
        simulateBtn.setOnAction(e -> executeSimulation(0, true, null));


        VBox panelContent = new VBox(10);
        panelContent.getChildren().addAll
        (
            gridPane,
            new Separator(),
            regeneratePinkNoise,
            // regenerateSpecies,
            simulateBtn);
        

        scrollPane.setContent(panelContent);
        return scrollPane;
    }




    // puts together the parameter panel for the testing grids
    private VBox buildParameterPanelTest() {

        ScrollPane scrollPane = createTestAbioticGrids();

        Accordion accordion = new Accordion();

        TitledPane abioticsHeader = new TitledPane("Abiotics Editing", scrollPane);
        abioticsHeader.setExpanded(true);

        accordion.getPanes().add(abioticsHeader);


        SpeciesDictionary dict = new SpeciesDictionary();
        List<Species> speciesList = List.of(
                dict.loadBoxwood(),
                dict.loadSnowyMespilus(),
                dict.loadMountainPine(),
                dict.loadSilverFir(),
                dict.loadSilverBirch(),
                dict.loadSissileOak(),
                dict.loadEuropeanBeech());

        for (Species species : speciesList)
        {

            GridPane speciesParametersPanel = createSpeciesPanelEditor(species);

            TitledPane speciesHeader = new TitledPane(species.getName(), speciesParametersPanel);
            speciesHeader.setExpanded(false);

            accordion.getPanes().add(speciesHeader);
        };

        VBox container = new VBox(accordion);

        container.getChildren().addAll(updateSpeciesParametersButton(), createStateButtons());

        return container;
    }
        
    // puts in the values for the right tab
    private void updateGridEditors() 
    {
        gridEditors.get("Temperature").setValues(simulationEngine.getTemperatureGrid());
        gridEditors.get("Age").setValues(simulationEngine.getAgeGrid());
        gridEditors.get("Moisture").setValues(simulationEngine.getMoistureGrid());
        gridEditors.get("Sunlight").setValues(simulationEngine.getSunlightGrid());
        gridEditors.get("Elevation").setValues(simulationEngine.getElevationGrid());
        gridEditors.get("Slope").setValues(simulationEngine.getSlopeGrid());
    }

    // reads in values from the screen grids
    private boolean readGridEditors()
    {
        boolean wasChange = false;

        float[][] newTemp = gridEditors.get("Temperature").getValues();
        float[][] newAge = gridEditors.get("Age").getValues();
        float[][] newMoist = gridEditors.get("Moisture").getValues();
        float[][] newSun = gridEditors.get("Sunlight").getValues();
        float[][] newElev = gridEditors.get("Elevation").getValues();

        if(gridEditors.get("Temperature").isEdited()) 
        {
            simulationEngine.setTemperatureGrid(newTemp);
            wasChange = true;
        }
        if(gridEditors.get("Age").isEdited()) 
        {
            simulationEngine.setAgeGrid(newAge);
            wasChange = true;
        }        
        if(gridEditors.get("Moisture").isEdited()) 
        {
            simulationEngine.setMoistureGrid(newMoist);
            wasChange = true;
        }        
        if(gridEditors.get("Sunlight").isEdited()) 
        {
            simulationEngine.setSunlightGrid(newSun);
            wasChange = true;
        }        
        // Elevation editing disabled for performance - slope recalculation is expensive
        if(gridEditors.get("Elevation").isEdited())
        {
            console.log("Warning: Elevation editing disabled for performance reasons");
            // Reset elevation values to original state
            gridEditors.get("Elevation").setValues(simulationEngine.getElevationGrid());
        }

        return wasChange;
    }

    // This is the bottom status bar
    private void updateStatusDisplay() 
    {
        StringBuilder status = new StringBuilder();
        status.append("Grid Status | ");
        status.append("Samples: ").append(simulationEngine.getSampleCount()).append(" | ");
        status.append("Pink Noise: ").append(simulationEngine.getNumPinkNoise()).append(" | ");
        status.append("Plants: ").append(simulationEngine.getNumPlants()).append("\n");
        
        status.append("Temp: ").append(formatGrid(simulationEngine.getTemperatureGrid())).append(" | ");
        status.append("Age: ").append(formatGrid(simulationEngine.getAgeGrid())).append(" | ");
        status.append("Moist: ").append(formatGrid(simulationEngine.getMoistureGrid())).append("\n");
        status.append("Sun: ").append(formatGrid(simulationEngine.getSunlightGrid())).append(" | ");
        status.append("Elev: ").append(formatGrid(simulationEngine.getElevationGrid())).append(" | ");
        status.append("Slope: ").append(formatGrid(simulationEngine.getSlopeGrid()));
        
        statusLabel.setText(status.toString());
    }
    
    // formats the grid values for display in the bottom status bar
    private String formatGrid(float[][] grid) {
        if (grid == null || grid.length < 2) return "N/A";
        return String.format("[%.1f,%.1f;%.1f,%.1f]", grid[0][0], grid[1][0], grid[0][1], grid[1][1]);
    }

}