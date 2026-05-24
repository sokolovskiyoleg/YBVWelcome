# YBVWelcome

[English](README.md) | Русский

**YBVWelcome** — современный и лёгкий плагин для Paper/Spigot, который позволяет игрокам и администраторам настраивать сообщения входа/выхода через понятные правила приоритета и конфиг.

## Возможности

- Групповые сообщения входа/выхода по пермишенам (`group-messages` в `messages.yml`)
- Персональные сообщения игроков (`/welcome set join|quit ...`)
- Приоритет сообщений: `custom -> group -> default`
- Поддержка цветов:
  - Legacy-коды (`&a`, `&f` и т.д.)
  - Hex-цвета (`&#RRGGBB`)
  - Опциональный MiniMessage serializer
- Опциональная интеграция с PlaceholderAPI
- Команда runtime-диагностики (`/welcome debug`)
- Поддержка SQLite/MySQL

## Команды

| Команда | Описание | Пермишен |
| --- | --- | --- |
| `/welcome` | Показать справку | `ybvwelcome.use` |
| `/welcome set <join|quit> <текст>` | Установить личное сообщение входа/выхода | `ybvwelcome.set` |
| `/welcome clear <join|quit>` | Очистить личное сообщение входа/выхода | `ybvwelcome.clear` |
| `/welcome reload` | Перезагрузить конфиги плагина | `ybvwelcome.admin` |
| `/welcome debug` | Показать runtime-состояние | `ybvwelcome.admin` |

## Установка

1. Поместите `.jar` плагина в папку `plugins/`.
2. Один раз запустите сервер для генерации файлов конфигурации.
3. Настройте:
   - `config.yml` для runtime/database параметров
   - `messages.yml` для форматов, сообщений команд и групп
4. Выполните reload или перезапустите сервер.

```bash
mvn package
```

Тесты:

```bash
mvn test
```
