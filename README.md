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
