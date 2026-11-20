#!/usr/bin/env python3
"""Regenerate the RxSoft mobile launcher icons from the brand mark.

Source: `scripts/launcher-source.png` (the green medical cross with the white
"Rx" plus the red PHARMACY wordmark).

Outputs (overwritten in app/src/main/res):
  drawable/ic_launcher_background.png   solid white, 108dp adaptive canvas
  drawable/ic_launcher_foreground.png   transparent, emblem filling the
                                        adaptive-icon visible area (66/108)
  mipmap-*/ic_launcher.png              legacy square tile per density
  mipmap-*/ic_launcher_round.png        legacy round tile per density

Usage (from rxsoft-mobile/):
  python3 scripts/make_launcher_icons.py                # cross + Rx + PHARMACY
  python3 scripts/make_launcher_icons.py --cross-only   # cross + Rx only

No third-party dependencies: PNG I/O and resampling live in scripts/png_io.py.
"""
import argparse
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from png_io import (Bitmap, blank, blit, clip_mask, downsample, key_white,  # noqa: E402
                    read_png, write_png, WHITE)

HERE = os.path.dirname(os.path.abspath(__file__))
RES = os.path.normpath(os.path.join(HERE, '..', 'app', 'src', 'main', 'res'))
SOURCE = os.path.join(HERE, 'launcher-source.png')

# Adaptive icons are a 108dp canvas with a 66dp visible area -> 61.1%.
ADAPTIVE = 432
VISIBLE_FRACTION = 66 / 108
FG_MARGIN = 0.94        # emblem stays inside the visible circle, 6% breathing room
TILE_FILL = 0.80        # emblem size relative to a legacy square tile
NAME_FILL = 0.90        # with the wordmark the stack fills the tile more
# Round fills (legacy round tile and adaptive foreground) are derived from the
# emblem's content box (see round_fill()), so the wordmark's corners never get
# clipped by a circular launcher mask.

DENSITIES = {
    'mdpi': 48,
    'hdpi': 72,
    'xhdpi': 96,
    'xxhdpi': 144,
    'xxxhdpi': 192,
}


def is_green(px, i):
    r, g, b = px[i], px[i + 1], px[i + 2]
    return g > 60 and g < 180 and r < 110 and b < 130 and g - r > 40 and g - b > 25


def is_red(px, i):
    r, g, b = px[i], px[i + 1], px[i + 2]
    return r > 140 and g < 110 and b < 130 and r - g > 70


def bbox(src, predicate, region=None):
    """Bounding box of the pixels matching `predicate` (optionally in a region)."""
    x0, y0, x1, y1 = region or (0, 0, src.w, src.h)
    px = src.px
    minx, miny, maxx, maxy = src.w, src.h, -1, -1
    for y in range(y0, y1):
        base = y * src.w
        for x in range(x0, x1):
            if predicate(px, (base + x) * 4):
                if x < minx:
                    minx = x
                if x > maxx:
                    maxx = x
                if y < miny:
                    miny = y
                if y > maxy:
                    maxy = y
    if maxx < 0:
        raise SystemExit('source image: could not locate the emblem')
    return minx, miny, maxx + 1, maxy + 1


def largest_green_blob(src):
    """Bounding box of the biggest green connected component (the cross).

    The poster's green rounded frame is a separate component, so this isolates
    the cross itself.
    """
    w, h = src.w, src.h
    px = src.px
    mask = bytearray(w * h)
    for y in range(h):
        base = y * w
        for x in range(w):
            if is_green(px, (base + x) * 4):
                mask[base + x] = 1

    seen = bytearray(w * h)
    best = (0, 0, 0, 0, 0)
    for y in range(h):
        base = y * w
        for x in range(w):
            start = base + x
            if not mask[start] or seen[start]:
                continue
            stack = [start]
            seen[start] = 1
            count = 0
            minx, miny, maxx, maxy = x, y, x, y
            while stack:
                i = stack.pop()
                count += 1
                cy, cx = divmod(i, w)
                if cx < minx:
                    minx = cx
                if cx > maxx:
                    maxx = cx
                if cy < miny:
                    miny = cy
                if cy > maxy:
                    maxy = cy
                if cx + 1 < w and mask[i + 1] and not seen[i + 1]:
                    seen[i + 1] = 1
                    stack.append(i + 1)
                if cx > 0 and mask[i - 1] and not seen[i - 1]:
                    seen[i - 1] = 1
                    stack.append(i - 1)
                if i + w < w * h and mask[i + w] and not seen[i + w]:
                    seen[i + w] = 1
                    stack.append(i + w)
                if i >= w and mask[i - w] and not seen[i - w]:
                    seen[i - w] = 1
                    stack.append(i - w)
            if count > best[0]:
                best = (count, minx, miny, maxx + 1, maxy + 1)
    _, x0, y0, x1, y1 = best
    return x0, y0, x1, y1


