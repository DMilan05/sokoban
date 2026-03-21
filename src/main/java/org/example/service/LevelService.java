package org.example.service;

import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;

@Service
public class LevelService {

    public Map<String, String> getAllLevels() {
        Map<String, String> levels = new LinkedHashMap<>();

        try {
            PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
            // Egyetlen nagy fájlt keresünk, amiben benne van az összes pálya
            Resource[] resources = resolver.getResources("classpath:levels/original50.txt");

            if (resources.length > 0 && resources[0].isReadable()) {
                String content = new String(resources[0].getInputStream().readAllBytes(), StandardCharsets.UTF_8);

                // Szétdaraboljuk a szöveget az üres sorok (dupla sortörések) mentén
                String[] rawLevels = content.split("\\r?\\n\\r?\\n");

                int counter = 1;
                for (String rawLevel : rawLevels) {
                    if (!rawLevel.trim().isEmpty()) {
                        levels.put(counter + ". Pálya", rawLevel.trim());
                        counter++;
                    }
                }
            }
        } catch (IOException e) {
            System.out.println("Figyelem: Nem található az original50.txt a resources/levels mappában!");
        }

        // Ha véletlenül nincs meg a fájl, adunk egy alap pályát
        if (levels.isEmpty()) {
            levels.put("Alapértelmezett", "######\n#@ $.#\n######");
        }

        return levels;
    }
}