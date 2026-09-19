#!/usr/bin/env python3
"""Build the bundled Musab Round font from the Ubuntu DejaVu Sans base font.

The build environment supplies DejaVu Sans under its system fonts. We subset it
to the keyboard's Latin/Turkish character set and rename the font family so
Android loads it as the app's bundled font.
"""
from pathlib import Path
import subprocess
import sys

src_candidates = [
    Path("/usr/share/fonts/truetype/dejavu/DejaVuSans.ttf"),
    Path("/usr/share/fonts/truetype/dejavu/DejaVuSansCondensed.ttf"),
]
src = next((p for p in src_candidates if p.exists()), None)
if src is None:
    raise SystemExit("DejaVu Sans base font was not found")

out = Path("app/src/main/res/font/musab_round.ttf")
out.parent.mkdir(parents=True, exist_ok=True)

text = (
    "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789"
    "ÇĞİÖŞÜçğıöşü.,!?;:#@+-=*/%()[]{}<>_ ₺€£¥©®™•…→←↑↓"
)
tmp = Path("/tmp/musab_round_subset.ttf")
subprocess.run([
    "pyftsubset", str(src),
    f"--output-file={tmp}",
    f"--text={text}",
    "--layout-features=*",
    "--name-IDs=*",
    "--glyph-names",
    "--symbol-cmap",
], check=True)

from fontTools.ttLib import TTFont

font = TTFont(str(tmp))
names = {
    1: "Musab Round",
    4: "Musab Round Regular",
    6: "MusabRound-Regular",
    16: "Musab Round",
    17: "Regular",
}
for record in font["name"].names:
    if record.nameID in names:
        value = names[record.nameID]
        record.string = value.encode("utf-16-be") if record.isUnicode() else value.encode("latin-1", "replace")
font.save(str(out))
print(f"Generated {out} ({out.stat().st_size} bytes)")
