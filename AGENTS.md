# AGENTS

Forge 1.20.1. Keep this mod small.

- Only three-body dehydration flow.
- States: `normal`, `dehydrated`, `rehydrating`.
- Server owns state, recovery, damage immunity.
- Client only HUD, FX, SFX, sync display.
- No second thirst bar.
- Entering `dehydrated` clears LSO thirst and caps max health at 5 hearts.
- Do not show resistance boost or LSO temperature immunity while dehydrated.
- While dehydrated, suppress LSO heat/freeze screen overlays and temperature HUD shake.
- While dehydrated, replace the LSO temperature indicator above the XP bar with a dehydration icon.
- Block temp/thirst attacks early enough to avoid hurt shake.
- Do not replace LSO temp/thirst/body damage.
- Only block temp/thirst damage, never all damage.
- Keep API stable: `isDehydrated`, `getState`, `isFirstDehydration`, `isFirstRehydration`.
- Persist via capability/NBT. Login, dimension change, sleep must keep state.

Verify: run `build`, then `runClient`.
