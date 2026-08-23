# EnthusiaDonorNPCs — SMP Player Guide

This file documents the leaderboard NPCs that players can encounter on Enthusia SMP. The main [`README.md`](README.md) remains the setup/provider reference.

The production values below were checked against the live server configuration on August 22, 2026.

## What the NPCs represent

EnthusiaDonorNPCs turns live PlaceholderAPI leaderboard positions into NPC skins. When the player occupying a configured leaderboard position changes, the corresponding NPC is updated to use that player's Minecraft skin.

The current SMP has NPC slots for:

| Leaderboard | NPC positions represented |
| --- | ---: |
| **All-Time Donors** | #1, #2, #3 |
| **Monthly Donors** | #1, #2, #3 |
| **Top Balance** | #1 |
| **Top Active Playtime** | #1 |
| **Top Kills** | #1 |

These NPCs are displays, not separate rankings. The underlying leaderboard data comes from the systems that own each statistic:

- EnthusiaDonors → all-time/monthly donor totals and kills;
- EnthusiaCurrency → balance;
- EnthusiaPlaytime → all-time active playtime.

## Update timing

The NPC updater runs every **10 minutes** in the current production configuration.

It is configured to update a slot only when the resolved leaderboard player changes, rather than repeatedly reapplying the same skin on every cycle. After a skin change, the NPC is refreshed so viewers see the new appearance.

If a configured leaderboard position does not resolve to a valid player, the plugin can fall back to the configured default `Steve` skin rather than inventing a donor/stat value.

## Names and holograms

The current configuration has direct NPC display names disabled. In other words, the NPC's own nametag is not automatically set to the leaderboard player's name by this plugin.

Text shown around an NPC can instead come from the server's associated hologram/display configuration and PlaceholderAPI. This keeps the visual NPC skin and leaderboard text separate.

## No player command is required

Players do not need a command to use these displays. `/enthusiadonornpcs ...` is an operator/admin management command for refreshing/reloading the NPC integration.

## What belongs on the public wiki

Useful wiki information is simply that Enthusia has in-world leaderboard NPCs for:

- top three all-time supporters;
- top three monthly supporters;
- richest player;
- highest active playtime;
- highest kills;

and that the NPC skin represents the current player in that position. The provider IDs, PlaceholderAPI wiring, skin-refresh internals, and staff commands do not need a normal player wiki page.
