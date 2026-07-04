# Floating Damage Indicators

Floating damage numbers above every target you hit, and your own incoming damage too — color-coded by damage type.

> **Note:** FDI must be installed on both the server and client for multiplayer servers.

![Fabric](https://img.shields.io/badge/Fabric-Supported-green)
![NeoForge](https://img.shields.io/badge/NeoForge-Supported-green)
![License](https://img.shields.io/badge/License-MIT-blue)

<img src="https://res.cloudinary.com/dbtdewiqk/image/upload/v1782936767/hit_is51d7.jpg" alt="Floating Damage Indicators: hits" width="900">
<img src="https://res.cloudinary.com/dbtdewiqk/image/upload/v1782936767/effect_xpc4w8.jpg" alt="Floating Damage Indicators: effects" width="900">
<img src="https://res.cloudinary.com/dbtdewiqk/image/upload/v1783148317/projectile_instant_s4i0xq.jpg" alt="Floating Damage Indicators: projectiles & instant kill" width="900">

---

## ⚡ Features

- Floating damage numbers above any entity you hit
- See your own incoming damage too
- Color-coded by damage type.
- Configurable — choose which numbers show up.
- Works in both singleplayer and dedicated multiplayer.

| Type         | Icon           | Color      | Example                      |
|--------------|----------------|------------|------------------------------|
| NORMAL       | —              | Red        | Normal hit                   |
| CRITICAL     | ✦              | Gold       | Jump crit                    |
| PROJECTILE   | ➵              | Cyan       | Arrow, trident               |
| FIRE         | ♨              | Orange     | Fire Aspect                  |
| POISON       | ⚗              | Green      | Poison                       |
| WITHER       | ☠              | Dark gray  | Wither effect                |
| RECEIVING    | —              | Light gray | Damage you take              |
| INSTANT_KILL | ⚡ INSTANT KILL | Gold       | Kill an enemy in one hit     |

---

## ⚙️ Configuration

After launching the game once, a configuration file will be generated:

```
config/floatingdamageindicators.json
```

Available options:

> **Note:** The `formats` section is **client-side only** — the server only reads `showDamage`, `showReceivedDamage`, and `INSTANT_KILL`'s `enabled`. To customize prefixes, colors, or `showDamage`, edit the config file **on your client**.
>
> **Tip:** Some Unicode characters or emojis may not display correctly depending on your Minecraft font. If a character shows as a blank square, try a different one. Stick to basic Unicode symbols for best results.
>
> **Tip:** Colors must be 6-digit hex (RRGGBB). Invalid values like `"ZZZ123"` or `"FF"` will silently fall back to the default for that damage type.


```
{
  "showDamage": true,
  "showReceivedDamage": true,
  "formats": {
    "NORMAL":       { "enabled": true, "prefix": "", "color": "FF3333", "showDamage": true },
    "CRITICAL":     { "enabled": true, "prefix": "\u2726", "color": "FFD700", "showDamage": true },
    "PROJECTILE":   { "enabled": true, "prefix": "\u27B5", "color": "00FFFF", "showDamage": true },
    "FIRE":         { "enabled": true, "prefix": "\u2668", "color": "FF6600", "showDamage": true },
    "POISON":       { "enabled": true, "prefix": "\u2697", "color": "4A9E2F", "showDamage": true },
    "WITHER":       { "enabled": true, "prefix": "\u2620", "color": "3C3C3C", "showDamage": true },
    "RECEIVING":    { "enabled": true, "prefix": "(You) ", "color": "AAAAAA", "showDamage": true },
    "INSTANT_KILL": { "enabled": true, "prefix": "\u26A1 INSTANT KILL", "color": "FFD700", "showDamage": false }
  }
}
```

Each format entry:
- **enabled** — show/hide this damage type entirely
- **prefix** — custom text shown before (or instead of) the number
- **color** — 6-digit hex color (RGB)
- **showDamage** — if false, only the prefix is shown (no damage number)

---

## 📦 Requirements

### Fabric

- Fabric API
- (+26.X) Java 25 or newer

### NeoForge

- (+26.X) Java 25 or newer

---

## 📜 License

This project is licensed under the MIT License.
