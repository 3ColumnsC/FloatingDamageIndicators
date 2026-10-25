package com.threecolumnsstudio.floatingdamageindicators;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.concurrent.TimeUnit;

public class ModConfig {
    private static final Logger LOGGER = LoggerFactory.getLogger("FDI-Config");

    public static final String FILE_NAME = "floatingdamageindicators.json";
    public static final int MAX_PREFIX_CODE_POINTS = 32;
    private static final long MAX_CONFIG_BYTES = 1024L * 1024L;
    private static final long RELOAD_CHECK_INTERVAL_NANOS = TimeUnit.SECONDS.toNanos(1);
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public boolean showDamage = true;
    public boolean showReceivedDamage = true;

    public Map<DamageType, FormatEntry> formats = defaultFormats();

    private static volatile ModConfig INSTANCE = new ModConfig();
    private static Path filePath;
    private static long lastModified = -1L;
    private static long lastReloadCheckNanos;

    public static ModConfig get() {
        return INSTANCE;
    }

    public static synchronized void load(Path configDir) {
        filePath = configDir.resolve(FILE_NAME);
        if (!Files.exists(filePath)) {
            INSTANCE = new ModConfig();
            save(configDir);
            return;
        }
        ModConfig loaded = read(filePath);
        if (loaded == null) {
            LOGGER.error("Failed to load config, regenerating with defaults");
            INSTANCE = new ModConfig();
            save(configDir);
            return;
        }
        INSTANCE = loaded;
        lastModified = modifiedTime(filePath);
        lastReloadCheckNanos = System.nanoTime();
        LOGGER.info("Config loaded from {}", filePath.getFileName());
    }

    public static synchronized void reloadIfChanged() {
        Path file = filePath;
        if (file == null) {
            return;
        }
        long now = System.nanoTime();
        if (now - lastReloadCheckNanos < RELOAD_CHECK_INTERVAL_NANOS) {
            return;
        }
        lastReloadCheckNanos = now;
        long modified = modifiedTime(file);
        if (modified < 0 || modified == lastModified) {
            return;
        }
        ModConfig loaded = read(file);
        if (loaded == null) {
            return;
        }
        INSTANCE = loaded;
        lastModified = modified;
        LOGGER.info("Config reloaded from {}", file.getFileName());
    }

    private static ModConfig read(Path file) {
        try {
            if (Files.size(file) > MAX_CONFIG_BYTES) {
                LOGGER.error("Config file is too large, ignoring it");
                return null;
            }
            try (Reader reader = Files.newBufferedReader(file)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                if (parsed == null || !parsed.isJsonObject()) {
                    LOGGER.error("Config root is not a JSON object");
                    return null;
                }
                JsonObject root = parsed.getAsJsonObject();
                ModConfig loaded = new ModConfig();
                loaded.showDamage = jsonBool(root, "showDamage", true);
                loaded.showReceivedDamage = jsonBool(root, "showReceivedDamage", true);
                if (root.has("formats") && root.get("formats").isJsonObject()) {
                    loaded.formats = readFormats(root.getAsJsonObject("formats"));
                }
                return loaded;
            }
        } catch (Exception e) {
            LOGGER.error("Failed to read config ({})", e.getClass().getSimpleName());
            return null;
        }
    }

    private static Map<DamageType, FormatEntry> readFormats(JsonObject formats) {
        Map<DamageType, FormatEntry> parsed = new EnumMap<>(DamageType.class);
        for (var entry : formats.entrySet()) {
            DamageType type = parseDamageType(entry.getKey());
            if (type == null) {
                continue;
            }
            if (!entry.getValue().isJsonObject()) {
                LOGGER.warn("Skipping non-object format entry");
                continue;
            }
            try {
                JsonObject format = entry.getValue().getAsJsonObject();
                parsed.put(type, new FormatEntry(
                    jsonBool(format, "enabled", true),
                    sanitizePrefix(type, jsonString(format, "prefix", type.defaultPrefix())),
                    sanitizeColor(type, jsonString(format, "color", formatColor(type.defaultColor()))),
                    jsonBool(format, "showDamage", type.defaultShowDamage())
                ));
            } catch (Exception ex) {
                LOGGER.warn("Skipping invalid format entry ({})", ex.getClass().getSimpleName());
            }
        }
        return parsed;
    }

