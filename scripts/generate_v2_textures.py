#!/usr/bin/env python3
"""
Generates v2 textures:
  - blank_vitraux.png        (16x16 transparent glass pane face)
  - glaziers_bench_top.png   (16x16 workbench-style top)
  - glaziers_bench_front.png (16x16 front face with glass tools)
  - glaziers_bench_side.json (16x16 wood side)
  - template.png             (16x16 rolled blueprint item)

Run from vitraux project root:
    python scripts/generate_v2_textures.py
"""
from pathlib import Path
from PIL import Image

BLOCK_OUT = Path("src/main/resources/assets/vitraux/textures/block")
ITEM_OUT  = Path("src/main/resources/assets/vitraux/textures/item")
BLOCK_OUT.mkdir(parents=True, exist_ok=True)
ITEM_OUT.mkdir(parents=True, exist_ok=True)

T  = (0,   0,   0,   0)   # transparent
W  = (230, 230, 230, 200)  # clear glass (semi-transparent white)
B  = (80,  60,  40,  255)  # dark wood border
P  = (160, 120, 70,  255)  # plank wood
G  = (200, 200, 220, 180)  # glass tint
K  = (40,  40,  40,  255)  # dark grey (tool outline)
LW = (245, 245, 245, 255)  # light wood highlight


def make_image(grid):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    px = img.load()
    for y, row in enumerate(grid):
        for x, c in enumerate(row):
            px[x, y] = c
    return img


# ── blank_vitraux.png ─────────────────────────────────────────────────────────
# Mostly transparent with a very faint grid to hint at a blank canvas
BV = (200, 220, 255, 60)  # faint blue tint
blank_grid = [[BV] * 16 for _ in range(16)]
# Thin border pixels
for i in range(16):
    blank_grid[0][i] = (180, 200, 240, 120)
    blank_grid[15][i] = (180, 200, 240, 120)
    blank_grid[i][0] = (180, 200, 240, 120)
    blank_grid[i][15] = (180, 200, 240, 120)

out = BLOCK_OUT / "blank_vitraux.png"
make_image(blank_grid).save(out, "PNG")
print(f"  OK    {out}")


# ── glaziers_bench_top.png ────────────────────────────────────────────────────
# Wood planks + two glass pane strips across the top
gt = [
#    0    1    2    3    4    5    6    7    8    9   10   11   12   13   14   15
    [ P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P],  # 0
    [ P,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  P],  # 1
    [ P,  LW,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,  LW,  P],  # 2
    [ P,  LW,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,  LW,  P],  # 3
    [ P,  LW,   G,   G,  LW,  LW,   G,   G,   G,   G,  LW,  LW,   G,   G,  LW,  P],  # 4
    [ P,  LW,   G,   G,  LW,  LW,   G,   G,   G,   G,  LW,  LW,   G,   G,  LW,  P],  # 5
    [ P,  LW,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,  LW,  P],  # 6
    [ P,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  P],  # 7
    [ B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B],  # 8 plank seam
    [ P,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  P],  # 9
    [ P,  LW,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,  LW,  P],  # 10
    [ P,  LW,   G,   G,  LW,  LW,   G,   G,   G,   G,  LW,  LW,   G,   G,  LW,  P],  # 11
    [ P,  LW,   G,   G,  LW,  LW,   G,   G,   G,   G,  LW,  LW,   G,   G,  LW,  P],  # 12
    [ P,  LW,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,  LW,  P],  # 13
    [ P,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  P],  # 14
    [ P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P],  # 15
]
out = BLOCK_OUT / "glaziers_bench_top.png"
make_image(gt).save(out, "PNG")
print(f"  OK    {out}")