def build_emblem(src, with_wordmark):
    """Square, white-backed emblem bitmap: the cross (+ optional wordmark)."""
    cross = largest_green_blob(src)
    cw, ch = cross[2] - cross[0], cross[3] - cross[1]
    pad = round(cw * 0.06)

    if not with_wordmark:
        side = max(cw, ch) + pad * 2
        emblem = Bitmap(side, side, WHITE)
        glyph = downsample(src, cross, cw, ch)
        blit(emblem, glyph, (side - cw) // 2, (side - ch) // 2)
        return emblem

    # Stack the cross above the red wordmark, both scaled to the cross width.
    word = bbox(src, is_red, (0, cross[3], src.w, src.h))
    ww, wh = word[2] - word[0], word[3] - word[1]
    name_w = cw
    name_h = max(1, round(wh * name_w / ww))
    gap = round(cw * 0.08)
    total = ch + gap + name_h
    side = max(cw, total) + pad * 2
    emblem = Bitmap(side, side, WHITE)
    glyph = downsample(src, cross, cw, ch)
    name = downsample(src, word, name_w, name_h)
    top = max(0, (side - total) // 2)
    blit(emblem, glyph, (side - cw) // 2, top)
    blit(emblem, name, (side - name_w) // 2, top + ch + gap)
    return emblem


def place(emblem, size, fill):
    """Scale `emblem` to `fill` * size and centre it on a size x size bitmap."""
    glyph = max(1, round(size * fill))
    scaled = downsample(emblem, (0, 0, emblem.w, emblem.h), glyph, glyph)
    tile = Bitmap(size, size, WHITE)
    off = (size - glyph) // 2
    blit(tile, scaled, off, off)
    return tile


def content_box(bmp):
    """Bounding box of the non-white (drawn) pixels of a white-backed bitmap."""
    px = bmp.px
    minx, miny, maxx, maxy = bmp.w, bmp.h, -1, -1
    for y in range(bmp.h):
        base = y * bmp.w
        for x in range(bmp.w):
            i = (base + x) * 4
            if min(px[i], px[i + 1], px[i + 2]) < 240:
                if x < minx:
                    minx = x
                if x > maxx:
                    maxx = x
                if y < miny:
                    miny = y
                if y > maxy:
                    maxy = y
    return minx, miny, maxx + 1, maxy + 1


def round_fill(emblem, margin=0.88):
    """Largest fill that keeps the emblem's content inside a round tile."""
    x0, y0, x1, y1 = content_box(emblem)
    rw = (x1 - x0) / emblem.w
    rh = (y1 - y0) / emblem.h
    return margin / (rw * rw + rh * rh) ** 0.5


def main():
    ap = argparse.ArgumentParser(description=__doc__)
    ap.add_argument('--cross-only', action='store_true',
                    help='drop the red PHARMACY wordmark and keep just the cross')
    ap.add_argument('--source', default=SOURCE, help=f'brand image (default {SOURCE})')
    args = ap.parse_args()

    with_wordmark = not args.cross_only
    src = read_png(args.source)
    emblem = build_emblem(src, with_wordmark)
    print(f'source {os.path.basename(args.source)} {src.w}x{src.h} '
          f'-> emblem {emblem.w}x{emblem.h}'
          f'{" + wordmark" if with_wordmark else ""}')

    drawable = os.path.join(RES, 'drawable')
    os.makedirs(drawable, exist_ok=True)

    # Adaptive icon: white backdrop + emblem inside the visible safe area.
    write_png(Bitmap(ADAPTIVE, ADAPTIVE, WHITE),
              os.path.join(drawable, 'ic_launcher_background.png'))
    fit = round_fill(emblem, FG_MARGIN)
    visible = round(ADAPTIVE * VISIBLE_FRACTION * fit)
    glyph = downsample(emblem, (0, 0, emblem.w, emblem.h), visible, visible)
    fg = blank(ADAPTIVE, ADAPTIVE)
    off = (ADAPTIVE - visible) // 2
    blit(fg, key_white(glyph), off, off)
    write_png(fg, os.path.join(drawable, 'ic_launcher_foreground.png'))

    tile_fill = NAME_FILL if with_wordmark else TILE_FILL
    rnd_fill = round_fill(emblem)

    for dpi, size in DENSITIES.items():
        folder = os.path.join(RES, f'mipmap-{dpi}')
        os.makedirs(folder, exist_ok=True)

        square = place(emblem, size, tile_fill)
        write_png(square, os.path.join(folder, 'ic_launcher.png'), alpha=False)

        rnd = clip_mask(place(emblem, size, rnd_fill), 'circle')
        write_png(rnd, os.path.join(folder, 'ic_launcher_round.png'))

        for stale in ('ic_launcher.webp', 'ic_launcher_round.webp'):
            path = os.path.join(folder, stale)
            if os.path.exists(path):
                os.remove(path)
        print(f'mipmap-{dpi}: ic_launcher.png + ic_launcher_round.png ({size}px)')

    print(f'adaptive foreground: {visible}px emblem on {ADAPTIVE}px canvas')
    print('Launcher icons regenerated.')


if __name__ == '__main__':
    main()
