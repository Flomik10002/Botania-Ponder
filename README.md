# Botania Ponder

Client-side [Ponder](https://github.com/Flomik10002/PonderLib) scenes for
[Botania](https://github.com/VazkiiMods/Botania) on Minecraft Forge 1.20.1.

Botania's own in-game documentation (the Lexica Botania) is text and static pictures. This adds the
Create-style Ponder overlay on top of it: hover a supported block or item and press the Ponder key to
get a short animated scene of what it actually does, built from the real block entities and particle
effects Botania uses in-game, not a mock-up. It's a client-side addon — install it and
[PonderLib](https://github.com/Flomik10002/PonderLib) on your own client and it works joining any
server, with or without Botania installed there, since none of it runs server-side.

## What's in it right now (v1.1.0)

83 scenes, one block or item's behavior each, no scene tries to explain more than one thing:

| Category | Scenes | Coverage |
|---|---|---|
| Generating Flora | 13 | every standard generating flower, Endoflame included |
| Functional Flora | 27 | every standard functional flower |
| Mana Lenses | 25 | the full standard lens set |
| Red String | 6 | Container, Dispenser, Nutrifier, Comparator, Spoofer, Interceptor |
| Mana network | 8 | Mana Pool (filling + infusion), Mana Spreader, Mana Void, Mana Detector, Mana Splitter, Open Crate, Spreader Turntable |
| Crafting | 4 | Pure Daisy, Petal Apothecary, Runic Altar, Botanical Brewery |

Full version-by-version history is in [CHANGELOG.md](CHANGELOG.md).

## What's not in it yet

- **Nobody has watched these scenes run in a real client.** They were all built and verified
  against Botania's actual source — real particle colors, real block-entity state, schematics
  checked byte-for-byte — but the machine this was developed on has no display, so `runClient` has
  never actually launched a window here. Camera framing, beat timing, anything you'd only catch by
  eye is unverified. Treat this release as needing an in-game pass before you'd call it polished.
- Equipment has no coverage at all — wands, rings, bands, the manasteel/elementium/terrasteel tool
  and armor tiers.
- The Corporea network (Index, Spark, Retainer, Funnel) isn't covered.
- Two scenes are deliberately missing rather than shipped broken: Mana Lens: Fire and the
  Endoflame/Exoflame flame effect both hit a PonderLib bug where fire blocks render wrong once
  captured into a scene section. Waiting on an upstream fix rather than working around it with
  something that isn't what the game actually shows.

## Requirements

- Minecraft 1.20.1
- Forge 47.x
- Botania 1.20.1-454 or newer
- PonderLib 0.7.0 or newer

## Installation

Drop Botania, PonderLib, and this mod into your client's `mods` folder. Point at any supported
Botania block or item's tooltip and press the Ponder key.

## Building

```shell
./gradlew build
```

Jar comes out in `build/libs`. `./gradlew runClient` boots a dev client with the mod loaded;
`./gradlew runData` regenerates the Ponder schematics under `src/generated/resources`.

## License

GNU Lesser General Public License 3.0 — see [LICENSE](LICENSE). Not affiliated with Vazkii or the
Botania project.
