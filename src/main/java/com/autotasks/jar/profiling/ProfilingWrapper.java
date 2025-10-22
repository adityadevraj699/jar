package com.autotasks.jar.profiling;

import com.autotasks.jar.thread.SmartTask;

import java.util.concurrent.Executors;

public class ProfilingWrapper implements Runnable {
    private final SmartTask task;

    public ProfilingWrapper(SmartTask task) {
        this.task = task;
    }

    @Override
    public void run() {
        // ✅ Use platform executor for profiling to measure CPU accurately
        try (var exec = Executors.newFixedThreadPool(
                Math.max(2, Runtime.getRuntime().availableProcessors())
        )) {
            exec.submit(() -> task.runTask()).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ✅ Run profiling once and store metadata
    public void profileOnceAndStore() {
        TaskProfiler.profileAndStore(this, task.getTaskName());
    }
}
