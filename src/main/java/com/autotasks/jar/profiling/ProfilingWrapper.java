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
        task.runTask(); // run directly
    }


    public void profileOnceAndStore() {
        TaskProfiler.profileAndStore(this, task.getTaskName());
    }
}
