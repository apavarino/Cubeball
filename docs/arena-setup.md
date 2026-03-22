# Arena Setup

CubeBall detects an arena by scanning blocks around the admin running `/cb match`.

## Marker Blocks

Default markers from `config.yml`:

- Ball spawn: `EMERALD_BLOCK`
- Blue team spawn: `BLUE_WOOL`
- Red team spawn: `RED_WOOL`
- Blue goal: `BLUE_CONCRETE`
- Red goal: `RED_CONCRETE`

## Placement Rules

- Place one ball spawn marker where the center kickoff point should be.
- Place one or more spawn markers for each team.
- Build each goal line using the corresponding goal marker material.
- Stand close enough to the arena before running `/cb match`, within `match.scan-radius`.

## How Positions Are Derived

- Ball spawn is created 3 blocks above the ball marker and centered on the block.
- Team spawns are created 2 blocks above each team marker.
- Goal detection uses the X/Z position of the goal marker blocks.

## Typical Setup Flow

1. Build the field.
2. Place the marker blocks.
3. Run `/cb match`.
4. Confirm all sections report `OK`.
5. Assign teams with `/cb team`.
6. Start the match with `/cb start`.
