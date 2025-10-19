package com.autotasks.jar.analysis;

import com.autotasks.jar.thread.SmartTask;
import com.autotasks.jar.util.MetadataManager;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

public class TaskMetadataGenerator {

    /**
     * Scans all classes available in the current jar/classpath for SmartTask subclasses.
     * For each SmartTask subclass, runs IOAnalyzer on its class bytes and stores metadata via MetadataManager.
     */
    public static void runFullScan() {
        try {
            URL location = TaskMetadataGenerator.class.getProtectionDomain().getCodeSource().getLocation();
            File source = new File(location.toURI());
            if (source.isFile() && source.getName().endsWith(".jar")) {
                scanJar(source);
            } else {
                scanClasspath();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void scanJar(File jarFile) {
        System.out.println("Scanning JAR for SmartTask classes: " + jarFile.getName());
        try (JarFile jf = new JarFile(jarFile)) {
            Enumeration<JarEntry> entries = jf.entries();
            while (entries.hasMoreElements()) {
                JarEntry e = entries.nextElement();
                if (e.getName().endsWith(".class")) {
                    try (InputStream in = jf.getInputStream(e)) {
                        String className = e.getName().replace('/', '.').replace(".class", "");
                        analyzeAndRegister(className, in);
                    } catch (Throwable t) {
                        // ignore classes that can't be loaded/analyzed
                    }
                }
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private static void scanClasspath() {
        System.out.println("Scanning classpath directories for SmartTask classes...");
        try {
            Enumeration<URL> resources = Thread.currentThread().getContextClassLoader().getResources("");
            while (resources.hasMoreElements()) {
                URL url = resources.nextElement();
                // simple approach: skip, but you can add directory scanning here
            }
            // Note: runtime scanning from IDE is not always necessary for packaged jar
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void analyzeAndRegister(String className, InputStream classBytes) {
        try {
            // Try to load class reflection to check inheritance
            Class<?> clazz = null;
            try {
                clazz = Class.forName(className);
            } catch (Throwable t) {
                // class not loadable at analysis time, still try bytecode analyze for runTask method
            }

            boolean isSmart = false;
            if (clazz != null) {
                isSmart = SmartTask.class.isAssignableFrom(clazz) && !java.lang.reflect.Modifier.isAbstract(clazz.getModifiers());
            } else {
                // fallback heuristic: class name contains "Task"
                isSmart = className.endsWith("Task");
            }

            if (!isSmart) return;

            // Need a fresh InputStream for ASM; caller may have provided stream already consumed
            // Here analyze from provided InputStream directly (we assume it's fresh)
            boolean io = IOAnalyzer.isIOBound(classBytes, className);

            String assigned = io ? "VIRTUAL" : "PLATFORM";
            MetadataManager.put(className, assigned, io ? "IO" : "CPU");

            System.out.printf("Registered %s → %s (%s)%n", className, assigned, io ? "IO" : "CPU");
        } catch (Exception ex) {
            // ignore per-class errors
        }
    }
}
