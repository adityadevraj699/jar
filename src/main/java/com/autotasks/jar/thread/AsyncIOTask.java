package com.autotasks.jar.thread;


public class AsyncIOTask extends SmartTask {
    @Override
    public void runTask() {
        logThread("Async Hidden I/O Task");
        try {
            Thread.sleep(500); // simulate I/O
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    
    
    @Override
    public String getTaskName() {
       return "unique.task.name";
    }

}
