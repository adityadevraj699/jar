package com.autotasks.jar.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles loading, storing, and retrieving SmartTask metadata.
 * Supports both class-level and method-level keys.
 */
public class MetadataManager {

    private static final Map<String, TaskMeta> META = new ConcurrentHashMap<>();
    private static final File STORE = new File("autothread-task-metadata.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();

    static {
        load();
    }

    // ✅ Helper: build key for method-level or class-level metadata
    public static String buildKey(String className, String methodName) {
        return (methodName == null || methodName.isEmpty())
                ? className
                : className + "." + methodName;
    }

    // ✅ Load from JSON file at startup
    private static void load() {
        try {
            if (STORE.exists()) {
                Map<String, TaskMeta> data = MAPPER.readValue(
                        STORE,
                        MAPPER.getTypeFactory().constructMapType(Map.class, String.class, TaskMeta.class)
                );
                META.putAll(data);
                System.out.println("✅ Loaded metadata: " + META.size() + " entries");
            }
        } catch (Exception e) {
            System.err.println("⚠️ Failed to load metadata file: " + e.getMessage());
        }
    }

    // ✅ Save to JSON file
    private static void persist() {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(STORE, META);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to save metadata: " + e.getMessage());
        }
    }

    // ✅ Get (class-level or method-level)
    public static TaskMeta get(String className, String methodName) {
        return META.get(buildKey(className, methodName));
    }

    // ✅ Old compatibility method for class-level only
    public static TaskMeta get(String className) {
        return get(className, null);
    }

    // ✅ Put (class-level or method-level)
    public static void put(String className, String methodName, String assignedThread, String ioType) {
        TaskMeta t = new TaskMeta(className, assignedThread, ioType);
        META.put(buildKey(className, methodName), t);
        persist();
    }

    // ✅ Old compatibility method for class-level only
    public static void put(String className, String assignedThread, String ioType) {
        put(className, null, assignedThread, ioType);
    }

    // ✅ Task metadata record
    public static class TaskMeta {
        public String className;
        public String assignedThread;
        public String ioType;

        public TaskMeta() {} // for Jackson

        public TaskMeta(String className, String assignedThread, String ioType) {
            this.className = className;
            this.assignedThread = assignedThread;
            this.ioType = ioType;
        }

        @Override
        public String toString() {
            return String.format("[%s] → %s (%s)", className, assignedThread, ioType);
        }
    }
}
