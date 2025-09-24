import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import plantsketch.FileManager;
import java.io.File;

public class FileManagerTest {

    private FileManager fileManager;
    private static final String TEST_DATA_256 = "src/test/resources/D1-256";
    private static final String TEST_DATA_1024 = "src/test/resources/D3-1024";

    // Expected values from test files (hardcoded from actual file inspection)
    private static final int EXPECTED_256_DIMX = 256;
    private static final int EXPECTED_256_DIMY = 256;
    private static final float EXPECTED_256_GRID_SPACING = 0.9144f;
    private static final float EXPECTED_256_ELEVATION_0_0 = 446.505f;
    private static final float EXPECTED_256_ELEVATION_1_1 = 447.264f;

    private static final int EXPECTED_1024_DIMX = 1024;
    private static final int EXPECTED_1024_DIMY = 1024;

    @BeforeEach
    void setUp() {
        fileManager = new FileManager();
    }

    @Test
    @DisplayName("Test loading 256x256 elevation file")
    void testLoad256ElevationFile() {
        File elevFile = new File(TEST_DATA_256 + "/D1-256.elv");
        assertTrue(elevFile.exists(), "Test elevation file should exist");

        float[][] elevationGrid = fileManager.loadElv(elevFile);

        assertNotNull(elevationGrid, "Elevation grid should not be null");
        assertEquals(EXPECTED_256_DIMX, fileManager.getDimX(), "X dimension should be 256");
        assertEquals(EXPECTED_256_DIMY, fileManager.getDimY(), "Y dimension should be 256");
        assertEquals(EXPECTED_256_GRID_SPACING, fileManager.getGridSpacing(), 0.001f, "Grid spacing should match");

        // Test specific known values from the file
        assertEquals(EXPECTED_256_ELEVATION_0_0, elevationGrid[0][0], 0.001f, "First elevation value should match");
        assertEquals(EXPECTED_256_ELEVATION_1_1, elevationGrid[1][1], 0.001f, "Second row elevation value should match");
    }

    @Test
    @DisplayName("Test loading 256x256 temperature file")
    void testLoad256TemperatureFile() {
        // Load elevation first to set dimensions
        File elevFile = new File(TEST_DATA_256 + "/D1-256.elv");
        fileManager.loadElv(elevFile);

        File tempFile = new File(TEST_DATA_256 + "/D1-256_temp.txt");
        assertTrue(tempFile.exists(), "Test temperature file should exist");

        float[][] tempGrid = fileManager.loadTxt(tempFile);

        assertNotNull(tempGrid, "Temperature grid should not be null");
        assertEquals(EXPECTED_256_DIMX, tempGrid.length, "Temperature grid X dimension should be 256");
        assertEquals(EXPECTED_256_DIMY, tempGrid[0].length, "Temperature grid Y dimension should be 256");

        // Temperature values should be reasonable (averaged from monthly data)
        assertTrue(tempGrid[0][0] > -50 && tempGrid[0][0] < 50, "Temperature should be in reasonable range");
    }

    @Test
    @DisplayName("Test loading 256x256 moisture file")
    void testLoad256MoistureFile() {
        // Load elevation first to set dimensions
        File elevFile = new File(TEST_DATA_256 + "/D1-256.elv");
        fileManager.loadElv(elevFile);

        File moistureFile = new File(TEST_DATA_256 + "/D1-256_water.txt");
        assertTrue(moistureFile.exists(), "Test moisture file should exist");

        float[][] moistureGrid = fileManager.loadTxt(moistureFile);

        assertNotNull(moistureGrid, "Moisture grid should not be null");
        assertEquals(EXPECTED_256_DIMX, moistureGrid.length, "Moisture grid X dimension should be 256");
        assertEquals(EXPECTED_256_DIMY, moistureGrid[0].length, "Moisture grid Y dimension should be 256");

        // Moisture values should be non-negative
        assertTrue(moistureGrid[0][0] >= 0, "Moisture should be non-negative");
    }

    @Test
    @DisplayName("Test loading 256x256 sunlight file")
    void testLoad256SunlightFile() {
        // Load elevation first to set dimensions
        File elevFile = new File(TEST_DATA_256 + "/D1-256.elv");
        fileManager.loadElv(elevFile);

        File sunFile = new File(TEST_DATA_256 + "/D1-256_sun.txt");
        assertTrue(sunFile.exists(), "Test sunlight file should exist");

        float[][] sunGrid = fileManager.loadTxt(sunFile);

        assertNotNull(sunGrid, "Sunlight grid should not be null");
        assertEquals(EXPECTED_256_DIMX, sunGrid.length, "Sunlight grid X dimension should be 256");
        assertEquals(EXPECTED_256_DIMY, sunGrid[0].length, "Sunlight grid Y dimension should be 256");

        // Sunlight values should be reasonable
        assertTrue(sunGrid[0][0] >= 0, "Sunlight should be non-negative");
    }

    @Test
    @DisplayName("Test loading 256x256 age PNG file")
    void testLoad256AgeFile() {
        // Load elevation first to set dimensions
        File elevFile = new File(TEST_DATA_256 + "/D1-256.elv");
        fileManager.loadElv(elevFile);

        File ageFile = new File(TEST_DATA_256 + "/256-256-Age.png");
        assertTrue(ageFile.exists(), "Test age file should exist");

        float[][] ageGrid = fileManager.loadPng(ageFile);

        assertNotNull(ageGrid, "Age grid should not be null");
        assertEquals(EXPECTED_256_DIMX, ageGrid.length, "Age grid X dimension should be 256");
        assertEquals(EXPECTED_256_DIMY, ageGrid[0].length, "Age grid Y dimension should be 256");

        // Age values should be in range [0, 600] as per PNG normalization
        assertTrue(ageGrid[0][0] >= 0 && ageGrid[0][0] <= 600, "Age should be in range [0, 600]");
    }

