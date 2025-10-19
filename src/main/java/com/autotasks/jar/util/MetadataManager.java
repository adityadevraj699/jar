package com.autotasks.jar.util;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.File;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MetadataManager {
    private static final File META_FILE = new File(System.getProperty("user.dir"), "autothread-task-metadata.json");
    private static final ObjectMapper MAPPER = new ObjectMapper();
    private static final Map<String, TaskMeta> META = new ConcurrentHashMap<>();

    static {
        try {
            if (META_FILE.exists()) {
                Map<String, TaskMeta> loaded = MAPPER.readValue(META_FILE, MAPPER.getTypeFactory().constructMapType(Map.class, String.class, TaskMeta.class));
                if (loaded != null) META.putAll(loaded);
            } else {
                META_FILE.getParentFile().mkdirs();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static TaskMeta get(String className) {
        return META.get(className);
    }

    public static void put(String className, String assignedThread, String ioType) {
        TaskMeta t = new TaskMeta(className, assignedThread, ioType);
        META.put(className, t);
        persist();
    }

    private static void persist() {
        try {
            MAPPER.writerWithDefaultPrettyPrinter().writeValue(META_FILE, META);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static class TaskMeta {
        public String className;
        public String assignedThread;
        public String ioType;

        public TaskMeta() {}
        public TaskMeta(String className, String assignedThread, String ioType) {
            this.className = className; this.assignedThread = assignedThread; this.ioType = ioType;
        }
    }
}
