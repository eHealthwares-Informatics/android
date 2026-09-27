#!/usr/bin/env python3
"""Regenerate the RxSoft mobile launcher icons.

- Backdrop (ic_launcher_background): solid white, 432x432.
- Foreground (ic_launcher_foreground): the honeycomb logo scaled so it fills
  the adaptive-icon *visible* area (~66dp of the 108dp canvas, i.e. ~61% of
  the canvas width). The glyph fills 100% of that visible area instead of the
  tiny 23% dot the old asset drew.
- Legacy mipmap ic_launcher(.webp/.png) + ic_launcher_round at every density:
  the logo on a white rounded/square tile, also filling the tile.

Run from the rxsoft-mobile/ directory:  python3 scripts/make_launcher_icons.py
"""
from PIL import Image, ImageDraw
import os

RES = os.path.join(os.path.dirname(__file__), '..', 'app', 'src', 'main', 'res')
LOGO = '/Users/john/develop/rxsoft/ehealthwares-logo/logo_concepts/honey_combb_trannnsparent.png'

WHITE = (255, 255, 255, 255)

# Adaptive icon canvas is 108dp; the visible circle is 66dp -> 66/108 = 61.1%.
ADAPTIVE = 432                       # px canvas (4x of 108dp)
VISIBLE_FRACTION = 66 / 108          # ~0.611 — the part of the canvas that shows
FG_FILL = 0.98                       # logo fills ~98% of the visible area

DENSITIES = {  # launcher tile sizes in px per density
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192,
}


def load_logo():
    logo = Image.open(LOGO).convert('RGBA')
    # Crop to the logo's opaque bounding box so it truly "fills"
    bbox = logo.getbbox()
    if bbox:
        logo = logo.crop(bbox)
    return logo


def make_background():
    bg = Image.new('RGBA', (ADAPTIVE, ADAPTIVE), WHITE)
    bg.save(os.path.join(RES, 'drawable', 'ic_launcher_background.png'))


def make_foreground(logo):
    fg = Image.new('RGBA', (ADAPTIVE, ADAPTIVE), (0, 0, 0, 0))
    visible = int(ADAPTIVE * VISIBLE_FRACTION * FG_FILL)
    scaled = logo.resize((visible, visible), Image.LANCZOS)
    off = (ADAPTIVE - visible) // 2
    fg.paste(scaled, (off, off), scaled)
    fg.save(os.path.join(RES, 'drawable', 'ic_launcher_foreground.png'))


def make_legacy(logo, size, rounded):
    tile = Image.new('RGBA', (size, size), WHITE)
    mask = Image.new('L', (size, size), 0)
    d = ImageDraw.Draw(mask)
    if rounded:
        d.ellipse((0, 0, size, size), fill=255)
    else:
        radius = size // 5  # squircle-ish corner radius like Pixel icons
        d.rounded_rectangle((0, 0, size, size), radius=radius, fill=255)
    logo_scaled = logo.resize((int(size * 0.92), int(size * 0.92)), Image.LANCZOS)
    off = (size - logo_scaled.size[0]) // 2
    tile.paste(logo_scaled, (off, off), logo_scaled)
    # apply mask for the round variant (square variant keeps white corners for webp? No:
    # legacy launchers use the shape as-is, so square = full-bleed white tile)
    if rounded:
        out = Image.new('RGBA', (size, size), (0, 0, 0, 0))
        out.paste(tile, (0, 0), mask)
        return out
    return tile


def main():
    logo = load_logo()
    make_background()
    make_foreground(logo)

    for dpi, size in DENSITIES.items():
        d = os.path.join(RES, f'mipmap-{dpi}')
        os.makedirs(d, exist_ok=True)
        square = make_legacy(logo, size, rounded=False)
        rnd = make_legacy(logo, size, rounded=True)
        square.save(os.path.join(d, 'ic_launcher.png'))
        rnd.save(os.path.join(d, 'ic_launcher_round.png'))
        # Remove stale webp so the new pngs are used
        for stale in ('ic_launcher.webp', 'ic_launcher_round.webp'):
            p = os.path.join(d, stale)
            if os.path.exists(p):
                os.remove(p)
        print(f'mipmap-{dpi}: wrote ic_launcher.png + ic_launcher_round.png ({size}px)')

    print('Launcher icons regenerated: white backdrop, logo filling the visible area.')


if __name__ == '__main__':
    main()
