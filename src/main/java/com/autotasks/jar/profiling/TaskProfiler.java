package com.autotasks.jar.profiling;

import com.autotasks.jar.util.MetadataManager;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class TaskProfiler {

    public static void profileAndStore(Runnable task, String className) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        if (bean.isThreadCpuTimeSupported() && !bean.isThreadCpuTimeEnabled()) {
            try { bean.setThreadCpuTimeEnabled(true); } catch (Throwable ignored) {}
        }

        long tid = Thread.currentThread().getId();
        long cpuBefore = bean.isThreadCpuTimeSupported() ? bean.getThreadCpuTime(tid) : 0;
        long start = System.nanoTime();

        task.run();

        long end = System.nanoTime();
        long cpuAfter = bean.isThreadCpuTimeSupported() ? bean.getThreadCpuTime(tid) : 0;

        long wall = end - start;
        long cpu = bean.isThreadCpuTimeSupported() ? Math.max(0, cpuAfter - cpuBefore) : wall;

        double ratio = wall == 0 ? 1.0 : (double) cpu / (double) wall;

        String type;
        if (ratio >= 0.7) type = "CPU";
        else if (ratio <= 0.3) type = "IO";
        else type = "MIXED";

        String assigned = switch (type) {
            case "CPU" -> "PLATFORM";
            case "IO" -> "VIRTUAL";
            default -> "MIXED";
        };

        MetadataManager.put(className, assigned, type);

        System.out.printf("🧠 Profiling %s → %s (%s) | ratio=%.2f%n", className, assigned, type, ratio);
    }
}
