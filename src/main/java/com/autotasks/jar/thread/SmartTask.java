package com.autotasks.jar.thread;

public abstract class SmartTask implements Runnable {
    public abstract void runTask();

    @Override
    public final void run() { runTask(); }

    public String getTaskName() { return this.getClass().getName(); }

    public void logThread(String taskName) {
        System.out.println("🧵 Task [" + taskName + "] running on " + Thread.currentThread());
    }
}
