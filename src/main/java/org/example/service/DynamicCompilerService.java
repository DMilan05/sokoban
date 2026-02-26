package org.example.service;

import org.example.model.SokobanHeuristic;
import org.springframework.stereotype.Service;

import javax.tools.*;
import java.io.File;
import java.io.FileWriter;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.Arrays;

@Service
public class DynamicCompilerService {

    public SokobanHeuristic compileAndInstantiate(String userCode, String className) throws Exception {

        // 1. A kód összeállítása
        String fullCode = """
                package org.example.dynamic;
                import org.example.model.SokobanState;
                import org.example.model.Position;
                import org.example.model.SokobanHeuristic;
                
                public class %s implements SokobanHeuristic {
                    @Override
                    %s
                }
                """.formatted(className, userCode);

        // 2. Létrehozzuk az ideiglenes mappát és beletesszük a .java fájlt
        File tempDir = new File(System.getProperty("java.io.tmpdir"), "sokoban_compiler_" + System.currentTimeMillis());
        tempDir.mkdirs();
        tempDir.deleteOnExit();

        File sourceFile = new File(tempDir, className + ".java");
        sourceFile.deleteOnExit();

        try (FileWriter writer = new FileWriter(sourceFile)) {
            writer.write(fullCode);
        }

        // 3. Fordítás
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler == null) {
            throw new IllegalStateException("Nem található a JavaCompiler! Biztosan JDK-t használsz, és nem csak JRE-t?");
        }

        StandardJavaFileManager fileManager = compiler.getStandardFileManager(null, null, null);
        Iterable<? extends JavaFileObject> compilationUnits = fileManager.getJavaFileObjectsFromFiles(Arrays.asList(sourceFile));

        // --- JAVÍTÁS ITT ---
        // Átadjuk a -d paramétert, hogy a fordító automatikusan létrehozza az org/example/dynamic mappákat!
        Iterable<String> options = Arrays.asList("-d", tempDir.getAbsolutePath());

        JavaCompiler.CompilationTask task = compiler.getTask(null, fileManager, null, options, null, compilationUnits);
        boolean success = task.call();
        fileManager.close();

        if (!success) {
            throw new RuntimeException("Fordítási hiba a beküldött kódban! Kérlek, ellenőrizd a szintaktikát.");
        }

        // 4. Betöltés a memóriába (most már a mappaszerkezet a helyén van)
        URLClassLoader classLoader = URLClassLoader.newInstance(new URL[]{tempDir.toURI().toURL()});
        Class<?> cls = Class.forName("org.example.dynamic." + className, true, classLoader);

        // 5. Példányosítás
        return (SokobanHeuristic) cls.getDeclaredConstructor().newInstance();
    }
}
