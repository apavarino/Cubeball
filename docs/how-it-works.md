# How It Works

CubeBall turns a configurable falling block into a football-like entity. Players move into the ball to kick it, and the plugin keeps the entity alive by recreating it when the server tries to place the block.

## Match Lifecycle

1. An admin marks the arena with specific blocks for the ball spawn, team spawns, and goals.
2. `/cb match` scans the nearby area and validates those markers.
3. Players are assigned to blue, red, or spectator teams.
4. `/cb start` launches a countdown, teleports teams, and spawns the match ball.
5. Goals update the score, trigger effects, and restart the round.
6. The match ends on time limit or max goals, with overtime if scores are tied.

## Ball Behavior

- The ball is a falling block using the `ball.material` setting.
- Player movement adds velocity to the ball.
- Sneaking and sprinting use different vertical kick boosts.
- Bounce handling is split between the scheduled physics task and the block-change listener.
- Optional particles and sounds are played when the ball spawns or impacts.

## Arena Discovery

Arena setup is block-driven. CubeBall scans a radius around the admin and detects:

- One ball spawn marker
- Blue team spawn markers
- Red team spawn markers
- Blue goal markers
- Red goal markers

See [Arena Setup](arena-setup.md) for the exact marker list.
