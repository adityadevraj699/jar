package com.autotasks.jar.exec;

import com.autotasks.jar.util.MetadataManager;

import java.util.concurrent.*;

public class HybridThreadManager {

    private final ExecutorService virtualExecutor;
    private final ExecutorService platformExecutor;
    private final ExecutorService hybridExecutor;

    public HybridThreadManager() {
        this.virtualExecutor = Executors.newVirtualThreadPerTaskExecutor();
        this.platformExecutor = Executors.newFixedThreadPool(Math.max(2, Runtime.getRuntime().availableProcessors()));
        this.hybridExecutor = Executors.newFixedThreadPool(Math.max(4, Runtime.getRuntime().availableProcessors() * 2));

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            shutdown(virtualExecutor);
            shutdown(platformExecutor);
            shutdown(hybridExecutor);
        }));
    }

    private void shutdown(ExecutorService e) {
        e.shutdown();
        try { e.awaitTermination(2, TimeUnit.SECONDS); } catch (InterruptedException ignored) {}
    }

    // ✅ Submit method with metadata check
    public Future<?> submit(Runnable task, String className) {
        MetadataManager.TaskMeta meta = MetadataManager.get(className);
        if (meta == null) {
            // fallback: profiling will update later
            return platformExecutor.submit(task);
        }

        return switch (meta.assignedThread) {
            case "VIRTUAL" -> virtualExecutor.submit(task);
            case "MIXED" -> hybridExecutor.submit(task);
            case "PLATFORM" -> platformExecutor.submit(task);
            default -> platformExecutor.submit(task);
        };
    }
}
