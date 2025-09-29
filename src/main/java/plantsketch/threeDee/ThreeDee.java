package plantsketch.threeDee;

import javafx.application.Application;
import javafx.scene.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.*;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;
import javafx.stage.Stage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;
import plantsketch.FileManager;
import plantsketch.Plant;
import plantsketch.Species;
import plantsketch.SpeciesDictionary;

import java.util.*;

public class ThreeDee extends Application {
    private static final double CAMERA_INITIAL_DISTANCE = -800;
    private static final double CAMERA_NEAR_CLIP = 0.1;
    private static final double CAMERA_FAR_CLIP = 50000.0;
    private static final double ROTATION_SPEED = 0.2;
    private static final double ZOOM_SPEED = 2.0;
    private static final double PAN_SPEED = 0.2;
    
    private double mouseX, mouseY;
    private double mouseOldX, mouseOldY;

    // Terrain data
    private float[][] elevations;
    private int dimX, dimY;
    private float gridSpacing = 1.0f;
    private float yHeight;

    // Plant data
    private List<Plant> plants = new ArrayList<>();

    // Species dictionary
    private Map<String, Species> speciesMap = new HashMap<>();

    // 3D scene components
    private Group root3D;
    private SubScene subScene;
    private PerspectiveCamera camera;
    private Rotate rotateX = new Rotate(-30, Rotate.X_AXIS);
    private Rotate rotateY = new Rotate(0, Rotate.Y_AXIS);
    private Translate cameraPosition = new Translate(0, -100, CAMERA_INITIAL_DISTANCE);
    private Translate cameraPan;


    private static String dataFolder;

    public static void setDataFolder(String folderPath) {
        dataFolder = folderPath;
    }


    @Override
    public void start(Stage primaryStage) {
        initializeSampleData();

        root3D = new Group();
        buildTerrain();
        cameraPan = new Translate(18, yHeight, 10);
        buildPlants();
        addLighting();

        // Camera setup
        camera = new PerspectiveCamera(true);
        camera.setNearClip(CAMERA_NEAR_CLIP);
        camera.setFarClip(CAMERA_FAR_CLIP);
        camera.getTransforms().addAll(cameraPan, rotateY, rotateX, cameraPosition);

        // Subscene
        subScene = new SubScene(root3D, 1024, 768, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.LIGHTBLUE);
        subScene.setCamera(camera);

        Group mainRoot = new Group(subScene);
        Scene scene = new Scene(mainRoot, 1024, 768, true);
        scene.setFill(Color.DARKGRAY);

        handleMouse(scene);

        primaryStage.setTitle("3D Plant Ecosystem Viewer");
        primaryStage.setScene(scene);
        primaryStage.show();
    }

    /** Loads terrain + plant data from FileManager and builds species dictionary. */
    private void initializeSampleData() {
        if (dataFolder == null) {
            System.err.println("No data folder specified!");
            return;
        }

        FileManager fm = new FileManager();
        fm.fileFinder(dataFolder);  // pass folder directly

        elevations = fm.getElevationGrid();
        gridSpacing = fm.getGridSpacing();
        dimX = fm.getDimX();
        dimY = fm.getDimY();
        plants = fm.getPlants();

        // Load dictionary
        SpeciesDictionary dict = new SpeciesDictionary();
        speciesMap.put("buse", dict.loadBoxwood());
        speciesMap.put("amov", dict.loadSnowyMespilus());
        speciesMap.put("pimu", dict.loadMountainPine());
        speciesMap.put("abal", dict.loadSilverFir());
        speciesMap.put("bepe", dict.loadSilverBirch());
        speciesMap.put("qupe", dict.loadSissileOak());
        speciesMap.put("fasy", dict.loadEuropeanBeech());
    }

