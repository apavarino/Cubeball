# Contributing

## Local Setup

Requirements:

- Java `17`
- Python `3.11` for the documentation toolchain

## Build And Test

Run the main plugin build and test suite with:

```bash
./gradlew test shadowJar
```

Useful additional task:

```bash
./gradlew printVersion
```

The plugin artifact is generated in `build/libs/`.

## Documentation

Install the MkDocs toolchain:

```bash
pip install -r requirements.txt
```

Run the documentation locally:

```bash
mkdocs serve
```

Validate the static build:

```bash
mkdocs build --strict
```

The documentation stack uses the Stellionix shared MkDocs package configured in [requirements.txt](/C:/Users/arthu/Documents/GitHub/plugins/bukkit/Cubeball/requirements.txt) and [mkdocs.yml](/C:/Users/arthu/Documents/GitHub/plugins/bukkit/Cubeball/mkdocs.yml).

## CI

GitHub Actions validates:

- Gradle build and tests in `.github/workflows/ci.yml`
- Documentation build in `.github/workflows/ci.yml`
- Documentation deployment in `.github/workflows/docs.yml`

## Contribution Scope

When changing the plugin:

- Keep gameplay behavior covered by tests where practical
- Update `docs/` when commands, config, installation, or admin workflows change
- Preserve version consistency between Gradle and `plugin.yml`

## Pull Requests

Before opening a pull request, make sure:

1. `./gradlew test shadowJar` passes.
2. `mkdocs build --strict` passes.
3. The related documentation has been updated when needed.
