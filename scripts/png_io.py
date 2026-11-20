"""Minimal dependency-free PNG read/write + image helpers (stdlib only).

Used by `make_launcher_icons.py` so the launcher icons can be regenerated
without Pillow / ImageMagick / sips.
"""
import struct
import zlib

WHITE = (255, 255, 255, 255)


class Bitmap:
    """RGBA bitmap stored as a flat bytearray (w*h*4)."""

    def __init__(self, w, h, fill=WHITE):
        self.w = w
        self.h = h
        self.px = bytearray(bytes(fill) * (w * h))

    def get(self, x, y):
        i = (y * self.w + x) * 4
        return tuple(self.px[i:i + 4])

    def put(self, x, y, c):
        i = (y * self.w + x) * 4
        self.px[i:i + 4] = bytes(c)


def read_png(path):
    """Decode an 8-bit non-interlaced PNG (gray, RGB, palette or RGBA)."""
    data = open(path, 'rb').read()
    assert data[:8] == b'\x89PNG\r\n\x1a\n', 'not a png'
    pos = 8
    idat = bytearray()
    palette = trns = None
    while pos < len(data):
        (length,) = struct.unpack('>I', data[pos:pos + 4])
        ctype = data[pos + 4:pos + 8]
        chunk = data[pos + 8:pos + 8 + length]
        pos += 12 + length
        if ctype == b'IHDR':
            w, h, bitdepth, colortype, _comp, _filt, interlace = struct.unpack(
                '>IIBBBBB', chunk)
            assert bitdepth == 8, f'bit depth {bitdepth} unsupported'
            assert interlace == 0, 'interlaced png unsupported'
        elif ctype == b'PLTE':
            palette = chunk
        elif ctype == b'tRNS':
            trns = chunk
        elif ctype == b'IDAT':
            idat += chunk
        elif ctype == b'IEND':
            break

    channels = {0: 1, 2: 3, 3: 1, 4: 2, 6: 4}[colortype]
    raw = zlib.decompress(bytes(idat))
    stride = w * channels
    out = Bitmap(w, h)
    prev = bytearray(stride)
    p = 0
    for y in range(h):
        ft = raw[p]
        p += 1
        line = bytearray(raw[p:p + stride])
        p += stride
        if ft == 1:
            for i in range(channels, stride):
                line[i] = (line[i] + line[i - channels]) & 0xFF
        elif ft == 2:
            for i in range(stride):
                line[i] = (line[i] + prev[i]) & 0xFF
        elif ft == 3:
            for i in range(stride):
                a = line[i - channels] if i >= channels else 0
                line[i] = (line[i] + ((a + prev[i]) >> 1)) & 0xFF
        elif ft == 4:
            for i in range(stride):
                a = line[i - channels] if i >= channels else 0
                b = prev[i]
                c = prev[i - channels] if i >= channels else 0
                pa, pb, pc = abs(b - c), abs(a - c), abs(a + b - 2 * c)
                pr = a if (pa <= pb and pa <= pc) else (b if pb <= pc else c)
                line[i] = (line[i] + pr) & 0xFF
        elif ft != 0:
            raise ValueError(f'bad filter type {ft}')
        prev = line

        base = y * w * 4
        px = out.px
        if colortype == 2:
            for x in range(w):
                sx = x * 3
                dx = base + x * 4
                px[dx] = line[sx]
                px[dx + 1] = line[sx + 1]
                px[dx + 2] = line[sx + 2]
                px[dx + 3] = 255
        elif colortype == 6:
            px[base:base + stride] = line
        elif colortype == 0:
            for x in range(w):
                v = line[x]
                dx = base + x * 4
                px[dx] = px[dx + 1] = px[dx + 2] = v
                px[dx + 3] = 255
        elif colortype == 4:
            for x in range(w):
                v = line[x * 2]
                dx = base + x * 4
                px[dx] = px[dx + 1] = px[dx + 2] = v
                px[dx + 3] = line[x * 2 + 1]
        elif colortype == 3:
            for x in range(w):
                idx = line[x]
                dx = base + x * 4
                px[dx:dx + 3] = palette[idx * 3:idx * 3 + 3]
                px[dx + 3] = trns[idx] if trns and idx < len(trns) else 255
    return out


def _chunk(ctype, data):
    return (struct.pack('>I', len(data)) + ctype + data
            + struct.pack('>I', zlib.crc32(ctype + data) & 0xFFFFFFFF))


def write_png(bitmap, path, alpha=True):
    w, h = bitmap.w, bitmap.h
    raw = bytearray()
    if alpha:
        for y in range(h):
            raw.append(0)
            raw += bitmap.px[y * w * 4:(y + 1) * w * 4]
        colortype = 6
    else:
        for y in range(h):
            raw.append(0)
            base = y * w * 4
            for x in range(w):
                o = base + x * 4
                raw += bitmap.px[o:o + 3]
        colortype = 2
    ihdr = struct.pack('>IIBBBBB', w, h, 8, colortype, 0, 0, 0)
    png = (b'\x89PNG\r\n\x1a\n' + _chunk(b'IHDR', ihdr)
           + _chunk(b'IDAT', zlib.compress(bytes(raw), 9))
           + _chunk(b'IEND', b''))
    with open(path, 'wb') as f:
        f.write(png)


