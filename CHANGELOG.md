# Changelog

## [0.2.3b] — 2026-06-19

### Added
- **Rectangle fill tool** — right-click + drag in cell editor to fill any rectangular zone; shows a live preview overlay during selection
- **Neighbor pixel strips** — cell editor shows the last row/column of adjacent cells on each border to help align designs across cells
- **Fill cell button** — one-click bucket fill of the entire current cell with the selected color
- **Colored shadows** — custom vitraux now cast colored shadows matching their average design color via `BlockColorProvider` + `tintindex`; works automatically with any shader that supports `shadowcolor0`
- `fill_cell` and `fill_rect` C2S network payloads

### Fixed
- **Connected panes showing as a grid of squares** — removed visible frame faces (outer tips and top/bottom edges) from side piece block models; eliminated inter-block border lines
- **Top/bottom faces conditional on vertical neighbors** — custom vitraux now tracks `CONNECTED_UP` / `CONNECTED_DOWN` block state properties; cap faces are only rendered when the pane is exposed (no adjacent pane above/below)
- **Transparent pixels rendering dark** — white_tint_pane alpha raised to 140 with normalized average color (max channel always = 255) so tint stays vivid without darkening transparent areas
- **Z-fighting flicker on applied designs** — BER quads offset 0.003 outward from block model face positions
- **Template pane connections reset** — applying a template now copies NORTH/SOUTH/EAST/WEST from the existing block state instead of resetting all connections to false
- **GUI elements overlapping buttons** — moved all content rendering to `drawBackground()` which executes before button children
- **Shadow intensity weaker than legacy vitraux** — resolved by vivid color normalization; custom vitraux now cast shadows of equivalent intensity

### Changed
- Replaced `renderProjection()` with shader-native colored shadow approach
- Cell editor redesigned: preview panel on the left, pixel editor on the right, horizontal palette below
- Overview mode hover now shows sub-grid lines and cell highlight
- Currently edited cell highlighted in orange in the overview

---

## [0.2.0b] — 2026-06-19

### Added

**Blocks**
- `vitraux:blank_vitraux` — transparent glass pane that accepts a vitraux template; crafted from 6 glass panes (3×2)
- `vitraux:custom_vitraux` — pane with a per-block 16×16 pixel custom pattern, placed automatically when a template is applied
- `vitraux:glaziers_bench` — crafting station for pixel art design; opens a GUI; crafted from 2 glass panes + 2 oak planks

**Items**
- `vitraux:template` — stores a full canvas design in NBT; applying it to a connected group of blank vitraux distributes the correct cell of the design to each pane; stacks up to 16; consumed on use

**GUI**
- Glazier's Bench pixel editor
  - **Overview mode**: shows all cells (up to 4×4) at a glance; click any cell to enter edit mode
  - **Cell edit mode**: 16×16 pixel grid at 8 px/pixel; drag to paint; Back button returns to overview
  - Palette of 17 swatches (16 dye colours + transparent)
  - Canvas size controls (W-/W+/H-/H+); canvas starts at 1×1 and goes up to 4×4
  - **Create Template** button outputs a template item to the player's inventory

**BFS template application**
- Right-clicking a blank vitraux with a template triggers a BFS flood-fill through all connected blank vitraux
- Each pane receives its own 16×16 cell of the design based on bounding box position
- Panes outside the template bounds are left unchanged; template is consumed on use

**Distance LOD rendering**
- Custom vitraux render per-block pixel art within 24 blocks
- Beyond 24 blocks falls back to blank vitraux appearance, saving GPU memory
- Per-block GPU textures built on demand and cached until pixel data changes

### Technical
- C2S packets: `set_pixel`, `set_dimensions`, `create_template`
- S2C packet: `canvas_sync` (on screen open and after dimension changes)
- Block entity types: `custom_vitraux`, `glaziers_bench`
- Screen handler type: `glaziers_bench`
- `NativeImageBackedTexture` per `CustomVitrauxBlockEntity` with dirty-flag tracking

---

## [0.1b] — 2026-06-19

### Added

**Blocks**
- 16 vitraux pane blocks (`white` through `black`), one per dye colour
- Glazed terracotta patterns converted to semi-transparent RGBA textures
- Luminance-based alpha: dark pattern lines are more opaque, bright colour areas are more transparent
- 4 texture orientations per block via the `texture_facing` blockstate property (0 / 90 / 180 / 270°)
- Pattern auto-orients toward the player on placement (mirrors glazed terracotta behaviour)
- Silk-touch-only loot tables — vitraux drop nothing without silk touch

**Items**
- `vitraux:pincette` — Glazier's Pincers, crafted from 2 iron ingots
- Right-clicking a placed vitraux with the Pincers rotates the pattern 90° CCW
- Custom 16×16 pixel art texture

**Creative Tab**
- Dedicated *Vitraux* tab containing all 16 panes and the Pincers

**Recipes**
- 6× `{colour}_glazed_terracotta` → 6× `{colour}_vitraux` (3×2 shaped)
- 2× iron ingot (column) → 1× Pincers

**Shader Support**
- Automatic detection of Iris Shaders via reflection (no compile-time dependency)
- Bundled `vitraux_shadows` shader pack auto-installed to `shaderpacks/` on first launch when Iris is present
- `vitraux_shadows` implements per-texel coloured shadow projection using `shadowcolor0`
- Native compatibility with popular shader packs (Complementary, BSL, SEUS) — no configuration required
