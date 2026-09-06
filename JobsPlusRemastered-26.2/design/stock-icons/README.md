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
UDM monogram, rather than official logo reproductions. Samsung Electronics (005930)
retains the Samsung wordmark. Samsung SDI (006400) and Samsung Electro-Mechanics
(009150) use their respective company-name combination marks from official sites:

- Samsung SDI CI: https://www.samsungsdi.com/about-sdi/ci.html
  SVG: https://www.samsungsdi.com/resources/images/about_sdi/ci_img_09.svg
- Samsung Electro-Mechanics: https://www.samsungsem.com/global/index.do
  SVG: https://www.samsungsem.com/resources/images/global/common/logo_oval.svg

The two combination marks retain their source geometry and colors, with a light
backing added at export for contrast against the dark UI. Brand names and marks
remain the property of their respective owners.

The SVG artwork was fitted into a square with transparent padding and exported
with Inkscape. PNGs, rather than the SVG design sources, are packaged for the game.

UI text and numbers remain native, dynamic Minecraft rendering. The layout is
adapted to the available GUI space; an in-game screenshot comparison is still
required to verify visual fidelity to the reference mockups.
