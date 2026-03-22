# Troubleshooting

## `/cb match` Reports `KO`

Possible causes:

- No ball spawn marker was found
- One team has no spawn markers
- One goal side has no goal markers
- The arena is outside `match.scan-radius`
- Marker materials in the world do not match `config.yml`

## The Ball Falls Incorrectly Or Stops Too Often

Check these settings:

- `physics.block-bounce.threshold`
- `physics.listener-bounce.divisor`
- `physics.listener-bounce.max-y-velocity`
- `physics.listener-bounce.stop-velocity-threshold`
- `physics.listener-bounce.stop-min-y-velocity`

Paper is recommended if entity behavior differs on your server fork.

## Players Cannot Use Admin Commands

Verify one of these is true:

- The player is operator
- The player has the `cubeball.manage` permission

## Configuration Changes Do Not Apply

Run:

```text
/cb reload
```

If the issue persists, check startup logs for invalid enum values in materials, particles, sounds, or effects.