    private void buildTerrain() {
        TriangleMesh mesh = new TriangleMesh();

        // Calculate terrain bounds for centering
        float terrainWidth = (dimX - 1) * gridSpacing;
        float terrainDepth = (dimY - 1) * gridSpacing;

        // Add vertices - positioned at Y = -500 as requested
        for (int z = 0; z < dimY; z++) {
            for (int x = 0; x < dimX; x++) {
                float xPos = x * gridSpacing - terrainWidth / 2;
                float yPos = -elevations[x][z] - 0;
                float zPos = z * gridSpacing - terrainDepth / 2;
                mesh.getPoints().addAll(xPos, yPos, zPos);
                if(z==0) yHeight = yPos;
            }
        }

        // Add texture coordinates (required)
        mesh.getTexCoords().addAll(0, 0);

        // Add faces (two triangles per grid cell)
        for (int z = 0; z < dimY - 1; z++) {
            for (int x = 0; x < dimX - 1; x++) {
                int topLeft = z * dimX + x;
                int topRight = topLeft + 1;
                int bottomLeft = (z + 1) * dimX + x;
                int bottomRight = bottomLeft + 1;

                // First triangle (top-left to bottom-left to top-right)
                mesh.getFaces().addAll(topLeft, 0, bottomLeft, 0, topRight, 0);

                // Second triangle (top-right to bottom-left to bottom-right)
                mesh.getFaces().addAll(topRight, 0, bottomLeft, 0, bottomRight, 0);
            }
        }

        // Create mesh view with better material
        MeshView terrain = new MeshView(mesh);
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(Color.TAN); 
        material.setSpecularColor(Color.SANDYBROWN);
        material.setSpecularPower(20.0);
        terrain.setMaterial(material);
        terrain.setDrawMode(DrawMode.FILL);
        
        // Make sure terrain is visible by not culling any faces
        terrain.setCullFace(CullFace.NONE);

        root3D.getChildren().add(terrain);
        
        // Add coordinate axes for debugging
        // addCoordinateAxes();
    }

    private void buildPlants() {
        for (Plant plant : plants) {
            float terrainHeight = getTerrainHeightAt(plant.getX(), plant.getY());
            
            // Calculate world position (centered around origin)
            float worldX = plant.getX() - ((dimX - 1) * gridSpacing / 2);
            float worldZ = plant.getY() - ((dimY - 1) * gridSpacing / 2);
            
            Group treeGroup = createTree(plant, terrainHeight, worldX, worldZ);
            root3D.getChildren().add(treeGroup);
        }
    }

    private Group createTree(Plant plant, float terrainHeight, float worldX, float worldZ) {
        Group treeGroup = new Group();
        
        // Trunk - proportional to tree height
        float trunkHeight = plant.getHeight() * 0.6f; // Trunk is 60% of total height
        float trunkRadius = Math.max(0.1f, plant.getHeight() * 0.03f); // Minimum radius
        
        Cylinder trunk = new Cylinder(trunkRadius, trunkHeight);
        
        // Position trunk so its BOTTOM is at terrain height and it extends UPWARD
        // In JavaFX: POSITIVE Y is DOWN, so we need to position carefully
        trunk.setTranslateX(worldX);
 trunk.setTranslateY(-terrainHeight - trunkHeight / 2);
//        trunk.setTranslateY(terrainHeight + trunkHeight / 2); // Position on terrain
        trunk.setTranslateZ(worldZ);
        trunk.setMaterial(new PhongMaterial(Color.SADDLEBROWN));

        // Canopy - properly sized and positioned on top of trunk
        Node canopy = createSpeciesCanopy(plant, trunkHeight);
        canopy.setTranslateX(worldX);
 canopy.setTranslateY(-terrainHeight - trunkHeight);
//        canopy.setTranslateY(terrainHeight + trunkHeight); // Position at top of trunk
        canopy.setTranslateZ(worldZ);

        treeGroup.getChildren().addAll(trunk, canopy);
        return treeGroup;
    }

