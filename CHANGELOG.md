# Changelog

## Unreleased

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