def downsample(src, box, dw, dh, bg=WHITE):
    """Area-average `box` (x0, y0, x1, y1) of `src` into a dw x dh bitmap.

    Only downscaling is supported; the source is composited over `bg`.
    """
    x0, y0, x1, y1 = box
    sw, sh = x1 - x0, y1 - y0
    out = Bitmap(dw, dh, bg)
    px = src.px
    ob = out.px
    bg_r, bg_g, bg_b = bg[0], bg[1], bg[2]
    for dy in range(dh):
        sy0 = y0 + dy * sh // dh
        sy1 = max(sy0 + 1, y0 + (dy + 1) * sh // dh)
        for dx in range(dw):
            sx0 = x0 + dx * sw // dw
            sx1 = max(sx0 + 1, x0 + (dx + 1) * sw // dw)
            r = g = b = a = n = 0
            for yy in range(sy0, sy1):
                base = yy * src.w
                for xx in range(sx0, sx1):
                    i = (base + xx) * 4
                    r += px[i]
                    g += px[i + 1]
                    b += px[i + 2]
                    a += px[i + 3]
                    n += 1
            r //= n
            g //= n
            b //= n
            a //= n
            o = (dy * dw + dx) * 4
            if a == 255:
                ob[o] = r
                ob[o + 1] = g
                ob[o + 2] = b
            else:
                inv = 255 - a
                ob[o] = (r * a + bg_r * inv) // 255
                ob[o + 1] = (g * a + bg_g * inv) // 255
                ob[o + 2] = (b * a + bg_b * inv) // 255
            ob[o + 3] = 255
    return out


def blank(w, h):
    """Fully transparent w x h bitmap."""
    return Bitmap(w, h, (0, 0, 0, 0))


def blit(dst, src, x, y):
    """Alpha-blend `src` onto `dst` at (x, y)."""
    dp, sp = dst.px, src.px
    for sy in range(src.h):
        dy = y + sy
        if not (0 <= dy < dst.h):
            continue
        for sx in range(src.w):
            dx = x + sx
            if not (0 <= dx < dst.w):
                continue
            si = (sy * src.w + sx) * 4
            a = sp[si + 3]
            if a == 0:
                continue
            di = (dy * dst.w + dx) * 4
            if a == 255:
                dp[di:di + 4] = sp[si:si + 4]
                continue
            inv = 255 - a
            dp[di] = (sp[si] * a + dp[di] * inv) // 255
            dp[di + 1] = (sp[si + 1] * a + dp[di + 1] * inv) // 255
            dp[di + 2] = (sp[si + 2] * a + dp[di + 2] * inv) // 255
            dp[di + 3] = max(dp[di + 3], a)


def key_white(bmp):
    """Turn a white-backed bitmap into a transparent one (white -> alpha 0).

    Alpha comes from how far a pixel is from white; the remaining colour is
    un-premultiplied so antialiased edges stay clean against any background.
    """
    out = Bitmap(bmp.w, bmp.h, (0, 0, 0, 0))
    sp, dp = bmp.px, out.px
    for i in range(0, len(sp), 4):
        r, g, b = sp[i], sp[i + 1], sp[i + 2]
        alpha = 255 - min(r, g, b)
        if alpha <= 3:
            continue
        dp[i] = max(0, min(255, 255 - (255 - r) * 255 // alpha))
        dp[i + 1] = max(0, min(255, 255 - (255 - g) * 255 // alpha))
        dp[i + 2] = max(0, min(255, 255 - (255 - b) * 255 // alpha))
        dp[i + 3] = alpha
    return out


def clip_mask(bmp, shape, inset=0):
    """Return a copy of `bmp` clipped to a circle ('circle') or rounded square.

    Uses 4x4 supersampling for smooth edges.
    """
    w, h = bmp.w, bmp.h
    out = Bitmap(w, h, (0, 0, 0, 0))
    out.px[:] = bmp.px
    radius = w // 5  # squircle-ish corner radius
    lo, hi = inset, w - 1 - inset
    for y in range(h):
        for x in range(w):
            cov = 0
            for sy in range(4):
                for sx in range(4):
                    px_ = x + (sx + 0.5) / 4
                    py_ = y + (sy + 0.5) / 4
                    if not (lo <= px_ <= hi and lo <= py_ <= hi):
                        continue
                    if shape == 'circle':
                        cx = cy = w / 2
                        if (px_ - cx) ** 2 + (py_ - cy) ** 2 <= (w / 2 - inset) ** 2:
                            cov += 1
                    else:
                        r = radius
                        cx = min(max(px_, lo + r), hi - r)
                        cy = min(max(py_, lo + r), hi - r)
                        if (px_ - cx) ** 2 + (py_ - cy) ** 2 <= r * r:
                            cov += 1
            if cov != 16:
                i = (y * w + x) * 4
                out.px[i + 3] = out.px[i + 3] * cov // 16
    return out