    /** Creates different canopy shapes for different species */
    private Node createSpeciesCanopy(Plant plant, float trunkHeight) {
        String species = plant.getMnemonic();
        
        // Much smaller canopy proportions
        double canopyHeight = plant.getHeight() * 0.4f; // Canopy is 40% of total height
        double canopyRadius = Math.max(0.5, plant.getHeight() * 0.15f); // Much smaller radius
        
        PhongMaterial material = new PhongMaterial();
        material.setDiffuseColor(getSpeciesColor(species));
        material.setSpecularColor(Color.WHITE);

        Node canopy;
        
        switch (species) {
            case "pimu": // Mountain Pine - conical shape
                Cone cone = new Cone(canopyRadius, canopyHeight);
                cone.setMaterial(material);
                // Position cone so its base is at trunk top
                cone.setTranslateY(-canopyHeight / 2);
                canopy = cone;
                break;
                
            case "abal": // Silver Fir - tall conical shape
                Cone tallCone = new Cone(canopyRadius * 0.8, canopyHeight * 1.3);
                tallCone.setMaterial(material);
                tallCone.setTranslateY(-canopyHeight * 0.65);
                canopy = tallCone;
                break;
                
            case "bepe": // Silver Birch - spherical shape
                Sphere birchCanopy = new Sphere(canopyRadius * 0.8);
                birchCanopy.setMaterial(material);
                birchCanopy.setTranslateY(-canopyRadius * 0.8);
                canopy = birchCanopy;
                break;
                
            case "qupe": // Sessile Oak - broad spherical shape
                Sphere oakCanopy = new Sphere(canopyRadius);
                oakCanopy.setMaterial(material);
                oakCanopy.setTranslateY(-canopyRadius);
                canopy = oakCanopy;
                break;
                
            case "fasy": // European Beech - egg shape
                Sphere beechCanopy = new Sphere(canopyRadius * 0.8);
                beechCanopy.setMaterial(material);
                beechCanopy.setTranslateY(-canopyRadius * 0.8);
                beechCanopy.setScaleY(1.4);
                canopy = beechCanopy;
                break;
                
            case "buse": // Boxwood - cubic/bushy shape
                Box bush = new Box(canopyRadius * 1.2, canopyHeight * 0.6, canopyRadius * 1.2);
                bush.setMaterial(material);
                bush.setTranslateY(-canopyHeight * 0.3);
                canopy = bush;
                break;
                
            case "amov": // Snowy Mespilus - multi-layer spherical
                Group layers = new Group();
                double layerSpacing = canopyHeight / 4;
                for (int i = 0; i < 3; i++) {
                    double layerRadius = canopyRadius * (1.0 - i * 0.15);
                    Sphere layer = new Sphere(layerRadius);
                    layer.setMaterial(material);
                    layer.setTranslateY(-i * layerSpacing - layerRadius);
                    layers.getChildren().add(layer);
                }
                canopy = layers;
                break;
                
            default: // Default spherical canopy
                Sphere defaultCanopy = new Sphere(canopyRadius);
                defaultCanopy.setMaterial(material);
                defaultCanopy.setTranslateY(-canopyRadius);
                canopy = defaultCanopy;
        }
        
        return canopy;
    }

    /** Custom Cone class that creates a proper cone shape */
    private class Cone extends Group {
        PhongMaterial material = new PhongMaterial();
        public Cone(double radius, double height) {
            TriangleMesh mesh = new TriangleMesh();
            
            int divisions = 16;
            
            // Add apex point (top of cone)
            mesh.getPoints().addAll(0, (float)(-height/2), 0);
            
            // Add base points
            for (int i = 0; i < divisions; i++) {
                double angle = 2 * Math.PI * i / divisions;
                float x = (float)(radius * Math.cos(angle));
                float z = (float)(radius * Math.sin(angle));
                mesh.getPoints().addAll(x, (float)(height/2), z);
            }
            
            // Add center of base
            mesh.getPoints().addAll(0f, (float)(height/2), 0f);
            
            // Texture coordinates
            mesh.getTexCoords().addAll(0, 0);
            
            // Create side faces
            for (int i = 0; i < divisions; i++) {
                int next = (i + 1) % divisions;
                mesh.getFaces().addAll(
                    0, 0,           // apex
                    i + 1, 0,       // current base point
                    next + 1, 0     // next base point
                );
            }
            
            // Create base face
            int baseCenter = divisions + 1;
            for (int i = 0; i < divisions; i++) {
                int next = (i + 1) % divisions;
                mesh.getFaces().addAll(
                    baseCenter, 0,   // base center
                    next + 1, 0,     // next base point  
                    i + 1, 0         // current base point
                );
            }
            
            MeshView coneMesh = new MeshView(mesh);
            getChildren().add(coneMesh);
        }

        public void setMaterial(PhongMaterial material) {
            this.material = material;
        }
    }

    private Color getSpeciesColor(String species) {
        switch (species) {
            case "buse": return Color.DARKGREEN;    // Boxwood - dark green
            case "pimu": return Color.FORESTGREEN;  // Mountain Pine - forest green
            case "amov": return Color.LIGHTGREEN;   // Snowy Mespilus - light green
            case "abal": return Color.SEAGREEN;     // Silver Fir - sea green
            case "bepe": return Color.PALEGREEN;    // Silver Birch - pale green
            case "qupe": return Color.GREEN;        // Sessile Oak - green
            case "fasy": return Color.DARKOLIVEGREEN; // European Beech - olive green
            default: return Color.LIMEGREEN;
        }
    }

