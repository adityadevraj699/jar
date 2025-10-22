package com.autotasks.jar.analysis;

import org.objectweb.asm.*;
import java.io.InputStream;
import java.util.Set;

public class IOAnalyzer {

	private static final Set<String> KNOWN_IO_LIBRARIES = Set.of(
            "java/io", "java/nio/file", "java/net", "java/net/http",
            "javax/mail", "javax/persistence", "org/postgresql",
            "org/springframework/data", "org/springframework/mail",
            "org/springframework/web/client", "org/springframework/kafka",
            "org/apache/commons/fileupload", "com/amazonaws", "com/google/firebase", "java/lang/Thread"
    );

    public static boolean isIOBound(InputStream classBytes, String className) {
        try {
            ClassReader reader = new ClassReader(classBytes);
            final boolean[] ioFlag = {false};

            reader.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
                    if (!name.equals("runTask")) return null;
                    return new MethodVisitor(Opcodes.ASM9) {
                        @Override
                        public void visitMethodInsn(int opcode, String owner, String name, String descriptor, boolean isInterface) {
                            for (String prefix : KNOWN_IO_LIBRARIES) {
                                if (owner.startsWith(prefix)) {
                                    ioFlag[0] = true;
                                    break;
                                }
                            }
                        }
                    };
                }
            }, 0);

            return ioFlag[0];
        } catch (Exception e) {
            System.err.println("⚠️ IOAnalyzer failed for " + className + " : " + e);
            return false;
        }
    }
}
