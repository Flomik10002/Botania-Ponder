# Changelog

## Unreleased

## 1.2.0 — 2026-08-13

- Completed the 135-scene block/device milestone with the final sixteen scenes: Force Relay,
  Tiny Potato, Incense Plate, Cacophonium, Teru Teru Bozu, Avatar, Animated Torch, Cocoon,
  Fel Pumpkin, Starfield Creator, Spectral Rail, and all five Luminizer behaviors.
- Completed the planned advanced Mana Device chapter with focused scenes for the Redstone, Elven,
  and Gaia Spreaders and the Mana, Natura, and Gaia Pylons.
- Added a maintained scene coverage audit with category totals, a 22-scene block/device backlog,
  and separately tracked future item and equipment chapters.

- Added seven focused automation scenes for the Crafty Crate, Mana Pump, Hovering Hourglass,
  Manastorm Charge, and all three platform variants.
- Kept the camera fixed after scene initialization in Advanced Crafting, Utility, and Automation
  scenes, removing the visible vertical jump when the Alchemy Catalyst's Mana Pool appears.
- Added the required Platform block-entity data to generated schematics so Forge can resolve the
  native Abstruse, Spectral, and Infrangible Platform models instead of the missing-model fallback.
- Pinned all three platform scenes to one camera focus across every reveal. Their native Forge
  models remain blocked by PonderLib 0.7.3 baking captured sections with empty model data.

- Updated to PonderLib 0.7.3, including the vanilla private-access rework and resolved captured
  fire/Mana Flame section rendering issues.
- Added the complete core Corporea request chapter: Index, Funnel, Crystal Cube, Interceptor and
  Retainer, with focused request, counting, failure-detection and replay demonstrations.
- Fixed the text-overlap audit so scenes with the same Java method name in different scene classes
  are checked independently.
- Added the complete Drum family: separate focused scenes for the Drum of the Wild, Drum of the
  Canopy, and the Gathering Drum's shearing, milking, and egg-timer mechanics.
- Added an Advanced Mana Crafting chapter for the Alchemy Catalyst, Conjuration Catalyst,
  Terrestrial Agglomeration Plate, Mana Enchanter, and Alfheim Portal.
- Added eight focused utility scenes for the Ender Overseer, Eye of the Ancients, Mana Fluxfield,
  Life Imbuer, Mana Prism, Spark Tinkerer, Bellows, and Tiny Planet.

## 1.1.1 — 2026-08-10

- Updated to PonderLib 0.7.2 (idle() timeline-blocking fix, hold-to-ponder localisation, scene
  info plaque, and a production-Forge SchematicLoader crash fix).
- Fixed text windows overlapping the next instruction across the Functional Flora, Generating
  Flora, Mana Spreader, Mana Device, Endoflame, Pure Daisy, Petal Apothecary, Runic Altar,
  Botanical Brewery, and Red String scenes: PonderLib fades text in/out over 5 ticks beyond its
  declared duration, so `idle()` calls right before the next text window needed a matching margin.

## 1.1.0 — 2026-08-09

- Completed the standard Functional Flora chapter with focused scenes for Bergamute, Bubbell,
  Heisei Dream, Hyacidus, Labellia, Loonium, Marimorphosis, Medumone, Orechid Ignem,
  Rannuncarpus, Solegnolia, Spectranthemum, Tigerseye, and Vinculotus.
- Stabilized camera focus throughout those scenes and corrected hostile-mob staging so entities
  stay grounded, avoid daylight burning, and never path through the flower itself.
- Added a complete Mana Lenses chapter with one focused scene for all 25 standard lenses, including
  burst-property, trajectory, impact, control, and endgame effects.
- Added the complete Red String chapter: Container, Dispenser, Nutrifier, Comparator, Spoofer, and
  Interceptor, each demonstrating its own remote-link behavior.
- Updated to PonderLib 0.7.0.
- Added scene-level category tags for the new animated sidebar highlighting and category return
  navigation.
- Replaced the manual per-tick Mana Pool item animation with PonderLib's native eased,
  collision-free entity movement.
- Added complete single-function scenes for Dreadthorn, Pollidisiac, Fallen Kanade, Tangleberrie,
  and Jiyuulia.

## 1.0.0 — 2026-08-09

Initial public release for Forge 1.20.1.

- Added 33 focused Ponder scenes for core Botania mechanics, generating flowers, functional
  flowers, Mana devices, and crafting blocks.
- Added animated Mana Pool filling, Mana Spreader transfer, Pure Daisy conversion, Runic Altar
  crafting, Petal Apothecary crafting, and Botanical Brewery brewing.
- Added Botania-styled, muted UI colours.
- Updated the project to PonderLib 0.5.0.
- Fixed fluid-source visibility, block destruction, block-entity state updates, scene reveal
  direction, overlapping text, and several animation timing issues.
- Switched ordinary items and mobs to standard entity physics. The Mana Pool infusion drop keeps a
  targeted collision-free animation so the item reaches the inside of the pool.
- Fixed mob placement, facing, Shulk Me Not projectile cleanup, and Exoflame furnace orientation.
