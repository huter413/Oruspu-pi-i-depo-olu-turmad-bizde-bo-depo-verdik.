from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path("app/src/main/res/drawable-nodpi")
ROOT.mkdir(parents=True, exist_ok=True)

STD = ["😀","😃","😄","😁","😆","😅","😂","🤣","😊","😇","🙂","🙃","😉","😌","😍","🥰","😘","😗","😙","😚","😋","😛","😝","😜","🤪","🤨","🧐","🤓","😎","🤩","🥳","😏","😒","😞","😔","😟","😕","🙁","☹️","😣","😖","😫","😩","🥺","😢","😭","😤","😡","😠","🤬","🤯","😳","🥶","🥵","😱","😨","😰","😥","😓","🤗","🤔","🤭","🤫","🤥","😶","😐","😑","😬","🙄","😯","😦","😧","😮","😲","🥱","😴","🤤","😪","😵","🤐","❤️","🧡","💛","💚","💙","💜","🖤","🤍","🔥","⭐","✨","🎮","🤖","👻","🍊","🎉"];

SPECIAL = ["👽","🍎","🥑","🎈","🍌","🏀","🏖️","🐝","🔔","🚲","🐦","🎂","📖","🏹","🍞","🍔","🍰","📷","🍬","🚗","🐱","🍒","🐔","☁️","☕","☄️","🍪","👑","💎","🐶","🍩","🐉","🌍","🥚","🎆","🌸","🏈","🎮","👻","🎁","🎸","🎧","🍦","🏝️","🔑","🥝","🍋","⚡","🔒","✨","🍈","🎤","🌙","🍄","🎵","🐙","🍊","🐼","🎉","🍑","🍐","🐧","🍕","🪐","🍿","🌈","🚀","🌹","🥪","🛰️","🦈","🛡️","⚽","✨","☀️","😎","⚔️","🌮","🧸","🌩️","🎟️","🏆","🌷","🦄","🍉","🐋","🧙","🐺"];

FONT = ImageFont.truetype("/usr/share/fonts/truetype/noto/NotoColorEmoji.ttf", 109)

def make_one(emoji, path, index):
    cell = 32
    out = Image.new("RGBA", (cell, cell), (0, 0, 0, 0))
    ImageDraw.Draw(out).rounded_rectangle(
        (2, 2, cell - 2, cell - 2),
        radius=8,
        fill=(25 + (index * 37) % 100, 25 + (index * 61) % 100, 32 + (index * 83) % 100, 255),
    )
    tile = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
    ImageDraw.Draw(tile).text((64, 64), emoji, font=FONT, anchor="mm", embedded_color=True)
    tile.thumbnail((27, 27), Image.Resampling.LANCZOS)
    out.alpha_composite(tile, ((cell - tile.width) // 2, (cell - tile.height) // 2))
    out.save(ROOT / path, optimize=True)

for i, emoji in enumerate(STD):
    make_one(emoji, f"skin_{i:03d}.png", i)

for i, emoji in enumerate(SPECIAL):
    make_one(emoji, f"special_{i:03d}.png", i)
