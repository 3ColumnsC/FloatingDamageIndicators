# Floating Damage Indicators

Floating damage numbers above every target you hit, and your own incoming damage too — color-coded by damage type.

> **Note:** FDI must be installed on both the server and client for multiplayer servers.

![Fabric](https://img.shields.io/badge/Fabric-Supported-green)
![NeoForge](https://img.shields.io/badge/NeoForge-Supported-green)
![License](https://img.shields.io/badge/License-MIT-blue)

<img src="https://res.cloudinary.com/dbtdewiqk/image/upload/v1782936767/hit_is51d7.jpg" alt="Floating Damage Indicators: hits" width="900">
<img src="https://res.cloudinary.com/dbtdewiqk/image/upload/v1782936767/effect_xpc4w8.jpg" alt="Floating Damage Indicators: effects" width="900">
<img src="https://res.cloudinary.com/dbtdewiqk/image/upload/v1782936767/projectile_wovc00.jpg" alt="Floating Damage Indicators: projectiles" width="900">

---

## ⚡ Features

- Floating damage numbers above any entity you hit
- See your own incoming damage too
- Color-coded by damage type.
- Configurable — choose which numbers show up.
- Works in both singleplayer and dedicated multiplayer.

| Type      | Icon | Color      | Example            |
|-----------|------|------------|--------------------|
| NORMAL    | —    | Red        | Normal hit         |
| CRITICAL  | ✦    | Gold       | Jump crit          |
| PROJECTILE| ➵    | Cyan       | Arrow, trident     |
| FIRE      | ♨    | Orange     | Fire Aspect        |
| POISON    | ⚗    | Green      | Poison             |
| WITHER    | ☠    | Dark gray  | Wither effect      |
| RECEIVING | —    | Light gray | Damage you take    |

---

## ⚙️ Configuration

After launching the game once, a configuration file will be generated:

```
config/floatingdamageindicators.json
```

Available options:

```
{
  "showDamage": true,
  "showReceivedDamage": true,
}
```

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
