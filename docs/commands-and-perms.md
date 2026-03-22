# Commands And Permissions

The main command is `/cb`.

## Commands

`/cb reload`
: Reloads `config.yml`. Available from console and to players with `cubeball.manage`.

`/cb match`
: Scans the nearby arena markers and prepares a match.

`/cb start`
: Starts the configured match and launches the countdown.

`/cb stop`
: Cancels the current match.

`/cb pause`
: Pauses the current match and removes the active ball.

`/cb resume`
: Restarts a paused match with a new countdown.

`/cb team <BLUE|RED|SPECTATOR> <player>`
: Assigns a player to a team.

`/cb generate <id>`
: Spawns a ball at the executing player's location.

`/cb generate <id> <x> <y> <z> <world>`
: Spawns a ball at an explicit position.

`/cb remove <id>`
: Removes a tracked ball by identifier.

## Permission

`cubeball.manage`
: Grants access to management commands. Defaults to server operators.

## Notes

- Console support is intentionally limited to `reload`, `generate`, and `remove`.
- Tab completion is available for admins and suggests commands, teams, tracked ball IDs, and worlds.
