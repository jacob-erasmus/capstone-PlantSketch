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

    private static final int EXPECTED_256_DIMX = 256;
    private static final int EXPECTED_256_DIMY = 256;
    private static final float EXPECTED_256_GRID_SPACING = 0.9144f;
    private static final float EXPECTED_256_ELEVATION_0_0 = 446.505f;

    private static final int EXPECTED_1024_DIMX = 1024;
    private static final int EXPECTED_1024_DIMY = 1024;

    @BeforeEach
    void setUp() {
        fileManager = new FileManager();
    }

    @Test
    @DisplayName("Test elevation file loading with dimension validation")
    void testElevationFileLoading() {
        File elevFile = new File(TEST_DATA_256 + "/D1-256.elv");
        assertTrue(elevFile.exists(), "Test elevation file should exist");

        float[][] elevationGrid = fileManager.loadElv(elevFile);

        assertNotNull(elevationGrid, "Elevation grid should not be null");
        assertEquals(EXPECTED_256_DIMX, fileManager.getDimX(), "X dimension should be 256");
        assertEquals(EXPECTED_256_DIMY, fileManager.getDimY(), "Y dimension should be 256");
        assertEquals(EXPECTED_256_GRID_SPACING, fileManager.getGridSpacing(), 0.001f, "Grid spacing should match");
        assertEquals(EXPECTED_256_ELEVATION_0_0, elevationGrid[0][0], 0.001f, "First elevation value should match");
    }

    @Test
    @DisplayName("Test text file loading (temperature/moisture/sunlight)")
    void testTextFileLoading() {
        File elevFile = new File(TEST_DATA_256 + "/D1-256.elv");
        fileManager.loadElv(elevFile);

        File tempFile = new File(TEST_DATA_256 + "/D1-256_temp.txt");
        assertTrue(tempFile.exists(), "Test temperature file should exist");

        float[][] tempGrid = fileManager.loadTxt(tempFile);

        assertNotNull(tempGrid, "Temperature grid should not be null");
        assertEquals(EXPECTED_256_DIMX, tempGrid.length, "Grid should have correct dimensions");
        assertTrue(tempGrid[0][0] > -50 && tempGrid[0][0] < 50, "Temperature should be in reasonable range");
    }

    @Test
    @DisplayName("Test PNG file loading")
    void testPngFileLoading() {
        File elevFile = new File(TEST_DATA_256 + "/D1-256.elv");
        fileManager.loadElv(elevFile);

        File ageFile = new File(TEST_DATA_256 + "/256-256-Age.png");
        assertTrue(ageFile.exists(), "Test age file should exist");

        float[][] ageGrid = fileManager.loadPng(ageFile);

        assertNotNull(ageGrid, "Age grid should not be null");
        assertEquals(EXPECTED_256_DIMX, ageGrid.length, "Age grid should have correct dimensions");
        assertTrue(ageGrid[0][0] >= 0 && ageGrid[0][0] <= 600, "Age should be in range [0, 600]");
    }

    @Test
    @DisplayName("Test complete directory loading")
    void testDirectoryLoading() {
        fileManager.fileFinder(TEST_DATA_256);

        assertNotNull(fileManager.getElevationGrid(), "Elevation grid should be loaded");
        assertNotNull(fileManager.getTemperatureGrid(), "Temperature grid should be loaded");
        assertNotNull(fileManager.getMoistureGrid(), "Moisture grid should be loaded");
        assertNotNull(fileManager.getSunlightGrid(), "Sunlight grid should be loaded");
        assertNotNull(fileManager.getAgeGrid(), "Age grid should be loaded");

        assertEquals(EXPECTED_256_DIMX, fileManager.getDimX(), "X dimension should be 256");
        assertEquals(EXPECTED_256_DIMY, fileManager.getDimY(), "Y dimension should be 256");
    }

    @Test
    @DisplayName("Test large dataset loading")
    void testLargeDatasetLoading() {
        File elevFile = new File(TEST_DATA_1024 + "/D3-1024.elv");
        assertTrue(elevFile.exists(), "Test 1024 elevation file should exist");

        float[][] elevationGrid = fileManager.loadElv(elevFile);

        assertNotNull(elevationGrid, "1024 elevation grid should not be null");
        assertEquals(EXPECTED_1024_DIMX, fileManager.getDimX(), "X dimension should be 1024");
        assertEquals(EXPECTED_1024_DIMY, fileManager.getDimY(), "Y dimension should be 1024");
        assertEquals(EXPECTED_1024_DIMX, elevationGrid.length, "Grid should have correct size");
    }

    @Test
    @DisplayName("Test error handling")
    void testErrorHandling() {
        File nonExistentFile = new File("nonexistent.elv");
        float[][] result = fileManager.loadElv(nonExistentFile);
        assertNull(result, "Loading non-existent file should return null");

        assertDoesNotThrow(() -> {
            fileManager.fileFinder("nonexistent/directory");
        }, "Loading invalid directory should not throw exception");
    }
}

