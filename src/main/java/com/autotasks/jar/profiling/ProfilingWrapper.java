package com.autotasks.jar.profiling;

import com.autotasks.jar.thread.SmartTask;

public class ProfilingWrapper implements Runnable {
    private final SmartTask task;

    public ProfilingWrapper(SmartTask task) { this.task = task; }

    @Override
    public void run() {
        task.runTask(); // actual run
    }

    public void profileOnceAndStore() {
        TaskProfiler.profileAndStore(this, task.getTaskName());
    }

}
