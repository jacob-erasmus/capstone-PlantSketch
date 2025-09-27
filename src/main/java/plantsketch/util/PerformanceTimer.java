package plantsketch.util;

import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

public class PerformanceTimer {
    private static final boolean PROFILING_ENABLED = Boolean.parseBoolean(
        System.getProperty("plantsketch.profiling", "true"));

    private static final Map<String, Long> startTimes = new ConcurrentHashMap<>();
    private static final Map<String, TimerStats> stats = new ConcurrentHashMap<>();

    private static class TimerStats {
        long totalTime = 0;
        long callCount = 0;
        long minTime = Long.MAX_VALUE;
        long maxTime = 0;

        void addTime(long time) {
            totalTime += time;
            callCount++;
            minTime = Math.min(minTime, time);
            maxTime = Math.max(maxTime, time);
        }

        double getAverageMs() {
            return callCount > 0 ? (totalTime / 1_000_000.0) / callCount : 0;
        }

        double getTotalMs() {
            return totalTime / 1_000_000.0;
        }

        double getMinMs() {
            return minTime == Long.MAX_VALUE ? 0 : minTime / 1_000_000.0;
        }

        double getMaxMs() {
            return maxTime / 1_000_000.0;
        }
    }

    public static void start(String name) {
        if (!PROFILING_ENABLED) return;
        startTimes.put(Thread.currentThread().getId() + ":" + name, System.nanoTime());
    }

    public static void end(String name) {
        if (!PROFILING_ENABLED) return;

        long endTime = System.nanoTime();
        String key = Thread.currentThread().getId() + ":" + name;
        Long startTime = startTimes.remove(key);

        if (startTime != null) {
            long duration = endTime - startTime;
            stats.computeIfAbsent(name, k -> new TimerStats()).addTime(duration);
        }
    }

    public static void printStats() {
        if (!PROFILING_ENABLED) {
            System.out.println("Profiling disabled. Enable with -Dplantsketch.profiling=true");
            return;
        }

        System.out.println("\n=== Performance Profile ===");
        stats.entrySet().stream()
            .sorted(Map.Entry.<String, TimerStats>comparingByValue(
                (a, b) -> Double.compare(b.getTotalMs(), a.getTotalMs())))
            .forEach(entry -> {
                String name = entry.getKey();
                TimerStats stat = entry.getValue();
                System.out.printf("%-30s: %8.2f ms total (%6d calls, avg: %6.2f ms, min: %6.2f ms, max: %6.2f ms)%n",
                    name, stat.getTotalMs(), stat.callCount, stat.getAverageMs(), stat.getMinMs(), stat.getMaxMs());
            });
        System.out.println("========================\n");
    }

    public static void reset() {
        if (!PROFILING_ENABLED) return;
        startTimes.clear();
        stats.clear();
    }
}