# ── glaziers_bench_front.png ──────────────────────────────────────────────────
# Wood plank face with a small glass window/tool silhouette
gf = [
    [ P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P],
    [ P,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  P],
    [ P,  LW,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,  LW,  P],
    [ P,  LW,   G,   K,   K,   G,   G,   G,   G,   G,   G,   K,   K,   G,  LW,  P],
    [ P,  LW,   G,   K,   G,   K,   G,   G,   G,   G,   K,   G,   K,   G,  LW,  P],
    [ P,  LW,   G,   G,   K,   G,   K,   G,   G,   K,   G,   K,   G,   G,  LW,  P],
    [ P,  LW,   G,   G,   G,   K,   G,   K,   K,   G,   K,   G,   G,   G,  LW,  P],
    [ P,  LW,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,   G,  LW,  P],
    [ B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B],
    [ P,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  P],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ P,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  P],
    [ P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P],
]
out = BLOCK_OUT / "glaziers_bench_front.png"
make_image(gf).save(out, "PNG")
print(f"  OK    {out}")


# ── glaziers_bench_side.png ───────────────────────────────────────────────────
# Simple wood plank side
gs = [
    [ P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P],
    [ P,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  P],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B,   B],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ P,  LW,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,  LW,  P],
    [ P,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  LW,  P],
    [ P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P,   P],
]
out = BLOCK_OUT / "glaziers_bench_side.png"
make_image(gs).save(out, "PNG")
print(f"  OK    {out}")


# ── template item ─────────────────────────────────────────────────────────────
# Rolled scroll / blueprint look
SC = (240, 220, 150, 255)  # parchment
SB = (180, 150, 80,  255)  # scroll border
SL = (100, 140, 200, 255)  # blue ink lines
SE = (140, 100, 50,  255)  # dark edge

tpl = [
#    0    1    2    3    4    5    6    7    8    9   10   11   12   13   14   15
    [ T,   T,  SE,  SB,  SB,  SB,  SB,  SB,  SB,  SB,  SB,  SB,  SB,  SE,  T,   T],
    [ T,  SE,  SB,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SB,  SE,  T],
    [SE,  SB,  SC,  SC,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SC,  SC,  SB,  SE],
    [SB,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SB],
    [SB,  SC,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SC,  SB],
    [SB,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SB],
    [SB,  SC,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SC,  SB],
    [SB,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SB],
    [SB,  SC,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SC,  SB],
    [SB,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SB],
    [SB,  SC,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SC,  SB],
    [SB,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SB],
    [SB,  SC,  SC,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SL,  SC,  SC,  SB],
    [SE,  SB,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SB,  SE],
    [ T,  SE,  SB,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SC,  SB,  SE,  T],
    [ T,   T,  SE,  SB,  SB,  SB,  SB,  SB,  SB,  SB,  SB,  SB,  SB,  SE,  T,   T],
]
out = ITEM_OUT / "template.png"
make_image(tpl).save(out, "PNG")
print(f"  OK    {out}")


# ── white_tint_pane.png ──────────────────────────────────────────────────────
# Neutral white semi-transparent texture used as the #pane face on custom_vitraux
# block models. BlockColorProvider multiplies it by the pixel art's average colour,
# so shaders see a correctly-coloured translucent pane for coloured-shadow projection.
WG = (255, 255, 255, 140)  # white glass (tintable) — alpha matches terracotta vitraux shadow intensity
WE = (255, 255, 255, 180)  # slightly more opaque border
wt_grid = [[WG] * 16 for _ in range(16)]
for i in range(16):
    wt_grid[0][i] = wt_grid[15][i] = WE
    wt_grid[i][0] = wt_grid[i][15] = WE
out = BLOCK_OUT / "white_tint_pane.png"
make_image(wt_grid).save(out, "PNG")
print(f"  OK    {out}")


# ── transparent_pane.png ──────────────────────────────────────────────────────
# Fully transparent 16×16; used as the pane face on custom_vitraux blocks so the
# block model doesn't z-fight with the BER-rendered pixel art.
tp_grid = [[T] * 16 for _ in range(16)]
out = BLOCK_OUT / "transparent_pane.png"
make_image(tp_grid).save(out, "PNG")
print(f"  OK    {out}")


if __name__ == "__main__":
    pass
