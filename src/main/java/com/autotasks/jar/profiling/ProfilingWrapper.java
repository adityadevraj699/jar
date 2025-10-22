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
        // Run actual task in a virtual thread during profiling for accurate I/O detection
        try (var exec = Executors.newVirtualThreadPerTaskExecutor()) {
            exec.submit(() -> task.runTask()).get();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void profileOnceAndStore() {
        TaskProfiler.profileAndStore(this, task.getTaskName());
    }
}
