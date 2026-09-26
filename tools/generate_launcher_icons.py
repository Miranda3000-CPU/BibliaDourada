#!/usr/bin/env python3
"""
Gera a identidade visual do app "Bíblia Dourada".

Fonte única de verdade da geometria do ícone (espaço 108x108, padrão de
adaptive icon do Android). A partir dela o script produz:

  1. um SVG mestre (renderizado com RSVG via ImageMagick);
  2. os ícones legados em webp para API 24-25 (mdpi..xxxhdpi);
  3. imprime os pathData equivalentes para colar nos VectorDrawable
     (ic_launcher_background.xml / ic_launcher_foreground.xml).

Uso:  python3 tools/generate_launcher_icons.py
"""

import os
import subprocess
import sys
import tempfile

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
RES = os.path.join(ROOT, "app", "src", "main", "res")

# --------------------------------------------------------------------------
# Geometria (espaço 108x108 do adaptive icon)
# --------------------------------------------------------------------------
# Capa do livro (silhueta única, ligeiramente maior que as páginas)
COVER = "M54,46 L24,39 L24,68 Q40,77 54,77 Q68,77 84,68 L84,39 Z"
# Página esquerda
PAGE_LEFT = "M54,44 L27,38 L27,67 Q41,74 54,74 Z"
# Página direita
PAGE_RIGHT = "M54,44 L81,38 L81,67 Q67,74 54,74 Z"
# Vinco central
SPINE = "M54,44 L54,74"

# Brilhos (estrelas de 4 pontas), ecoando o "✦" usado na interface
SPARKLES = [
    (77.0, 29.0, 6.0, "#FFD54F"),
    (32.0, 31.0, 4.0, "#E5C158"),
]

DENSITIES = {
    "mdpi": 48,
    "hdpi": 72,
    "xhdpi": 96,
    "xxhdpi": 144,
    "xxxhdpi": 192,
}


def sparkle_path(cx, cy, r):
    """Estrela de 4 pontas com lados côncavos."""
    k = r * 0.26
    pts = [
        (cx, cy - r), (cx + k, cy - k), (cx + r, cy), (cx + k, cy + k),
        (cx, cy + r), (cx - k, cy + k), (cx - r, cy), (cx - k, cy - k),
    ]
    d = "M" + " L".join(f"{x:.2f},{y:.2f}" for x, y in pts) + " Z"
    return d


def build_svg(shape):
    """shape: 'rounded' (ícone quadrado) ou 'circle' (ícone redondo)."""
    if shape == "circle":
        clip = '<circle cx="54" cy="54" r="54"/>'
    else:
        clip = '<rect x="0" y="0" width="108" height="108" rx="17" ry="17"/>'

    sparkles = "\n    ".join(
        f'<path d="{sparkle_path(cx, cy, r)}" fill="{color}"/>'
        for cx, cy, r, color in SPARKLES
    )

    return f'''<svg xmlns="http://www.w3.org/2000/svg" width="108" height="108"
     viewBox="0 0 108 108">
  <defs>
    <radialGradient id="bg" cx="54" cy="48" r="72" gradientUnits="userSpaceOnUse">
      <stop offset="0%" stop-color="#3A2E16"/>
      <stop offset="55%" stop-color="#1C160E"/>
      <stop offset="100%" stop-color="#0B0906"/>
    </radialGradient>
    <linearGradient id="pageL" x1="27" y1="38" x2="27" y2="74"
        gradientUnits="userSpaceOnUse">
      <stop offset="0%" stop-color="#F5E8C7"/>
      <stop offset="100%" stop-color="#C29B38"/>
    </linearGradient>
    <linearGradient id="pageR" x1="81" y1="38" x2="81" y2="74"
        gradientUnits="userSpaceOnUse">
      <stop offset="0%" stop-color="#FFEFC9"/>
      <stop offset="100%" stop-color="#D4AF37"/>
    </linearGradient>
    <clipPath id="shape">{clip}</clipPath>
  </defs>

  <g clip-path="url(#shape)">
    <rect x="0" y="0" width="108" height="108" fill="url(#bg)"/>
    {sparkles}
    <path d="{COVER}" fill="#8C6A0A"/>
    <path d="{PAGE_LEFT}" fill="url(#pageL)"/>
    <path d="{PAGE_RIGHT}" fill="url(#pageR)"/>
    <path d="{SPINE}" stroke="#7A5C10" stroke-width="1.2" fill="none"
        stroke-linecap="round"/>
  </g>
</svg>
'''


def render(svg_text, out_path, size):
    """Renderiza o SVG com RSVG (via ImageMagick) no tamanho pedido."""
    with tempfile.NamedTemporaryFile("w", suffix=".svg", delete=False) as fh:
        fh.write(svg_text)
        svg_path = fh.name
    try:
        subprocess.run(
            ["magick", "-background", "none", svg_path,
             "-resize", f"{size}x{size}", out_path],
            check=True, capture_output=True,
        )
    finally:
        os.unlink(svg_path)


def main():
    if subprocess.run(["which", "magick"], capture_output=True).returncode != 0:
        sys.exit("ImageMagick (magick) não encontrado.")

    rounded = build_svg("rounded")
    circle = build_svg("circle")

    for density, size in DENSITIES.items():
        folder = os.path.join(RES, f"mipmap-{density}")
        os.makedirs(folder, exist_ok=True)

        square_out = os.path.join(folder, "ic_launcher.webp")
        round_out = os.path.join(folder, "ic_launcher_round.webp")
        render(rounded, square_out, size)
        render(circle, round_out, size)
        print(f"  {density:8s} {size:3d}px  ->  ic_launcher.webp / ic_launcher_round.webp")

    # Expõe os paths para conferência com os VectorDrawable.
    print("\n--- pathData para os VectorDrawable ---")
    print(f"COVER      : {COVER}")
    print(f"PAGE_LEFT  : {PAGE_LEFT}")
    print(f"PAGE_RIGHT : {PAGE_RIGHT}")
    print(f"SPINE      : {SPINE}")
    for cx, cy, r, _ in SPARKLES:
        print(f"SPARKLE    : {sparkle_path(cx, cy, r)}")


if __name__ == "__main__":
    main()
