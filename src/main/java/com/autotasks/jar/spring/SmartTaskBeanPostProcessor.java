package com.autotasks.jar.spring;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.springframework.beans.factory.config.BeanPostProcessor;

import com.autotasks.jar.analysis.TaskMetadataGenerator;
import com.autotasks.jar.annotation.SmartTask;
import com.autotasks.jar.exec.HybridThreadManager;
import com.autotasks.jar.profiling.TaskProfiler;
import com.autotasks.jar.util.MetadataManager;

public class SmartTaskBeanPostProcessor implements BeanPostProcessor {

    private final HybridThreadManager manager;

    public SmartTaskBeanPostProcessor(HybridThreadManager manager) {
        this.manager = manager;
        // Pre-scan all JAR classes for SmartTask metadata
        TaskMetadataGenerator.runFullScan();
    }

    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) {
        // ✅ Fully-qualified check to avoid IDE/compiler errors
        if (!(bean instanceof com.autotasks.jar.thread.SmartTask)) return bean;

        // Cast safely
        com.autotasks.jar.thread.SmartTask task = (com.autotasks.jar.thread.SmartTask) bean;
        Class<?> clazz = task.getClass();

        // ===== CLASS-level @SmartTask annotation =====
        if (clazz.isAnnotationPresent(SmartTask.class)) {
            handleClassLevel(task, clazz);
        }

        // ===== METHOD-level @SmartTask annotation =====
        for (Method method : clazz.getDeclaredMethods()) {
            if (method.isAnnotationPresent(SmartTask.class)) {
                handleMethodLevel(task, clazz, method);
            }
        }

        return bean;
    }

    private void handleClassLevel(com.autotasks.jar.thread.SmartTask task, Class<?> clazz) {
        String key = clazz.getName();
        if (MetadataManager.get(key) == null) {
            // Profile task once to detect CPU/IO/MIXED
            TaskProfiler.profileAndStore(task, key);
        }
        // Submit task to proper executor based on metadata
        manager.submit(task, key);
    }

    private void handleMethodLevel(com.autotasks.jar.thread.SmartTask task, Class<?> clazz, Method method) {
        String key = clazz.getName() + "." + method.getName();

        if (MetadataManager.get(clazz.getName(), method.getName()) == null) {
            try {
                method.setAccessible(true);
                // Profile once to detect CPU/IO/MIXED
                TaskProfiler.profileAndStore(() -> {
                    try {
						method.invoke(task);
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
                }, key);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Submit method to proper executor based on metadata
        manager.submit(() -> {
            try { method.invoke(task); } catch (Exception ignored) {}
        }, key);
    }
}