    @Test
    @DisplayName("Test complete 256x256 directory loading")
    void testLoad256Directory() {
        fileManager.fileFinder(TEST_DATA_256);

        // Verify all grids are loaded
        assertNotNull(fileManager.getElevationGrid(), "Elevation grid should be loaded");
        assertNotNull(fileManager.getTemperatureGrid(), "Temperature grid should be loaded");
        assertNotNull(fileManager.getMoistureGrid(), "Moisture grid should be loaded");
        assertNotNull(fileManager.getSunlightGrid(), "Sunlight grid should be loaded");
        assertNotNull(fileManager.getAgeGrid(), "Age grid should be loaded");

        // Verify dimensions are consistent
        assertEquals(EXPECTED_256_DIMX, fileManager.getDimX(), "X dimension should be 256");
        assertEquals(EXPECTED_256_DIMY, fileManager.getDimY(), "Y dimension should be 256");
    }

    @Test
    @DisplayName("Test 1024x1024 elevation file loading")
    void testLoad1024ElevationFile() {
        File elevFile = new File(TEST_DATA_1024 + "/D3-1024.elv");
        assertTrue(elevFile.exists(), "Test 1024 elevation file should exist");

        float[][] elevationGrid = fileManager.loadElv(elevFile);

        assertNotNull(elevationGrid, "1024 elevation grid should not be null");
        assertEquals(EXPECTED_1024_DIMX, fileManager.getDimX(), "X dimension should be 1024");
        assertEquals(EXPECTED_1024_DIMY, fileManager.getDimY(), "Y dimension should be 1024");

        // Verify grid size
        assertEquals(EXPECTED_1024_DIMX, elevationGrid.length, "Elevation grid should have 1024 rows");
        assertEquals(EXPECTED_1024_DIMY, elevationGrid[0].length, "Elevation grid should have 1024 columns");
    }

    @Test
    @DisplayName("Test complete 1024x1024 directory loading")
    void testLoad1024Directory() {
        fileManager.fileFinder(TEST_DATA_1024);

        // Verify all available grids are loaded
        assertNotNull(fileManager.getElevationGrid(), "1024 elevation grid should be loaded");
        assertNotNull(fileManager.getTemperatureGrid(), "1024 temperature grid should be loaded");
        assertNotNull(fileManager.getMoistureGrid(), "1024 moisture grid should be loaded");
        assertNotNull(fileManager.getSunlightGrid(), "1024 sunlight grid should be loaded");
        assertNotNull(fileManager.getAgeGrid(), "1024 age grid should be loaded");

        // Verify dimensions are consistent
        assertEquals(EXPECTED_1024_DIMX, fileManager.getDimX(), "X dimension should be 1024");
        assertEquals(EXPECTED_1024_DIMY, fileManager.getDimY(), "Y dimension should be 1024");
    }

    @Test
    @DisplayName("Test error handling - non-existent file")
    void testLoadNonExistentFile() {
        File nonExistentFile = new File("nonexistent.elv");

        float[][] result = fileManager.loadElv(nonExistentFile);

        assertNull(result, "Loading non-existent file should return null");
    }

    @Test
    @DisplayName("Test error handling - invalid directory")
    void testLoadInvalidDirectory() {
        // This should handle the error gracefully without crashing
        assertDoesNotThrow(() -> {
            fileManager.fileFinder("nonexistent/directory");
        }, "Loading invalid directory should not throw exception");
    }

    @Test
    @DisplayName("Test grid value consistency across multiple loads")
    void testGridValueConsistency() {
        // Load the same file twice and ensure values are identical
        File elevFile = new File(TEST_DATA_256 + "/D1-256.elv");

        FileManager fm1 = new FileManager();
        FileManager fm2 = new FileManager();

        float[][] grid1 = fm1.loadElv(elevFile);
        float[][] grid2 = fm2.loadElv(elevFile);

        assertNotNull(grid1, "First grid load should succeed");
        assertNotNull(grid2, "Second grid load should succeed");

        // Compare several values
        assertEquals(grid1[0][0], grid2[0][0], 0.001f, "Grid values should be identical across loads");
        assertEquals(grid1[10][10], grid2[10][10], 0.001f, "Grid values should be identical across loads");
        assertEquals(grid1[255][255], grid2[255][255], 0.001f, "Grid values should be identical across loads");
    }

    @Test
    @DisplayName("Test getter methods return correct grids")
    void testGetterMethods() {
        fileManager.fileFinder(TEST_DATA_256);

        float[][] elevation = fileManager.getElevationGrid();
        float[][] temperature = fileManager.getTemperatureGrid();
        float[][] moisture = fileManager.getMoistureGrid();
        float[][] sunlight = fileManager.getSunlightGrid();
        float[][] age = fileManager.getAgeGrid();

        // All grids should be non-null and properly sized
        assertNotNull(elevation, "Elevation getter should return non-null grid");
        assertNotNull(temperature, "Temperature getter should return non-null grid");
        assertNotNull(moisture, "Moisture getter should return non-null grid");
        assertNotNull(sunlight, "Sunlight getter should return non-null grid");
        assertNotNull(age, "Age getter should return non-null grid");

        // All grids should have the same dimensions
        assertEquals(elevation.length, temperature.length, "All grids should have same X dimension");
        assertEquals(elevation[0].length, temperature[0].length, "All grids should have same Y dimension");
        assertEquals(elevation.length, moisture.length, "All grids should have same X dimension");
        assertEquals(elevation.length, sunlight.length, "All grids should have same X dimension");
        assertEquals(elevation.length, age.length, "All grids should have same X dimension");
    }
}

