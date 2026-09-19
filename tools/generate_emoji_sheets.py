from pathlib import Path
from PIL import Image, ImageDraw, ImageFont

ROOT = Path("app/src/main/res/drawable-nodpi")
ROOT.mkdir(parents=True, exist_ok=True)

STD = ["😀","😃","😄","😁","😆","😅","😂","🤣","😊","😇","🙂","🙃","😉","😌","😍","🥰","😘","😗","😙","😚","😋","😛","😝","😜","🤪","🤨","🧐","🤓","😎","🤩","🥳","😏","😒","😞","😔","😟","😕","🙁","☹️","😣","😖","😫","😩","🥺","😢","😭","😤","😡","😠","🤬","🤯","😳","🥶","🥵","😱","😨","😰","😥","😓","🤗","🤔","🤭","🤫","🤥","😶","😐","😑","😬","🙄","😯","😦","😧","😮","😲","🥱","😴","🤤","😪","😵","🤐","❤️","🧡","💛","💚","💙","💜","🖤","🤍","🔥","⭐","✨","🎮","🤖","👻","🍊","🎉"];

SPECIAL = ["👽","🍎","🥑","🎈","🍌","🏀","🏖️","🐝","🔔","🚲","🐦","🎂","📖","🏹","🍞","🍔","🍰","📷","🍬","🚗","🐱","🍒","🐔","☁️","☕","☄️","🍪","👑","💎","🐶","🍩","🐉","🌍","🥚","🎆","🌸","🏈","🎮","👻","🎁","🎸","🎧","🍦","🏝️","🔑","🥝","🍋","⚡","🔒","✨","🍈","🎤","🌙","🍄","🎵","🐙","🍊","🐼","🎉","🍑","🍐","🐧","🍕","🪐","🍿","🌈","🚀","🌹","🥪","🛰️","🦈","🛡️","⚽","✨","☀️","😎","⚔️","🌮","🧸","🌩️","🎟️","🏆","🌷","🦄","🍉","🐋","🧙","🐺"];

FONT = ImageFont.truetype("/usr/share/fonts/truetype/noto/NotoColorEmoji.ttf", 109)

def make_sheet(items, columns, name):
    cell = 32
    rows = (len(items) + columns - 1) // columns
    out = Image.new("RGBA", (columns * cell, rows * cell), (0, 0, 0, 0))
    for i, emoji in enumerate(items):
        x, y = (i % columns) * cell, (i // columns) * cell
        ImageDraw.Draw(out).rounded_rectangle(
            (x + 2, y + 2, x + cell - 2, y + cell - 2),
            radius=8,
            fill=(25 + (i * 37) % 100, 25 + (i * 61) % 100, 32 + (i * 83) % 100, 255),
        )
        tile = Image.new("RGBA", (128, 128), (0, 0, 0, 0))
        ImageDraw.Draw(tile).text((64, 64), emoji, font=FONT, anchor="mm", embedded_color=True)
        tile.thumbnail((27, 27), Image.Resampling.LANCZOS)
        out.alpha_composite(tile, (x + 3 + (27 - tile.width) // 2, y + 3 + (27 - tile.height) // 2))
    out.save(ROOT / name, optimize=True)

make_sheet(STD, 12, "emoji_skins.png")
make_sheet(SPECIAL, 11, "special_emojis.png")
