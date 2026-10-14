# NOVA Cheats — Minecraft Java 26.2 / Fabric

A client-side item-browser mod prototype for private worlds where the player has permission to use commands.

## Planned features

- Open the NOVA menu with Right Shift.
- Browse registered Minecraft items and search by display name.
- Select an item and request it without enchantments.
- Enchantment selection UI with levels 1–255.
- Later: night vision controls and configurable X-Ray visualization.

## Build

The GitHub Actions workflow runs on pushes to `main` and can be started manually from **Actions → Build NOVA Cheats → Run workflow**.

When a run succeeds, download the `NOVA-Cheats-26.2` artifact.

## Important

This repository is an initial prototype and the Minecraft 26.2 / Fabric dependency coordinates and mapped GUI APIs must be confirmed by a successful Actions build before the JAR is considered usable. A client-only mod cannot bypass server permissions. Item giving and enchantment commands require the appropriate permissions in the world or server.
