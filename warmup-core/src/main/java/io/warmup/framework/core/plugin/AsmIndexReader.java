package io.warmup.framework.core.plugin;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassVisitor;
import org.objectweb.asm.Opcodes;

public final class AsmIndexReader {

    public static List<String> read(String resource) throws PluginException {
        InputStream in = Thread.currentThread().getContextClassLoader()
                .getResourceAsStream(resource);
        if (in == null) {
            return Collections.emptyList();
        }
        List<String> result = new ArrayList<>();

        try {
            ClassReader cr = new ClassReader(in);
            cr.accept(new ClassVisitor(Opcodes.ASM9) {
                @Override
                public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
                    if (interfaces != null) {
                        String clazz = name.replace("/", ".");
                        for (String itf : interfaces) {
                            if ("io/warmup/plugin/Plugin".equals(itf)) {
                                result.add(clazz);
                                break;
                            }
                        }
                    }
                }

            }, ClassReader.SKIP_CODE | ClassReader.SKIP_DEBUG);
        } catch (IOException e) {
            throw new PluginException("Error leyendo indice", e);
        }
        return result;
    }

}
