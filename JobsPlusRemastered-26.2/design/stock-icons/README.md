# Stock icons

The game loads the bundled 96 × 96 transparent PNG sprites from
`common/src/main/resources/assets/jobsplus/textures/gui/sprites/stocks/`.
`mapping.json` maps the existing stock catalog IDs to the retained SVG sources.
There are no runtime image downloads.

Sources and corresponding license copies:

- Simple Icons: https://github.com/simple-icons/simple-icons — `LICENSE-simpleicons.txt`.
- Microsoft, TSMC and SK Hynix: https://github.com/gilbarbara/logos — `LICENSE-logos.txt`.
- Multicolor Google: https://github.com/devicons/devicon — `LICENSE-devicon.txt`.
- Amazon: https://github.com/FortAwesome/Font-Awesome — `LICENSE-fontawesome.txt`.
- Disney: https://github.com/tabler/tabler-icons — `LICENSE-tabler.txt`.

`hanwha.svg` is a simplified group-inspired symbol and `udmtek.svg` is a custom
UDM monogram, rather than official logo reproductions. The three Samsung
catalog entries share the Samsung group wordmark. Brand names and marks remain
the property of their respective owners.

The SVG artwork was fitted into a square with transparent padding and exported
with Inkscape. PNGs, rather than the SVG design sources, are packaged for the game.

UI text and numbers remain native, dynamic Minecraft rendering. The layout is
adapted to the available GUI space; an in-game screenshot comparison is still
required to verify visual fidelity to the reference mockups.
