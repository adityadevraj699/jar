package com.autotasks.jar.spring;

import com.autotasks.jar.exec.HybridThreadManager;
import com.autotasks.jar.profiling.ProfilingWrapper;
import com.autotasks.jar.thread.SmartTask;

import java.util.concurrent.Future;

public class AutoThreadService {

    private final HybridThreadManager manager;

    public AutoThreadService(HybridThreadManager manager) {
        this.manager = manager;
    }

    public Future<?> submit(SmartTask task, boolean isProfiling) {
        if (isProfiling) {
            return manager.submit(new ProfilingWrapper(task), task.getTaskName());
        } else {
            return manager.submit(task, task.getTaskName());
        }
    }
}
