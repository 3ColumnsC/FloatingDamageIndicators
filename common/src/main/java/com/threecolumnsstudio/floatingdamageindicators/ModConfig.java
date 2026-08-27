package com.threecolumnsstudio.floatingdamageindicators;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("FDI-Config");

    public boolean showDamage = true;
    public boolean showReceivedDamage = true;

    public Map<DamageType, FormatEntry> formats = defaultFormats();

    private static volatile ModConfig INSTANCE = new ModConfig();

    public static ModConfig get() {
        return INSTANCE;
    }

    public static void load(Path configDir) {
        Path file = configDir.resolve("floatingdamageindicators.json");
        if (!Files.exists(file)) {
            save(configDir);
            return;
        }
        try (Reader reader = Files.newBufferedReader(file)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            ModConfig loaded = new ModConfig();
            loaded.showDamage = jsonBool(root, "showDamage", true);
            loaded.showReceivedDamage = jsonBool(root, "showReceivedDamage", true);
            if (root.has("formats") && root.get("formats").isJsonObject()) {
                loadFormats(root.getAsJsonObject("formats"), loaded);
            }
            INSTANCE = loaded;
            LOGGER.info("Config loaded from {}", file);
        } catch (Exception e) {
            LOGGER.error("Failed to load config {}, regenerating with defaults", file, e);
            INSTANCE = new ModConfig();
            save(configDir);
        }
    }

    private static void loadFormats(JsonObject formats, ModConfig loaded) {
        Map<DamageType, FormatEntry> parsed = new EnumMap<>(DamageType.class);
        for (var entry : formats.entrySet()) {
            DamageType type = parseDamageType(entry.getKey());
            if (type == null) continue;
            try {
                JsonObject f = entry.getValue().getAsJsonObject();
                parsed.put(type, new FormatEntry(
                    jsonBool(f, "enabled", true),
                    jsonStr(f, "prefix", ""),
                    jsonStr(f, "color", "FFFFFF"),
                    jsonBool(f, "showDamage", true)
                ));
            } catch (Exception ex) {
                LOGGER.warn("Skipping invalid format entry '{}'", entry.getKey());
            }
        }
        loaded.formats = parsed;
    }

    private static DamageType parseDamageType(String key) {
        try {
            return DamageType.valueOf(key);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Ignoring unknown damage type '{}' in config", key);
            return null;
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

    private static Map<DamageType, FormatEntry> defaultFormats() {
        Map<DamageType, FormatEntry> map = new EnumMap<>(DamageType.class);
        for (DamageType type : DamageType.values()) {
            String color = String.format(Locale.ROOT, "%06X", type.defaultColor());
            map.put(type, new FormatEntry(true, type.defaultPrefix(), color, type.defaultShowDamage()));
        }
        return map;
    }

    private static boolean jsonBool(JsonObject obj, String key, boolean def) {
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull() || !el.isJsonPrimitive()) return def;
        JsonPrimitive p = el.getAsJsonPrimitive();
        if (p.isBoolean()) return p.getAsBoolean();
        if (p.isNumber()) return p.getAsInt() != 0;
        return Boolean.parseBoolean(p.getAsString());
    }

    private static String jsonStr(JsonObject obj, String key, String def) {
        JsonElement el = obj.get(key);
        if (el != null && el.isJsonPrimitive()) return el.getAsString();
        return def;
    }

    public FormatEntry getFormat(DamageType type) {
        return formats.get(type);
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