    private float getTerrainHeightAt(float x, float y) {
        // Convert plant coordinates to grid coordinates
        int gridX = Math.round(x / gridSpacing);
        int gridZ = Math.round(y / gridSpacing);
        
        // Clamp to valid range
        gridX = Math.max(0, Math.min(dimX - 1, gridX));
        gridZ = Math.max(0, Math.min(dimY - 1, gridZ));
        
        return elevations[gridX][gridZ];
    }

    private void addLighting() {
        // Ambient light for general illumination
        AmbientLight ambientLight = new AmbientLight(Color.rgb(180, 180, 180));
        
        // Main directional light - positioned to illuminate the terrain from above
        PointLight mainLight = new PointLight(Color.WHITE);
        mainLight.setTranslateX(-200);
        mainLight.setTranslateY(-600);  // Above the scene
        mainLight.setTranslateZ(-200);
        
        // Fill light to reduce shadows
        PointLight fillLight = new PointLight(Color.rgb(200, 200, 220));
        fillLight.setTranslateX(-200);
        fillLight.setTranslateY(-600);
        fillLight.setTranslateZ(-200);
        
        root3D.getChildren().addAll(ambientLight, mainLight, fillLight);
    }

    /** Add coordinate axes to help understand the 3D space */
    private void addCoordinateAxes() {
        // X-axis (Red)
        Cylinder xAxis = new Cylinder(1, 100);
        xAxis.setMaterial(new PhongMaterial(Color.RED));
        xAxis.setTranslateX(0);
        xAxis.setTranslateY(-500);
        xAxis.setRotationAxis(Rotate.Z_AXIS);
        xAxis.setRotate(90);
        
        // Y-axis (Green)
        Cylinder yAxis = new Cylinder(1, 100);
        yAxis.setMaterial(new PhongMaterial(Color.GREEN));
        yAxis.setTranslateY(-500);
        
        // Z-axis (Blue)
        Cylinder zAxis = new Cylinder(1, 100);
        zAxis.setMaterial(new PhongMaterial(Color.BLUE));
        zAxis.setTranslateZ(0);
        zAxis.setTranslateY(-500);
        zAxis.setRotationAxis(Rotate.X_AXIS);
        zAxis.setRotate(90);
        
        // Add axes to scene (commented out by default - uncomment for debugging)
        root3D.getChildren().addAll(xAxis, yAxis, zAxis);
    }

    private void handleMouse(Scene scene) {
        scene.setOnMousePressed((MouseEvent me) -> {
            mouseOldX = me.getSceneX();
            mouseOldY = me.getSceneY();
        });

        scene.setOnMouseDragged((MouseEvent me) -> {
            mouseX = me.getSceneX();
            mouseY = me.getSceneY();
            double deltaX = (mouseX - mouseOldX);
            double deltaY = (mouseY - mouseOldY);

            if (me.isPrimaryButtonDown()) {
                // Rotation
                rotateY.setAngle(rotateY.getAngle() + deltaX * ROTATION_SPEED);
                rotateX.setAngle(rotateX.getAngle() - deltaY * ROTATION_SPEED);
            } else if (me.isSecondaryButtonDown()) {
                // Panning
                cameraPan.setX(cameraPan.getX() + deltaX * PAN_SPEED);
                // System.out.println("X: " + cameraPan.getX() + deltaX * PAN_SPEED );
                cameraPan.setY(cameraPan.getY() + deltaY * PAN_SPEED);
                // System.out.println("Y: " + cameraPan.getY() + deltaY * PAN_SPEED );
            }

            mouseOldX = mouseX;
            mouseOldY = mouseY;
        });

        scene.setOnScroll((ScrollEvent e) -> {
            double zoomDelta = e.getDeltaY() * ZOOM_SPEED;
            double newZ = cameraPosition.getZ() + zoomDelta;
            // System.out.println("Z: " + newZ);
            // Limit zoom range
            newZ = Math.max(-2000, Math.min(-100, newZ));
            cameraPosition.setZ(newZ);
        });
    }

    public static void main(String[] args) {
        launch(args);
    }
}