    private static DamageType parseDamageType(String key) {
        try {
            return DamageType.valueOf(key);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Ignoring unknown damage type in config");
            return null;
        }
    }

    public static synchronized void save(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        try {
            Files.createDirectories(configDir);
            Files.writeString(file, toJson());
            filePath = file;
            lastModified = modifiedTime(file);
        } catch (IOException e) {
            LOGGER.error("Failed to save config ({})", e.getClass().getSimpleName());
        }
    }

    public static synchronized void updateToggles(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        try {
            Files.createDirectories(configDir);
            JsonObject root = readRoot(file);
            if (root == null) {
                root = new JsonObject();
                writeFormats(root);
            }
            writeToggles(root);
            Files.writeString(file, GSON.toJson(root));
            filePath = file;
            lastModified = modifiedTime(file);
        } catch (IOException e) {
            LOGGER.error("Failed to save config ({})", e.getClass().getSimpleName());
        }
    }

    public static synchronized void updateFormats(Path configDir) {
        Path file = configDir.resolve(FILE_NAME);
        try {
            Files.createDirectories(configDir);
            JsonObject root = readRoot(file);
            if (root == null) {
                root = new JsonObject();
                writeToggles(root);
            }
            writeFormats(root);
            Files.writeString(file, GSON.toJson(root));
            filePath = file;
            lastModified = modifiedTime(file);
        } catch (IOException e) {
            LOGGER.error("Failed to save config ({})", e.getClass().getSimpleName());
        }
    }

    public static synchronized void resetFormats() {
        INSTANCE.formats = defaultFormats();
    }

    private static JsonObject readRoot(Path file) {
        if (!Files.exists(file)) {
            return null;
        }
        try {
            if (Files.size(file) > MAX_CONFIG_BYTES) {
                LOGGER.warn("Config file is too large, rebuilding it");
                return null;
            }
            try (Reader reader = Files.newBufferedReader(file)) {
                JsonElement parsed = JsonParser.parseReader(reader);
                return parsed != null && parsed.isJsonObject() ? parsed.getAsJsonObject() : null;
            }
        } catch (Exception e) {
            LOGGER.warn("Config file could not be read ({}), rebuilding it", e.getClass().getSimpleName());
            return null;
        }
    }

    private static void writeToggles(JsonObject root) {
        root.addProperty("showDamage", INSTANCE.showDamage);
        root.addProperty("showReceivedDamage", INSTANCE.showReceivedDamage);
    }

    private static void writeFormats(JsonObject root) {
        JsonObject formats = root.has("formats") && root.get("formats").isJsonObject()
            ? root.getAsJsonObject("formats")
            : new JsonObject();
        for (DamageType type : DamageType.values()) {
            JsonObject entry = formats.has(type.name()) && formats.get(type.name()).isJsonObject()
                ? formats.getAsJsonObject(type.name())
                : new JsonObject();
            FormatEntry format = INSTANCE.getEffectiveFormat(type);
            entry.addProperty("enabled", format.enabled);
            entry.addProperty("prefix", sanitizePrefix(type, format.prefix));
            entry.addProperty("color", sanitizeColor(type, format.color));
            entry.addProperty("showDamage", format.showDamage);
            formats.add(type.name(), entry);
        }
        root.add("formats", formats);
    }

    private static String toJson() {
        JsonObject root = new JsonObject();
        writeToggles(root);
        writeFormats(root);
        return GSON.toJson(root) + "\n";
    }

    public static String formatColor(int rgb) {
        return String.format(Locale.ROOT, "#%06X", rgb & 0xFFFFFF);
    }

