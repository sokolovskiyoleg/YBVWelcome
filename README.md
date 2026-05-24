# YBVWelcome

English | [Русский](README.ru.md)

**YBVWelcome** is a modern, lightweight Paper/Spigot plugin that lets players and admins customize join/quit messages with clear priority rules and config-driven behavior.

## Features

- Group-based join/quit messages by permissions (`group-messages` in `messages.yml`)
- Player custom messages (`/welcome set join|quit ...`)
- Message priority: `custom -> group -> default`
- Color support:
  - Legacy codes (`&a`, `&f`, etc.)
  - Hex colors (`&#RRGGBB`)
  - Optional MiniMessage serializer
- Optional PlaceholderAPI integration
- Runtime debug command (`/welcome debug`)
- SQLite/MySQL support

## Commands

| Command | Description | Permission |
| --- | --- | --- |
| `/welcome` | Show usage/help | `ybvwelcome.use` |
| `/welcome set <join|quit> <text>` | Set personal join/quit message | `ybvwelcome.set` |
| `/welcome clear <join|quit>` | Clear personal join/quit message | `ybvwelcome.clear` |
| `/welcome reload` | Reload plugin configs | `ybvwelcome.admin` |
| `/welcome debug` | Show runtime state | `ybvwelcome.admin` |

## Install

1. Put the plugin `.jar` into your server `plugins/` directory.
2. Start the server once to generate config files.
3. Edit:
   - `config.yml` for runtime/database settings
   - `messages.yml` for formats, command messages, and group rules
4. Reload or restart the server.

```bash
mvn package
```

Run tests:

```bash
mvn test
```
