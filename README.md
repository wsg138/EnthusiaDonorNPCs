# EnthusiaDonorNPCs

Paper plugin that updates donor leaderboard NPC skins from PlaceholderAPI values.

## Requirements

- Paper 26.2 or compatible server running Java 25+
- PlaceholderAPI
- One NPC provider:
  - Citizens, or
  - FancyNpcs 2.9.2+

## Configuration

Choose the NPC provider in `config.yml`:

```yml
npc-provider: "citizens"
```

Use `"fancynpcs"` to update FancyNpcs instead. With Citizens, each `npc-id` must be its numeric Citizens ID. With FancyNpcs, `npc-id` can be a numeric ID, NPC name, or internal NPC ID.

## FancyHolograms

Install FancyHolograms and create a text hologram for each donor NPC. Set the hologram's name with `hologram-name` under that position; the included configuration uses names such as `donorboard_1_text`. The plugin links each configured hologram to its FancyNpc, so FancyHolograms renders and positions the text using display entities. It does not overwrite hologram lines, formatting, or PlaceholderAPI settings.

Each leaderboard position supports `name-placeholder`, `uuid-placeholder`, and an optional cardinal `facing` direction. UUID placeholders are preferred when available because they avoid username-history ambiguity.

## Commands

- `/enthusiadonornpcs reload` — reload configuration and reconcile NPCs
- `/enthusiadonornpcs update` — force a skin update
- `/enthusiadonornpcs status` — view the current update status

All commands require `enthusiadonornpcs.admin` (default: operators).

## Building

Build with JDK 25 or newer:

```bash
mvn package
```

The plugin jar is written to `target/`.
