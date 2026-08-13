# Botania Ponder

**Botania Ponder** adds interactive, Create-style Ponder scenes to [Botania](https://github.com/VazkiiMods/Botania) for Minecraft Forge 1.20.1.

Botania has always had excellent in-game documentation through the Lexica Botania, but learning a mechanic from text and pictures is very different from actually seeing it happen. Botania Ponder adds another way to explore the mod: point at a supported block or item, press the Ponder key, and watch its behavior demonstrated directly in-game.

Instead of explaining an entire system at once, each scene focuses on one specific mechanic. A Mana Pool scene can show how infusion works. A Mana Spreader scene can demonstrate its own behavior. A functional flower can show what it reacts to and what happens when it does. The goal is to make Botania easier to understand without turning Ponder into another wiki or a collection of long tutorials.

The project is built on [PonderLib](https://github.com/Flomik10002/PonderLib), a standalone implementation of the Ponder system designed for mods outside of Create.

## What the scenes are like

The scenes are intended to look and behave like demonstrations of actual Botania mechanics rather than animated illustrations loosely based on them.

Where a mechanic has visible in-game behavior, the scene tries to reproduce that behavior using Botania's real blocks, block entities, states and effects. Mana Pools visibly change their mana state, items involved in interactions actually appear and disappear, and Botania-specific visual effects use the same colors and particles the mod itself uses.

This matters particularly for Botania because many of its systems are easy to misrepresent visually. Mana transfer, flowers, lenses and functional blocks do not always behave the way a generic animation would suggest. Botania Ponder therefore treats the game itself as the reference instead of inventing convenient visual metaphors for mechanics that do not exist.

At the same time, the scenes are deliberately kept small. Pondering a block should answer questions about that block, not launch a five-minute tour through half of Botania. More complicated systems are split into several scenes so individual mechanics can be understood independently.

## Current coverage

Botania Ponder currently contains **135 scenes**.

The current block-and-device milestone contains 135 planned scenes: **all 135 complete**.
Item, equipment, and challenge chapters are tracked separately until their
individual scenes have been scoped.

A large part of Botania's flora is already covered. There are scenes for all standard functional flowers and thirteen generating flowers, including the Endoflame. Their demonstrations focus on the individual behavior of each flower rather than repeating a generic description of the generating and functional flora systems.

The standard Mana Lens set is also extensively documented, with **25 lens scenes** showing what the different lenses do.

The Red String family currently has six scenes covering the Container, Dispenser, Nutrifier, Comparator, Spoofer and Interceptor variants.

The core Corporea request chain has five focused scenes for the Index, Funnel, Crystal Cube,
Interceptor and Retainer.

The complete Drum family is covered by five scenes: vegetation clearing, canopy removal, animal
shearing, bucket milking and chicken egg-timer acceleration.

Advanced Mana crafting includes the Alchemy and Conjuration Catalysts, Terrestrial Agglomeration
Plate, Mana Enchanter, and Alfheim Portal.

Core mana infrastructure is represented as well. Existing scenes cover Mana Pool filling and infusion, the Mana Spreader, Mana Void, Mana Detector, Mana Splitter, Open Crate and Spreader Turntable.

The utility chapter additionally covers the Ender Overseer, Eye of the Ancients, Mana Fluxfield,
Life Imbuer, Mana Prism, Spark Tinkerer, Bellows, and Tiny Planet.

Automation coverage includes the Crafty Crate, Mana Pump, Hovering Hourglass, Manastorm Charge,
and the complete Abstruse, Spectral, and Infrangible Platform family.

Several of Botania's major crafting mechanics already have their own demonstrations, including the **Pure Daisy, Petal Apothecary, Runic Altar and Botanical Brewery**.

The intention is not simply to increase the scene counter. Coverage is added when a mechanic can be represented clearly enough that the scene is actually useful while playing the mod.

For a version-by-version list of additions and changes, see [CHANGELOG.md](CHANGELOG.md).

## What's still missing

Botania is large enough that 135 scenes still leave plenty of territory untouched.

Equipment is currently one of the largest missing areas. Wands, rings, bands, armor and the manasteel, elementium and terrasteel equipment families do not yet have Ponder coverage.

The core Corporea request blocks are covered, while broader network construction, the individual
Spark augment modes and less common Corporea integrations still need their own demonstrations.

Some interactions still need purpose-built scenes, but the previous captured-section rendering
problem for fire and Mana Flame blocks is resolved by PonderLib 0.7.3. Kindle and Flash can now
show their real placed blocks without proxy effects.

More scenes and missing systems will be added as the project develops.

## Client-side only

Botania Ponder is designed as a client-side documentation addon.

It does not add gameplay mechanics, change Botania's balance or require a server to know anything about Ponder scenes. The mod exists purely to show documentation and demonstrations to the player using it.

That also means Botania Ponder can remain separate from the actual gameplay logic it documents. The game continues to behave exactly as Botania defines it; Ponder is simply another way to understand that behavior.

## About PonderLib

Botania Ponder is also one of the projects the standalone [PonderLib](https://github.com/Flomik10002/PonderLib) was built for.

Ponder originally comes from Create, where it is deeply integrated into the mod's own codebase. PonderLib makes the same style of interactive documentation available as an independent system that other mods and addons can build on.

Botania Ponder uses that system to build scenes specifically around Botania while keeping the Ponder engine itself in a separate reusable project.

## License

Botania Ponder is licensed under the [GNU Lesser General Public License 3.0](LICENSE).

This is an independent community project and is not affiliated with or maintained by the Botania project.
