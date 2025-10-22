package com.autotasks.jar.spring;

import com.autotasks.jar.annotation.SmartTask;
import com.autotasks.jar.exec.HybridThreadManager;
import com.autotasks.jar.profiling.ProfilingWrapper;
import com.autotasks.jar.util.MetadataManager;
import com.autotasks.jar.analysis.TaskMetadataGenerator;

import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;

import java.util.concurrent.Future;

/**
 * Detects beans that extend SmartTask AND are annotated with @SmartTask.
 * Schedules an initial profiling run if metadata doesn't exist.
 */
public class SmartTaskBeanPostProcessor implements BeanPostProcessor {

    private final HybridThreadManager manager;

    public SmartTaskBeanPostProcessor(HybridThreadManager manager) {
        this.manager = manager;

        // Optional: pre-scan all SmartTask classes at startup
        try {
            TaskMetadataGenerator.runFullScan();
        } catch (Exception e) {
            System.err.println("⚠️ AutoThread: TaskMetadataGenerator scan failed — " + e.getMessage());
        }
    }

   @Override
public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
    if (!(bean instanceof com.autotasks.jar.thread.SmartTask)) return bean;

    Class<?> clazz = bean.getClass();
    com.autotasks.jar.thread.SmartTask task = (com.autotasks.jar.thread.SmartTask) bean;

    // 🔹 CASE 1: Entire class is SmartTask annotated
    if (clazz.isAnnotationPresent(SmartTask.class)) {
        handleClassLevel(task, clazz);
    }

    // 🔹 CASE 2: Specific methods are SmartTask annotated
    for (var method : clazz.getDeclaredMethods()) {
        if (method.isAnnotationPresent(SmartTask.class)) {
            handleMethodLevel(task, clazz, method);
        }
    }

    return bean;
}

private void handleClassLevel(com.autotasks.jar.thread.SmartTask task, Class<?> clazz) {
    String fqcn = clazz.getName();
    if (MetadataManager.get(fqcn, null) == null) {
        System.out.println("🧠 Profiling CLASS " + fqcn);
        new ProfilingWrapper(task).profileOnceAndStore();
    }
    manager.submit(() -> task.runTask(), fqcn);
}

private void handleMethodLevel(com.autotasks.jar.thread.SmartTask task, Class<?> clazz, java.lang.reflect.Method method) {
    String fqcn = clazz.getName();
    String key = fqcn + "." + method.getName();

    if (MetadataManager.get(fqcn, method.getName()) == null) {
        System.out.println("🧠 Profiling METHOD " + key);
        try {
            long start = System.nanoTime();
            method.setAccessible(true);
            method.invoke(task);
            long end = System.nanoTime();
            long duration = end - start;
            String ioType = duration > 2_000_000 ? "IO" : "CPU"; // simple heuristic
            String assigned = ioType.equals("IO") ? "VIRTUAL" : "PLATFORM";
            MetadataManager.put(fqcn, method.getName(), assigned, ioType);
        } catch (Exception e) {
            System.err.println("⚠️ Profiling failed for " + key + ": " + e);
        }
    }

    manager.submit(() -> {
        try { method.invoke(task); } catch (Exception ignored) {}
    }, key);
}

}
