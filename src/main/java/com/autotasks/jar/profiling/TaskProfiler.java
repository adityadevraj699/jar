package com.autotasks.jar.profiling;

import com.autotasks.jar.util.MetadataManager;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;

public class TaskProfiler {
    public static void profileAndStore(Runnable task, String className) {
        ThreadMXBean bean = ManagementFactory.getThreadMXBean();
        boolean supported = bean.isThreadCpuTimeSupported();
        if (supported && !bean.isThreadCpuTimeEnabled()) {
            try { bean.setThreadCpuTimeEnabled(true); } catch (Throwable ignored) {}
        }

        long tid = Thread.currentThread().getId();
        long cpuBefore = supported ? bean.getThreadCpuTime(tid) : 0;
        long start = System.nanoTime();

        task.run();

        long end = System.nanoTime();
        long cpuAfter = supported ? bean.getThreadCpuTime(tid) : 0;

        long wall = end - start;
        long cpu = supported ? Math.max(0, cpuAfter - cpuBefore) : wall;
        double ratio = wall == 0 ? 1.0 : (double) cpu / (double) wall;

        String type;
        if (ratio > 0.7) type = "CPU";
        else if (ratio < 0.5) type = "IO"; // use 0.5 for small/short sleep tasks
        else type = "MIXED";


        String assigned = switch (type) {
            case "CPU" -> "PLATFORM";
            case "IO" -> "VIRTUAL";
            default -> "MIXED";
        };

        MetadataManager.put(className, assigned, type);
    }
}
