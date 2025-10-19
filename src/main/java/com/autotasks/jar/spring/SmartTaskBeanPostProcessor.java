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
        if (bean instanceof com.autotasks.jar.thread.SmartTask) {
            Class<?> clazz = bean.getClass();

            if (clazz.isAnnotationPresent(SmartTask.class)) {
                com.autotasks.jar.thread.SmartTask task = (com.autotasks.jar.thread.SmartTask) bean;
                String fqcn = task.getClass().getName();

                if (MetadataManager.get(fqcn) == null) {
                    System.out.println("🧠 AutoThread: Profiling " + fqcn + " (first time).");
                    ProfilingWrapper wrapper = new ProfilingWrapper(task);
                    wrapper.profileOnceAndStore();
                }

                Future<?> f = manager.submit(() -> task.runTask(), fqcn);
                System.out.println("🚀 AutoThread: Submitted " + fqcn + " via HybridThreadManager.");
            }
        }
        return bean;
    }
}
