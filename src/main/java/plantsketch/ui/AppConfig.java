package plantsketch.ui;

import java.nio.file.Path;

public final class AppConfig {
    private AppConfig() {}

    public static Path dataRoot;        // e.g., C:\...\plantsketch\src\main\resources
    public static String environment;   // e.g., "D1-256"
    public static int sampleCount;      // e.g., 2000

    public static Path selectedEnvPath() {
        return (dataRoot == null || environment == null) ? null : dataRoot.resolve(environment);
    }

    public static boolean isReady() {
        return dataRoot != null && environment != null && sampleCount > 0;
    }
}