    public static int parseColorRgb(String value, int fallbackRgb) {
        if (value == null) {
            return fallbackRgb;
        }
        String hex = value.trim();
        if (hex.startsWith("#")) {
            hex = hex.substring(1);
        }
        if (hex.length() != 6) {
            return fallbackRgb;
        }
        for (int i = 0; i < hex.length(); i++) {
            if (Character.digit(hex.charAt(i), 16) < 0) {
                return fallbackRgb;
            }
        }
        try {
            return Integer.parseUnsignedInt(hex, 16) & 0xFFFFFF;
        } catch (NumberFormatException e) {
            return fallbackRgb;
        }
    }

    public static String sanitizeColor(DamageType type, String value) {
        return formatColor(parseColorRgb(value, type.defaultColor()));
    }

    public static String sanitizePrefix(DamageType type, String value) {
        if (value == null) {
            return type.defaultPrefix();
        }
        if (value.codePointCount(0, value.length()) > MAX_PREFIX_CODE_POINTS) {
            return type.defaultPrefix();
        }
        for (int i = 0; i < value.length(); ) {
            int codePoint = value.codePointAt(i);
            if (Character.isISOControl(codePoint) || isBidiControl(codePoint) || isSurrogate(codePoint)
                || codePoint == 0x00A7) {
                return type.defaultPrefix();
            }
            i += Character.charCount(codePoint);
        }
        return value;
    }

    private static boolean isBidiControl(int codePoint) {
        return codePoint == 0x200E || codePoint == 0x200F
            || (codePoint >= 0x202A && codePoint <= 0x202E)
            || (codePoint >= 0x2066 && codePoint <= 0x2069);
    }

    private static boolean isSurrogate(int codePoint) {
        return codePoint >= 0xD800 && codePoint <= 0xDFFF;
    }

    private static Map<DamageType, FormatEntry> defaultFormats() {
        Map<DamageType, FormatEntry> map = new EnumMap<>(DamageType.class);
        for (DamageType type : DamageType.values()) {
            map.put(type, new FormatEntry(true, type.defaultPrefix(), formatColor(type.defaultColor()),
                type.defaultShowDamage()));
        }
        return map;
    }

    private static boolean jsonBool(JsonObject obj, String key, boolean def) {
        JsonElement el = obj.get(key);
        if (el == null || el.isJsonNull() || !el.isJsonPrimitive()) {
            return def;
        }
        JsonPrimitive p = el.getAsJsonPrimitive();
        if (p.isBoolean()) {
            return p.getAsBoolean();
        }
        if (p.isNumber()) {
            return p.getAsInt() != 0;
        }
        return Boolean.parseBoolean(p.getAsString());
    }

    private static String jsonString(JsonObject obj, String key, String def) {
        JsonElement el = obj.get(key);
        if (el != null && el.isJsonPrimitive() && el.getAsJsonPrimitive().isString()) {
            return el.getAsString();
        }
        return def;
    }

    private static long modifiedTime(Path file) {
        try {
            return Files.exists(file) ? Files.getLastModifiedTime(file).toMillis() : -1L;
        } catch (IOException e) {
            return -1L;
        }
    }

    public FormatEntry getEffectiveFormat(DamageType type) {
        FormatEntry format = formats == null ? null : formats.get(type);
        if (format != null) {
            return format;
        }
        return new FormatEntry(true, type.defaultPrefix(), formatColor(type.defaultColor()),
            type.defaultShowDamage());
    }

    public FormatEntry ensureFormat(DamageType type) {
        if (formats == null) {
            formats = new EnumMap<>(DamageType.class);
        }
        FormatEntry format = formats.get(type);
        if (format == null) {
            format = new FormatEntry(true, type.defaultPrefix(), formatColor(type.defaultColor()),
                type.defaultShowDamage());
            formats.put(type, format);
        }
        return format;
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
