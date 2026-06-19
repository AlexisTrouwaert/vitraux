#!/usr/bin/env python3
"""
Generates the pincette (glazier's pincers) item texture — 16×16 pixel art.
Run from the root of the vitraux project:
    python scripts/generate_pincette.py
"""
from pathlib import Path
from PIL import Image

# Palette
T = (0,   0,   0,   0)    # transparent
O = (35,  35,  35,  255)  # dark outline
D = (88,  88,  88,  255)  # shadow/body dark
M = (148, 148, 148, 255)  # main metal
L = (205, 205, 205, 255)  # highlight
P = (190, 155, 75,  255)  # brass pivot bolt

# 16×16 — glazier's pincers at 45°
# Upper-left = jaws (flat-nose, slightly open)
# Centre     = brass pivot bolt
# Lower-right = handle
grid = [
#    0   1   2   3   4   5   6   7   8   9  10  11  12  13  14  15
    [ T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T], #  0
    [ T,  O,  L,  M,  D,  O,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T], #  1  jaw 1 flat tip
    [ T,  T,  O,  M,  D,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T], #  2  jaw 1
    [ T,  T,  T,  O,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T], #  3  gap (jaws open)
    [ T,  T,  T,  T,  O,  M,  D,  T,  T,  T,  T,  T,  T,  T,  T,  T], #  4  jaw 2
    [ T,  T,  T,  D,  M,  M,  D,  O,  T,  T,  T,  T,  T,  T,  T,  T], #  5  jaw 2 flat tip
    [ T,  T,  T,  T,  D,  P,  M,  L,  O,  T,  T,  T,  T,  T,  T,  T], #  6  brass pivot bolt
    [ T,  T,  T,  T,  T,  O,  D,  M,  M,  L,  O,  T,  T,  T,  T,  T], #  7  handle forming
    [ T,  T,  T,  T,  T,  T,  O,  D,  M,  M,  L,  O,  T,  T,  T,  T], #  8
    [ T,  T,  T,  T,  T,  T,  T,  O,  D,  M,  M,  L,  O,  T,  T,  T], #  9
    [ T,  T,  T,  T,  T,  T,  T,  T,  O,  D,  M,  M,  L,  O,  T,  T], # 10
    [ T,  T,  T,  T,  T,  T,  T,  T,  T,  O,  D,  M,  M,  L,  O,  T], # 11
    [ T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  O,  D,  M,  L,  D,  O], # 12
    [ T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  O,  D,  D,  O,  T], # 13  handle end
    [ T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T], # 14
    [ T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T,  T], # 15
]

def main() -> None:
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    pixels = img.load()
    for y, row in enumerate(grid):
        for x, color in enumerate(row):
            pixels[x, y] = color

    out = Path("src/main/resources/assets/vitraux/textures/item/pincette.png")
    out.parent.mkdir(parents=True, exist_ok=True)
    img.save(out, format="PNG")
    print(f"  OK    {out}")

if __name__ == "__main__":
    main()
