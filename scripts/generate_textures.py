#!/usr/bin/env python3
"""
Generates vitraux textures from Minecraft 1.21.1's glazed terracotta textures.
Run from the root of the vitraux project:

    pip install Pillow
    python scripts/generate_textures.py
"""
import io
import os
import sys
import zipfile
import random
from pathlib import Path

try:
    from PIL import Image
except ImportError:
    print("Pillow manquant. Installe-le avec :")
    print("  pip install Pillow")
    sys.exit(1)

COLORS = [
    "white", "orange", "magenta", "light_blue", "yellow", "lime",
    "pink", "gray", "light_gray", "cyan", "purple", "blue",
    "brown", "green", "red", "black",
]


def find_mc_jar() -> Path | None:
    appdata = Path(os.environ.get("APPDATA", ""))
    candidates = [
        # Modrinth App (detected on this machine)
        appdata / "ModrinthApp" / "meta" / "versions" / "1.21.1-0.19.3" / "1.21.1-0.19.3.jar",
        # Vanilla launcher
        appdata / ".minecraft" / "versions" / "1.21.1" / "1.21.1.jar",
        appdata / ".minecraft" / "versions" / "1.21.4" / "1.21.4.jar",
        Path.home() / ".minecraft" / "versions" / "1.21.1" / "1.21.1.jar",
    ]
    for p in candidates:
        if p.exists():
            return p
    return None


def apply_glass_alpha(img: Image.Image) -> Image.Image:
    """
    Transforms an opaque terracotta texture into a semi-transparent glass texture.

    Formula: darker pixels (pattern outlines / deep colours) are more opaque,
    brighter pixels (flat colour areas) are more transparent. This mimics the
    way real stained glass looks — the lead came lines are darker/thicker while
    the coloured glass sections let light through.

    Alpha range: ~50 (bright areas, transparent) → ~180 (dark areas, opaque)
    """
    rgba = img.convert("RGBA")
    pixels = rgba.load()
    for y in range(rgba.height):
        for x in range(rgba.width):
            r, g, b, _ = pixels[x, y]
            lum = (r * 299 + g * 587 + b * 114) // 1000
            new_alpha = max(50, min(180, 185 - lum // 3))
            pixels[x, y] = (r, g, b, new_alpha)
    return rgba


def generate_lead_frame(size: int = 16) -> Image.Image:
    """
    Generates a charcoal / dark-lead texture for the pane edges.
    A small random grain makes it look like actual lead rather than flat grey.
    """
    frame = Image.new("RGBA", (size, size), (0, 0, 0, 255))
    pixels = frame.load()
    rng = random.Random(0xDEADBEEF)  # deterministic seed
    for y in range(size):
        for x in range(size):
            v = 28 + rng.randint(0, 18)
            pixels[x, y] = (v, v, v, 255)
    return frame


def main() -> None:
    mc_jar = find_mc_jar()
    if mc_jar is None:
        print("ERREUR : JAR Minecraft 1.21.1 introuvable.")
        print("Lance le launcher Minecraft une fois pour télécharger 1.21.1,")
        print("ou modifie `find_mc_jar()` dans ce script pour pointer vers ton JAR.")
        sys.exit(1)

    print(f"JAR trouvé : {mc_jar}")

    out_dir = Path("src/main/resources/assets/vitraux/textures/block")
    out_dir.mkdir(parents=True, exist_ok=True)

    with zipfile.ZipFile(mc_jar) as jar:
        for color in COLORS:
            tc_path = f"assets/minecraft/textures/block/{color}_glazed_terracotta.png"
            try:
                data = jar.read(tc_path)
            except KeyError:
                print(f"  SKIP  {tc_path} (introuvable dans le JAR)")
                continue

            img = Image.open(io.BytesIO(data))
            result = apply_glass_alpha(img)
            out_path = out_dir / f"{color}_vitraux.png"
            result.save(out_path, format="PNG")
            print(f"  OK    {color}_vitraux.png")

            # Rotated variants for texture_facing blockstate property
            for angle, suffix in [(90, "r90"), (180, "r180"), (270, "r270")]:
                rotated = result.rotate(angle)
                rotated.save(out_dir / f"{color}_vitraux_{suffix}.png", format="PNG")
            print(f"        + r90 / r180 / r270")

    # Lead frame texture (shared by all variants)
    frame = generate_lead_frame()
    frame.save(out_dir / "vitraux_frame.png", format="PNG")
    print("  OK    vitraux_frame.png")

    print()
    print("Textures générées ! Rebuild avec :")
    print("  .\\gradlew build")


if __name__ == "__main__":
    main()
