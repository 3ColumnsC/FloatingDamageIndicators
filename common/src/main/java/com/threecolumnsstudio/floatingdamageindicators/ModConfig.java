package com.threecolumnsstudio.floatingdamageindicators;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("FDI-Config");

    public boolean showDamage = true;
    public boolean showReceivedDamage = true;

    public Map<String, FormatEntry> formats = defaultFormats();

    private static volatile ModConfig INSTANCE = new ModConfig();

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve("floatingdamageindicators.json");
        if (Files.exists(file)) {
            try (Reader reader = Files.newBufferedReader(file)) {
                JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
                ModConfig loaded = new ModConfig();

                if (root.has("showDamage"))
                    loaded.showDamage = root.get("showDamage").getAsBoolean();
                if (root.has("showReceivedDamage"))
                    loaded.showReceivedDamage = root.get("showReceivedDamage").getAsBoolean();

                if (root.has("formats")) {
                    JsonObject fmts = root.getAsJsonObject("formats");
                    loaded.formats = new LinkedHashMap<>();
                    for (var entry : fmts.entrySet()) {
                        JsonObject f = entry.getValue().getAsJsonObject();
                        loaded.formats.put(entry.getKey(), new FormatEntry(
                            jsonBool(f, "enabled", true),
                            jsonStr(f, "prefix", ""),
                            jsonStr(f, "color", "FFFFFF"),
                            jsonBool(f, "showDamage", true)
                        ));
                    }
                }

                INSTANCE = loaded;
                LOGGER.info("Config loaded from {}", file);
            } catch (Exception e) {
                LOGGER.error("Failed to load config, regenerating with defaults", e);
                INSTANCE = new ModConfig();
                save(configDir);
            }
        } else {
            save(configDir);
        }
    }

    public static void save(Path configDir) {
        Path file = configDir.resolve("floatingdamageindicators.json");
        try {
            Files.createDirectories(configDir);
            Files.writeString(file, toJson());
            LOGGER.info("Config saved to {}", file);
        } catch (IOException e) {
            LOGGER.error("Failed to save config to {}", file, e);
        }
    }

    private static String toJson() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"showDamage\": ").append(INSTANCE.showDamage).append(",\n");
        sb.append("  \"showReceivedDamage\": ").append(INSTANCE.showReceivedDamage).append(",\n");
        sb.append("  \"formats\": {\n");
        boolean first = true;
        for (var entry : INSTANCE.formats.entrySet()) {
            if (!first) sb.append(",\n");
            first = false;
            FormatEntry f = entry.getValue();
            sb.append("    \"").append(entry.getKey()).append("\": {\n");
            sb.append("      \"enabled\": ").append(f.enabled).append(",\n");
            sb.append("      \"prefix\": \"").append(jsonEscape(f.prefix != null ? f.prefix : "")).append("\",\n");
            sb.append("      \"color\": \"").append(jsonEscape(f.color != null ? f.color : "FFFFFF")).append("\",\n");
            sb.append("      \"showDamage\": ").append(f.showDamage).append("\n");
            sb.append("    }");
        }
        sb.append("\n  }\n");
        sb.append("}\n");
        return sb.toString();
    }

    private static String jsonEscape(String s) {
        return s.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static Map<String, FormatEntry> defaultFormats() {
        Map<String, FormatEntry> map = new LinkedHashMap<>();
        map.put("NORMAL",       new FormatEntry(true, "",           "FF3333", true));
        map.put("CRITICAL",     new FormatEntry(true, "\u2726",     "FFD700", true));
        map.put("PROJECTILE",   new FormatEntry(true, "\u27B5",     "00FFFF", true));
        map.put("FIRE",         new FormatEntry(true, "\u2668",     "FF6600", true));
        map.put("POISON",       new FormatEntry(true, "\u2697",     "4A9E2F", true));
        map.put("WITHER",       new FormatEntry(true, "\u2620",     "3C3C3C", true));
        map.put("RECEIVING",    new FormatEntry(true, "(You) ",    "AAAAAA", true));
        map.put("INSTANT_KILL", new FormatEntry(true, "\u26A1 INSTANT KILL", "FFD700", false));
        return map;
    }

    private static boolean jsonBool(JsonObject obj, String key, boolean def) {
        return obj.has(key) ? obj.get(key).getAsBoolean() : def;
    }

    private static String jsonStr(JsonObject obj, String key, String def) {
        return obj.has(key) ? obj.get(key).getAsString() : def;
    }

    public FormatEntry getFormat(DamageType type) {
        if (formats == null) return null;
        return formats.get(type.name());
    }

    public static class FormatEntry {
        public boolean enabled;
        public String prefix;
        public String color;
        public boolean showDamage;

        public FormatEntry() {}

        public FormatEntry(boolean enabled, String prefix, String color, boolean showDamage) {
            this.enabled = enabled;
            this.prefix = prefix;
            this.color = color;
            this.showDamage = showDamage;
        }
    }
}
