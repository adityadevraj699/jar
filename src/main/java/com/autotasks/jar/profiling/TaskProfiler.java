package com.autotasks.jar.profiling;

import com.autotasks.jar.util.MetadataManager;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class TaskProfiler {

    public static void profileAndStore(Runnable task, String key) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (bean.isThreadCpuTimeSupported() && !bean.isThreadCpuTimeEnabled()) {
            try { bean.setThreadCpuTimeEnabled(true); } catch (Throwable ignored) {}
        }

        long cpuBefore = bean.isThreadCpuTimeSupported() ? bean.getCurrentThreadCpuTime() : 0;
        long startWall = System.nanoTime();

        // Run the task
        try { task.run(); } catch (Exception ignored) {}

        long endWall = System.nanoTime();
        long cpuAfter = bean.isThreadCpuTimeSupported() ? bean.getCurrentThreadCpuTime() : 0;

        long cpuTime = Math.max(0, cpuAfter - cpuBefore);
        long wallTime = endWall - startWall;
        double ratio = wallTime == 0 ? 1.0 : (double) cpuTime / wallTime;

        String type;
        if (ratio >= 0.7) type = "CPU";
        else if (ratio <= 0.3) type = "IO";
        else type = "MIXED";

        String assigned;
        switch (type) {
            case "CPU" -> assigned = "PLATFORM";
            case "IO" -> assigned = "VIRTUAL";
            default -> assigned = "MIXED";
        }

        MetadataManager.put(key, assigned, type);

        System.out.printf("🧠 Profiling %s → %s (%s) | ratio=%.2f%n", key, assigned, type, ratio);
    }
}
