# Stock icons

The game loads bundled PNG sprites from
`common/src/main/resources/assets/jobsplus/textures/gui/sprites/stocks/`.
Most sprites are 96 × 96 with transparent padding. The two uploaded Samsung
company marks below retain their original PNG dimensions and white backgrounds.
`mapping.json` maps stock catalog IDs to source basenames: SVG for the existing
vector artwork and PNG for these two uploaded marks. There are no runtime image downloads.

Sources and corresponding license copies:

- Simple Icons: https://github.com/simple-icons/simple-icons — `LICENSE-simpleicons.txt`.
- Microsoft, TSMC and SK Hynix: https://github.com/gilbarbara/logos — `LICENSE-logos.txt`.
- Multicolor Google: https://github.com/devicons/devicon — `LICENSE-devicon.txt`.
- Amazon: https://github.com/FortAwesome/Font-Awesome — `LICENSE-fontawesome.txt`.
- Disney: https://github.com/tabler/tabler-icons — `LICENSE-tabler.txt`.

`hanwha.svg` is a simplified group-inspired symbol and `udmtek.svg` is a custom
UDM monogram, rather than official logo reproductions. Samsung Electronics (005930)
retains the Samsung wordmark. Samsung SDI (006400) and Samsung Electro-Mechanics
(009150) now use the user-uploaded Korean company-name marks:

| Stock ID | Retained source | Source dimensions | Packaged sprite |
|---|---|---|---|
| `009150` | [삼성전기.png](삼성전기.png) | 576 × 347 | `stocks/009150.png` |
| `006400` | [삼성sdi.png](삼성sdi.png) | 513 × 407 | `stocks/006400.png` |

Each packaged PNG is an unchanged copy of its uploaded source, preserving the
logo artwork, company lettering, colors and background. `StockIcons.draw`
centers these rectangular images vertically within the existing square icon
slots and derives the displayed height from the original aspect ratio.
Other stock icons keep their existing square rendering.

The previous `samsung-sdi.svg` and `samsung-electro-mechanics.svg` files are
retained for provenance; they are no longer the mapped sources for these IDs.
Their original references are:

- Samsung SDI CI: https://www.samsungsdi.com/about-sdi/ci.html
  SVG: https://www.samsungsdi.com/resources/images/about_sdi/ci_img_09.svg
- Samsung Electro-Mechanics: https://www.samsungsem.com/global/index.do
  SVG: https://www.samsungsem.com/resources/images/global/common/logo_oval.svg

Brand names and marks remain the property of their respective owners.

The remaining SVG artwork was fitted into a square with transparent padding and
exported with Inkscape. The two uploaded PNGs do not go through this SVG export.
Only the PNG sprites under the resources directory are packaged for the game.

UI text and numbers remain native, dynamic Minecraft rendering. The layout is
adapted to the available GUI space; an in-game screenshot comparison is still
required to verify visual fidelity to the reference mockups.
