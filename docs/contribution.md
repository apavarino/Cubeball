# Contribution

## Local Development

Build the plugin:

```bash
./gradlew test shadowJar
```

Build the documentation locally:

```bash
pip install -r requirements.txt
mkdocs serve
```

## Repository Conventions

- Java 17 is required
- The plugin artifact is produced with the Shadow plugin
- Documentation is published through GitHub Actions and `Stellionix/docs-common`

## Pull Requests

- Keep gameplay behavior covered by tests when possible
- Update `docs/` when commands, config, or installation steps change
- Ensure `./gradlew test shadowJar` still passes before submitting
