# Vitraux

**Minecraft Fabric mod — version 1.0.0 — 1.21.1**

Vitraux adds 16 stained glass panes with the ornate patterns of glazed terracotta. Each pane casts coloured light when the sun shines through it, and its pattern can be freely rotated in place using the **Glazier's Pincers** tool.

---

## Features

- **16 coloured vitraux panes** — one per dye colour, using the glazed terracotta patterns as semi-transparent glass
- **Coloured light projection** — sunlight passing through a vitraux casts its pattern on the floor and walls below (shader-dependent; see [Shader Support](#shader-support))
- **Pattern rotation** — right-click any placed vitraux with the Glazier's Pincers to rotate the texture by 90° (4 orientations)
- **Auto-orient on placement** — the pattern faces the player when the block is placed, like glazed terracotta
- **Dedicated creative tab** — all blocks and the pincers are grouped under the *Vitraux* tab

---

## Requirements

| Dependency | Version | Required |
|---|---|---|
| Minecraft | 1.21.1 | ✅ |
| Fabric Loader | ≥ 0.16.9 | ✅ |
| Fabric API | 0.107.0+1.21.1 | ✅ |
| Iris Shaders | ≥ 1.8.8 | Optional |
| Sodium | any | Optional (recommended with Iris) |

---

## Installation

1. Install [Fabric Loader](https://fabricmc.net/use/installer/) for Minecraft 1.21.1.
2. Download [Fabric API](https://modrinth.com/mod/fabric-api) and place it in your `mods/` folder.
3. Place `vitraux-1.0.0.jar` in your `mods/` folder.
4. *(Optional)* Install [Iris Shaders](https://modrinth.com/mod/iris) and [Sodium](https://modrinth.com/mod/sodium) for coloured shadow projection.
5. Launch the game.

---

## Blocks

All 16 dye colours are available. Each vitraux pane connects to adjacent blocks exactly like a vanilla glass pane.

| ID | Name | Recipe ingredient |
|---|---|---|
| `vitraux:white_vitraux` | White Vitraux | White Glazed Terracotta |
| `vitraux:orange_vitraux` | Orange Vitraux | Orange Glazed Terracotta |
| `vitraux:magenta_vitraux` | Magenta Vitraux | Magenta Glazed Terracotta |
| `vitraux:light_blue_vitraux` | Light Blue Vitraux | Light Blue Glazed Terracotta |
| `vitraux:yellow_vitraux` | Yellow Vitraux | Yellow Glazed Terracotta |
| `vitraux:lime_vitraux` | Lime Vitraux | Lime Glazed Terracotta |
| `vitraux:pink_vitraux` | Pink Vitraux | Pink Glazed Terracotta |
| `vitraux:gray_vitraux` | Gray Vitraux | Gray Glazed Terracotta |
| `vitraux:light_gray_vitraux` | Light Gray Vitraux | Light Gray Glazed Terracotta |
| `vitraux:cyan_vitraux` | Cyan Vitraux | Cyan Glazed Terracotta |
| `vitraux:purple_vitraux` | Purple Vitraux | Purple Glazed Terracotta |
| `vitraux:blue_vitraux` | Blue Vitraux | Blue Glazed Terracotta |
| `vitraux:brown_vitraux` | Brown Vitraux | Brown Glazed Terracotta |
| `vitraux:green_vitraux` | Green Vitraux | Green Glazed Terracotta |
| `vitraux:red_vitraux` | Red Vitraux | Red Glazed Terracotta |
| `vitraux:black_vitraux` | Black Vitraux | Black Glazed Terracotta |

---

## Crafting

### Vitraux Pane

Produces **6 panes** of the corresponding colour.

```
[ G ][ G ][ G ]
[ G ][ G ][ G ]
```

`G` = any glazed terracotta block of the desired colour  
*(e.g. 6× Cyan Glazed Terracotta → 6× Cyan Vitraux)*

### Glazier's Pincers (`vitraux:pincette`)

```
[ I ]
[ I ]
```

`I` = Iron Ingot — produces **1 Pincers**

---

## Usage

### Placing a vitraux

Place a vitraux pane like any glass pane. It connects to adjacent solid blocks and other panes automatically. The pattern auto-orients toward you when placed.

### Rotating the pattern

Hold the **Glazier's Pincers** in your main hand and right-click the pane. Each click rotates the pattern **90° counter-clockwise**. There are 4 orientations; the 4th click brings the pattern back to its original position.

The rotation is stored per-block in the `texture_facing` blockstate property (values `0`–`3`).

---

## Shader Support

### With a popular shader pack (recommended)

Shader packs such as **Complementary Reimagined**, **BSL Shaders**, **SEUS**, and most Iris-compatible packs already implement coloured shadows for translucent blocks. Because vitraux extend the vanilla `StainedGlassPaneBlock`, they benefit from this automatically — no configuration needed.

To improve shadow sharpness, raise the **Shadow Resolution** in your shader's options (4096 or higher).

### With the bundled `vitraux_shadows` pack

If Iris is installed but no other shader pack is active, the mod automatically installs a minimal shader pack to:

```
<game directory>/shaderpacks/vitraux_shadows/
```

Select **`vitraux_shadows`** in *Options → Video Settings → Shader Packs* to activate coloured pattern projection.

The bundled pack is intentionally minimal (no bloom, no ambient occlusion changes, no colour grading) — it only adds coloured light projection.

### Without shaders

The blocks render with the vanilla DyeColor tint system, giving a flat coloured tint. No pattern projection is produced. The blocks remain fully functional as decorative panes.

---

## Technical Details

### Texture generation

Textures are derived at build time from the vanilla Minecraft 1.21.1 JAR using the script `scripts/generate_textures.py`. Each glazed terracotta texture is converted to RGBA with a luminance-based alpha (dark pixels → more opaque; bright pixels → more transparent), producing the stained-glass look.

Four orientations (`r0`, `r90`, `r180`, `r270`) are generated per colour to support the `texture_facing` blockstate property without UV manipulation in model files.

### Blockstate property

`texture_facing` is an `IntProperty` (range `0`–`3`). It controls which pre-rotated texture variant is used across all five model parts (`post`, `side`, `side_alt`, `noside`, `noside_alt`).

---

## Building from Source

Requirements: JDK 21, internet connection (Gradle downloads dependencies automatically).

```bat
git clone <repo>
cd vitraux

REM Generate textures (requires Python 3.12+ and Pillow)
pip install Pillow
python scripts/generate_textures.py
python scripts/generate_pincette.py

REM Build
.\gradlew build
```

The output JAR is at `build/libs/vitraux-1.0.0.jar`.

---

## Licence

All rights reserved — Alexis Trouwaert, 2026.  
Textures derived from Minecraft assets © Mojang Studios.
