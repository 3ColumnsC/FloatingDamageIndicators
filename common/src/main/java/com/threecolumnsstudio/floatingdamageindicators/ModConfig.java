package com.threecolumnsstudio.floatingdamageindicators;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("FDI-Config");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public boolean showDamage = true;
    public boolean showReceivedDamage = true;

    private static volatile ModConfig INSTANCE = new ModConfig();

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve("floatingdamageindicators.json");
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                ModConfig loaded = GSON.fromJson(reader, ModConfig.class);
                if (loaded != null) {
                    INSTANCE = loaded;
                }
                LOGGER.info("Config loaded from {}", file);
            } catch (Exception e) {
                LOGGER.error("Failed to load config from {}, using defaults", file, e);
            }
        } else {
            save(configDir);
        }
    }

    public static void save(Path configDir) {
        Path file = configDir.resolve("floatingdamageindicators.json");
        try {
            Files.createDirectories(configDir);
            Files.writeString(file, GSON.toJson(INSTANCE));
            LOGGER.info("Config saved to {}", file);
        } catch (IOException e) {
            LOGGER.error("Failed to save config to {}", file, e);
        }
    }
}
