# OumProfile

Available at:

[![Modrinth](https://img.shields.io/badge/Modrinth-oumprofile-00C853?style=for-the-badge&logo=modrinth&logoColor=white)](https://modrinth.com/plugin/oumprofile)
[![Hangar](https://img.shields.io/badge/Hangar-OumProfile-007FFF?style=for-the-badge&logo=papermc&logoColor=white)](https://hangar.papermc.io/sun-dev/OumProfile)
[![SpigotMC](https://img.shields.io/badge/SpigotMC-oumprofile-FF8C00?style=for-the-badge&logo=spigotmc&logoColor=white)](https://www.spigotmc.org/resources/oumprofile.131872/)

OumProfile is a multiprofile plugin for Paper servers. It allows players to create and switch between different profile
slots, each with their own inventory, status attributes, LuckPerms group, and Vault economy balance. This is useful for
servers offering distinct loadouts or gameplay modes on a single account.

This plugin is built using the [OumLib](https://github.com/sun-mc-dev/oumlib) framework.

---

## Features

Each player profile stores:

* **Inventory State**: Storage contents, armor contents, off-hand item, and ender chest.
* **Attributes**: Health, max health, food level, saturation, XP level, and XP progress.
* **Status States**: GameMode, active potion effects, fall distance, fire ticks, and remaining air bubbles.
* **Movement States**: Flight capabilities (allowFlight and isFlying state).
* **Optional Features**: Coordinate location (if saveLocation is enabled), Vault balance, LuckPerms group, mcMMO skills,
  AuraSkills, JobsReborn jobs, custom multi-currencies, playtime tracking, and vanilla Minecraft statistics.
* **Server Resilience**: Non-blocking periodic auto-save to protect against sudden crashes, plus administrative stale profile pruning (`/profile admin prune <days>`) to keep databases compact.

---

## Requirements

* Paper 1.21 or newer
* Java 21 or newer
* OumLib (shaded within the jar)

### Soft Dependencies

The plugin automatically integrates with the following if present on the server:

* **LuckPerms**: For per-profile permission groups.
* **Vault**: For per-profile economy balances.
* **mcMMO**: For per-profile skill level and experience synchronization.
* **AuraSkills**: For per-profile skill level and experience synchronization.
* **JobsReborn**: For per-profile job level and experience progression.
* **CombatLogX / PvPManager / DeluxeCombat**: Automatic combat tag detection for profile switch blocking.
* **PlaceholderAPI & MiniPlaceholders**: For displaying profile statistics in chats, scoreboards, and tablists.

---

## Placeholders

The following placeholders are supported under the `oumprofile` namespace:

| Description                    | PlaceholderAPI                              | MiniPlaceholders                            |
|:-------------------------------|:--------------------------------------------|:--------------------------------------------|
| Active profile name            | `%oumprofile_active%`                       | `<oumprofile_active>`                       |
| Total profiles created         | `%oumprofile_count%`                        | `<oumprofile_count>`                        |
| Maximum allowed profiles       | `%oumprofile_max%`                          | `<oumprofile_max>`                          |
| Saved Vault economy balance    | `%oumprofile_balance%`                      | `<oumprofile_balance>`                      |
| Stored primary LuckPerms group | `%oumprofile_group%`                        | `<oumprofile_group>`                        |
| Profile playtime (seconds)     | `%oumprofile_playtime%`                     | `<oumprofile_playtime>`                     |
| Formatted playtime (duration)  | `%oumprofile_playtime_formatted%`           | `<oumprofile_playtime_formatted>`           |
| Stored skill level             | `%oumprofile_skill_<plugin>_<skill>_level%` | `<oumprofile_skill_<plugin>_<skill>_level>` |
| Stored skill experience        | `%oumprofile_skill_<plugin>_<skill>_xp%`    | `<oumprofile_skill_<plugin>_<skill>_xp>`    |
| Stored job level               | `%oumprofile_job_<job>_level%`              | `<oumprofile_job_<job>_level>`              |
| Stored job experience          | `%oumprofile_job_<job>_xp%`                 | `<oumprofile_job_<job>_xp>`                 |
| Custom economy balance         | `%oumprofile_currency_<name>%`              | `<oumprofile_currency_<name>>`              |

---

## Commands and Permissions

### Player Commands

| Command                            | Description                                | Permission               | Default |
|:-----------------------------------|:-------------------------------------------|:-------------------------|:--------|
| `/profile`                         | Opens the profile selection GUI.           | `profiles.use`           | True    |
| `/profile list`                    | Lists all created profiles in chat.        | `profiles.use`           | True    |
| `/profile current`                 | Displays the active profile name.          | `profiles.use`           | True    |
| `/profile create <name>`           | Creates a new profile with the given name. | `profiles.create.<name>` | OP      |
| `/profile switch <name>`           | Switches to the specified profile.         | `profiles.use`           | True    |
| `/profile delete <name>`           | Deletes the specified profile.             | `profiles.use`           | True    |
| `/profile rename <old> <new>`      | Renames a profile.                         | `profiles.use`           | True    |
| `/profile alerts`                  | Toggles receiving admin profile alerts.    | `profiles.alerts`        | OP      |

*Aliases: `/profiles`, `/prof`*

### Bypass and Administrative Permissions

| Command / Permission                              | Description                                                  | Default                        |
|:--------------------------------------------------|:-------------------------------------------------------------|:-------------------------------|
| `/profile admin open <player>`                    | Opens the profile GUI for the specified player.              | OP (requires `profiles.admin`) |
| `/profile admin list <player>`                    | Lists all profiles of the specified player.                  | OP (requires `profiles.admin`) |
| `/profile admin create <player> <profile>`        | Creates a profile for the specified player.                  | OP (requires `profiles.admin`) |
| `/profile admin switch <player> <profile>`        | Forces the specified player to switch to a profile.          | OP (requires `profiles.admin`) |
| `/profile admin delete <player> <profile>`        | Deletes a profile for the specified player.                  | OP (requires `profiles.admin`) |
| `/profile admin rename <player> <old> <new>`      | Renames a profile for the specified player.                  | OP (requires `profiles.admin`) |
| `/profile admin export <player> <profile>`        | Exports a player's profile to a JSON file.                   | OP (requires `profiles.admin`) |
| `/profile admin import <player> <file>`           | Imports a profile from a JSON file for a player.             | OP (requires `profiles.admin`) |
| `/profile admin prune <days>`                     | Prunes all inactive profiles older than the given days.      | OP (requires `profiles.admin`) |
| `/profile debug`                                  | Toggles debug logging in console.                            | OP (requires `profiles.admin`) |
| `profiles.admin`                                  | Access to administrative commands and config reload.         | OP                             |
| `profiles.create.*`                               | Permission to create profiles with any name.                 | OP                             |
| `profiles.max.unlimited`                          | Bypasses all profile slot limits.                            | OP                             |
| `profiles.max.<number>`                           | Sets the maximum profile slot limit (e.g. `profiles.max.5`). | False                          |
| `profiles.bypass.combat`                          | Allows switching profiles while tagged in combat.            | OP                             |
| `profiles.bypass.warmup`                          | Bypasses the switch countdown warmup.                        | OP                             |
| `profiles.bypass.cooldown`                        | Bypasses the switch cooldown.                                | OP                             |
| `profiles.alerts`                                 | Receives administrative alerts for profile actions.          | OP                             |

---

## Configuration Reference

OumProfile is designed with a clean, modular configuration architecture split into 3 dedicated files:
- **`config.yml`**: Core plugin settings, database persistence, profile switching mechanics, auto-save, and third-party integrations.
- **`menus.yml`**: Inventory GUI layouts, custom head textures, button patterns, items, lores, and confirmation dialogs.
- **`messages.yml`**: Localization, chat messages, alerts, countdown titles, and error notifications with full MiniMessage support.

All configuration files support **live auto-reloading** without requiring a server restart.

### 1. `config.yml` (Main Configuration)

```yaml
# Enable detailed debug logging in console
debug: false

# Name of the default profile created on first join
default-profile-name: "default"

# Global date format pattern
date-format: "yyyy-MM-dd HH:mm"

# Enable administrative alerts when players switch, create, or delete profiles
admin-alerts-enabled: true

# Maximum character length for profile names
profile-name-max-length: 16

# Regex pattern for valid profile names
profile-name-regex: "[a-zA-Z0-9_-]+"

# Max profile limits based on profiles.max.<tier> permission nodes
limit-tiers:
  - 1
  - 3
  - 5
  - 10

# Periodic background auto-save settings for active player profiles
auto-save:
  enabled: true
  interval-minutes: 5

# Database storage settings (SQLite/MySQL)
storage:
  type: "sqlite" # 'sqlite' or 'mysql'
  host: "localhost"
  port: 3306
  database: "oumprofile"
  username: "root"
  password: ""

# Profile switching mechanics, warmups, combat checks, and sounds
switching:
  warmup-enabled: true
  warmup-seconds: 5
  cancel-on-move: true
  cancel-on-damage: true
  cancel-in-combat: true
  combat-tag-duration: 10
  switch-cooldown-seconds: 10
  save-location: false
  warmup-title-enabled: true
  warmup-title-text: "<color:#74c7ec>Switching Profile...</color>"
  warmup-subtitle-text: "<color:#9399b2>Do not move for <color:#f9e2af><seconds>s</color></color>"
  warmup-sound-enabled: true
  warmup-sound-key: "block.note_block.hat"
  warmup-complete-sound-key: "entity.player.levelup"
  warmup-cancel-sound-key: "entity.villager.no"

# LuckPerms group synchronization
luckperms:
  enabled: true

# Multi-currency economy settings
economy:
  enabled: true
  currencies:
    - "vault"
    - "playerpoints"

# Skill and Job synchronization settings
skills:
  mcmmo-enabled: true
  aura-skills-enabled: true
  jobs-enabled: true

# Vanilla statistics synchronization settings
statistics:
  enabled: true
  tracked:
    - "MOB_KILLS"
    - "DEATHS"
    - "JUMP"
```

### 2. `menus.yml` (GUI Layouts & Menus)

```yaml
gui:
  title: "<color:#5c5f77>Select a Profile</color>"
  rows: 3
  pattern:
    - "#########"
    - "  PPPPP  "
    - "####C####"
  profile-slot-char: "P"
  create-button-slot-char: "C"
  create-button-material: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19"
  create-button-material-limit-reached: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODE5OWI1ZWUzMjBlNzk5N2Q5MWJiNWY4NjY1ZjNkMzJhZTQ5MjBlMDNjNmIzZDliN2VlY2E2OTcxMTk5OTcifX19"
  create-button-name: "<color:#a6e3a1><b>Create New Profile</b></color>"
  create-button-name-limit-reached: "<color:#f38ba8><b>Profile Limit Reached</b></color>"
  create-button-lore:
    - "<color:#9399b2>Slots: <color:#f9e2af><slots_current></color> / <color:#9399b2><slots_max></color>"
    - ""
    - "<color:#a6e3a1>Click to start profile creation</color>"
  create-button-lore-limit-reached:
    - "<color:#9399b2>Slots: <color:#f9e2af><slots_current></color> / <color:#9399b2><slots_max></color>"
    - ""
    - "<color:#f38ba8>Purchase more slots on our store</color>"
  active-profile-material: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjdmYWFlMWQxOTgzNmJkMDc4NTQyNmU0ZmQyOGFhNjNhMzgxZTllNzE0OTU1OWVlNmIyYTUwOTk5NWJiY2ZkMiJ9fX0="
  inactive-profile-material: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDVjNmRjMmJiZjUxYzM2Y2ZjNzcxNDU4NWE2YTU2ODNlZjJiMTRkNDdkOGZmNzE0NjU0YTg5M2Y1ZGE2MjIifX19"
  empty-slot-material: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZiYTYzMzQ0ZjQ5ZGQxYzRmNTQ4OGU5MjZiZjNkOWUyYjI5OTE2YTZjNTBkNjEwYmI0MGE1MjczZGM4YzgyIn19fQ=="
  active-profile-name: "<color:#a6e3a1><b><name></b></color> <color:#9399b2>(Active)</color>"
  inactive-profile-name: "<color:#cba6f7><b><name></b></color>"
  empty-slot-name: "<color:#585b70>Empty Slot</color>"
  active-profile-lore:
    - "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>"
    - "<color:#9399b2>Created: <color:#cdd6f4><created></color></color>"
    - "<color:#9399b2>Last Used: <color:#cdd6f4><last_used></color></color>"
    - "<color:#9399b2>Playtime: <color:#f9e2af><playtime></color></color>"
    - "<color:#9399b2>Balance: <color:#f9e2af>$<balance></color></color>"
    - "<color:#9399b2>Rank Group: <color:#b4befe><group></color></color>"
    - "<color:#9399b2>Jobs: <color:#f9e2af><jobs></color></color>"
    - "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>"
    - "<color:#a6e3a1>Currently Active</color>"
  inactive-profile-lore:
    - "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>"
    - "<color:#9399b2>Created: <color:#cdd6f4><created></color></color>"
    - "<color:#9399b2>Last Used: <color:#cdd6f4><last_used></color></color>"
    - "<color:#9399b2>Playtime: <color:#f9e2af><playtime></color></color>"
    - "<color:#9399b2>Balance: <color:#f9e2af>$<balance></color></color>"
    - "<color:#9399b2>Rank Group: <color:#b4befe><group></color></color>"
    - "<color:#9399b2>Jobs: <color:#f9e2af><jobs></color></color>"
    - "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>"
    - "<color:#74c7ec>Left-Click to switch</color>"
    - "<color:#f38ba8>Right-Click to delete</color>"
  border-material: "GRAY_STAINED_GLASS_PANE"
  border-name: " "
  prompt-message: "<color:#74c7ec>Type a profile name in chat:</color>"
  text-input-timeout-seconds: 15
  cancel-word: "cancel"
  open-sound-enabled: true
  open-sound-key: "block.chest.open"
  click-sound-enabled: true
  click-sound-key: "ui.button.click"
  close-sound-enabled: true
  close-sound-key: "block.chest.close"
  error-sound-enabled: true
  error-sound-key: "entity.villager.no"

confirm-delete:
  enabled: true
  title: "<color:#f38ba8>Confirm Deleting <profile></color>"
  rows: 3
  pattern:
    - "#########"
    - "  C   D  "
    - "#########"
  confirm-slot-char: "C"
  confirm-material: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ=="
  confirm-name: "<color:#f38ba8><b>Confirm Deletion</b></color>"
  confirm-lore:
    - "<color:#a6adc8>Clicking here will permanently</color>"
    - "<color:#a6adc8>delete the profile <color:#fab387><profile></color>.</color>"
    - ""
    - "<color:#f38ba8><b>WARNING: This cannot be undone!</b></color>"
  deny-slot-char: "D"
  deny-material: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDMxMmNhNDYzMmRlZjVmZmFmMmViMGQ5ZDdjYzdiNTVhNTBjNGUzOTIwZDkwMzcyYWFiMTQwNzgxZjVkZmJjNCJ9fX0="
  deny-name: "<color:#a6e3a1><b>Cancel</b></color>"
  deny-lore:
    - "<color:#a6adc8>Click to keep your profile</color>"
    - "<color:#a6adc8>and return to the menu.</color>"
  border-material: "GRAY_STAINED_GLASS_PANE"
  border-name: " "

confirm-create:
  enabled: true
  title: "<color:#a6e3a1>Confirm Creating <name></color>"
  rows: 3
  pattern:
    - "#########"
    - "  C   D  "
    - "#########"
  confirm-slot-char: "C"
  confirm-material: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDMxMmNhNDYzMmRlZjVmZmFmMmViMGQ5ZDdjYzdiNTVhNTBjNGUzOTIwZDkwMzcyYWFiMTQwNzgxZjVkZmJjNCJ9fX0="
  confirm-name: "<color:#a6e3a1><b>Confirm Creation</b></color>"
  confirm-lore:
    - "<color:#a6adc8>Click here to create</color>"
    - "<color:#a6adc8>profile <color:#cba6f7><name></color>.</color>"
  deny-slot-char: "D"
  deny-material: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ=="
  deny-name: "<color:#f38ba8><b>Cancel</b></color>"
  deny-lore:
    - "<color:#a6adc8>Click to cancel creation</color>"
    - "<color:#a6adc8>and return to the menu.</color>"
  border-material: "GRAY_STAINED_GLASS_PANE"
  border-name: " "
```

### 3. `messages.yml` (Localization & Chat Messages)

```yaml
profile-not-found: "<color:#f38ba8>Profile <color:#fab387>'<target>'</color> does not exist.</color>"
profile-already-active: "<color:#f38ba8>You are already using that profile.</color>"
combat-block: "<color:#f38ba8>You cannot switch profiles while in combat.</color>"
warmup-start: "<color:#74c7ec>Switching to <color:#cba6f7><target></color> in <color:#fab387><seconds>s</color>...</color>"
switch-success: "<color:#a6e3a1>Switched to profile <color:#cba6f7><target></color>.</color>"
no-permission: "<color:#f38ba8>You don't have permission to create a profile named <color:#fab387>'<name>'</color>.</color>"
create-fail: "<color:#f38ba8>Could not create profile <color:#fab387>'<name>'</color> (limit reached or name exists).</color>"
create-success: "<color:#a6e3a1>Created profile <color:#cba6f7><name></color>.</color>"
delete-fail: "<color:#f38ba8>Could not delete profile <color:#fab387>'<name>'</color> (active, last remaining, or not found).</color>"
delete-success: "<color:#a6e3a1>Deleted profile <color:#cba6f7><name></color>.</color>"
help: "<color:#b4befe>OumProfile <color:#585b70>»</color> <color:#9399b2>/profile <list | current | create | switch | delete | rename | reload></color></color>"
list-header: "<color:#74c7ec>Your profiles (<color:#fab387><count></color>):</color>"
list-item-active: "<color:#a6e3a1>● <color:#cdd6f4><name></color> <color:#585b70>—</color> <color:#9399b2>Active</color></color>"
list-item-inactive: "<color:#9399b2>○ <color:#a6adc8><name></color> <color:#585b70>—</color> <color:#6c7086>Last used <date></color></color>"
current-profile: "<color:#b4befe>Active Profile: <color:#cba6f7><name></color></color>"
player-only: "<color:#f38ba8>This command can only be executed by players.</color>"
no-profiles: "<color:#9399b2>You do not have any profiles yet.</color>"
no-active-profile: "<color:#f38ba8>You do not have an active profile loaded.</color>"
reload-success: "<color:#a6e3a1>Configuration reloaded successfully.</color>"
switch-cooldown: "<color:#f38ba8>You must wait <color:#fab387><seconds></color> before switching profiles again.</color>"
max-profiles-reached: "<color:#f38ba8>You have reached the maximum number of profiles allowed (<color:#fab387><max></color>).</color>"
cannot-delete-active: "<color:#f38ba8>You cannot delete your active profile. Switch to another profile first.</color>"
cannot-delete-default: "<color:#f38ba8>You cannot delete the default profile.</color>"
invalid-profile-name: "<color:#f38ba8>Profile name cannot be empty or contain spaces.</color>"
profile-creation-cancelled: "<color:#9399b2>Profile creation cancelled.</color>"
profile-creation-timed-out: "<color:#f38ba8>Profile creation timed out.</color>"
player-not-found: "<color:#f38ba8>Player not found.</color>"
admin-open-success: "<color:#a6e3a1>Opened profile menu for <color:#cba6f7><target></color>.</color>"
admin-list-header: "<color:#74c7ec>Profiles for <color:#cba6f7><target></color> (<color:#fab387><count></color>):</color>"
admin-create-success: "<color:#a6e3a1>Created profile <color:#fab387><name></color> for <color:#cba6f7><target></color>.</color>"
admin-create-fail: "<color:#f38ba8>Failed to create profile for player (limit reached or name exists).</color>"
admin-switch-success: "<color:#a6e3a1>Switched <color:#cba6f7><target></color> to profile <color:#fab387><name></color>.</color>"
admin-switch-fail-no-profile: "<color:#f38ba8>Player does not have profile <color:#fab387>'<name>'</color>.</color>"
admin-delete-success: "<color:#a6e3a1>Deleted profile <color:#fab387><name></color> for <color:#cba6f7><target></color>.</color>"
admin-delete-fail: "<color:#f38ba8>Failed to delete profile for player (active, last profile, or not found).</color>"
admin-alert-switch: "<color:#f38ba8><b>ALERT</b></color> <color:#585b70><b>|</b></color> <color:#a6adc8><player> switched to profile <color:#cba6f7><b><to></b></color></color>"
admin-alert-create: "<color:#f38ba8><b>ALERT</b></color> <color:#585b70><b>|</b></color> <color:#a6adc8><player> created profile <color:#cba6f7><b><name></b></color></color>"
admin-alert-delete: "<color:#f38ba8><b>ALERT</b></color> <color:#585b70><b>|</b></color> <color:#a6adc8><player> deleted profile <color:#cba6f7><b><name></b></color></color>"
alerts-enabled: "<color:#a6e3a1>Profile alerts enabled.</color>"
alerts-disabled: "<color:#f38ba8>Profile alerts disabled.</color>"
warmup-cancelled-move: "<color:#f38ba8>Profile switch cancelled because you moved.</color>"
warmup-cancelled-damage: "<color:#f38ba8>Profile switch cancelled because you took damage.</color>"
warmup-cancelled-generic: "<color:#f38ba8>Profile switch cancelled.</color>"
debug-enabled: "<color:#a6e3a1>Debug mode enabled.</color>"
debug-disabled: "<color:#f38ba8>Debug mode disabled.</color>"
rename-success: "<color:#a6e3a1>Renamed profile <color:#fab387><old></color> to <color:#cba6f7><new></color>.</color>"
rename-fail: "<color:#f38ba8>Could not rename profile <color:#fab387>'<old>'</color>.</color>"
cannot-rename-default: "<color:#f38ba8>You cannot rename the default profile.</color>"
admin-rename-success: "<color:#a6e3a1>Renamed profile <color:#fab387><old></color> to <color:#cba6f7><new></color> for <color:#cba6f7><target></color>.</color>"
admin-rename-fail: "<color:#f38ba8>Failed to rename profile for player.</color>"
admin-alert-rename: "<color:#f38ba8><b>ALERT</b></color> <color:#585b70><b>|</b></color> <color:#a6adc8><player> renamed profile <color:#cba6f7><b><old></b></color> to <color:#cba6f7><b><new></b></color></color>"
profile-name-too-long: "<color:#f38ba8>Profile name must be at most <color:#fab387><max></color> characters.</color>"
profile-name-invalid-chars: "<color:#f38ba8>Profile name contains invalid characters. Only letters, numbers, hyphens and underscores are allowed.</color>"
export-success: "<color:#a6e3a1>Exported profile <color:#fab387><name></color> to file.</color>"
import-success: "<color:#a6e3a1>Imported profile <color:#fab387><name></color> for <color:#cba6f7><target></color>.</color>"
import-fail: "<color:#f38ba8>Failed to import profile from file.</color>"
admin-prune-success: "<color:#a6e3a1>Successfully pruned <color:#fab387><count></color> inactive profile(s) older than <color:#fab387><days></color> days.</color>"
admin-prune-none: "<color:#9399b2>No inactive profiles older than <color:#fab387><days></color> days were found to prune.</color>"
invalid-days: "<color:#f38ba8>Please specify a valid number of days (greater than 0).</color>"
```

### Configuration Options

#### Global Settings (`config.yml`)

| Option                    | Type          | Default            | Description                                                                         |
|:--------------------------|:--------------|:-------------------|:------------------------------------------------------------------------------------|
| `debug`                   | Boolean       | `false`            | Enable detailed debug logging in the server console.                                |
| `default-profile-name`    | String        | `default`          | Name of the initial profile created automatically when a player first joins.        |
| `date-format`             | String        | `yyyy-MM-dd HH:mm` | Date format used for displaying profile creation and last used timestamps.          |
| `admin-alerts-enabled`    | Boolean       | `true`             | Broadcast profile actions (create, delete, switch, rename) to administrators.       |
| `limit-tiers`             | List<Integer> | `[1, 3, 5, 10]`    | Profile slot limit thresholds based on permission nodes (e.g. `profiles.max.5`).    |
| `profile-name-max-length` | Integer       | `16`               | Maximum character length allowed for profile names.                                 |
| `profile-name-regex`      | String        | `[a-zA-Z0-9_-]+`   | Regex pattern that profile names must match.                                        |
| `auto-save.enabled`       | Boolean       | `true`             | Enable periodic background auto-saving of active player profiles.                   |
| `auto-save.interval-minutes` | Integer    | `5`                | Time in minutes between automatic profile saves.                                    |

#### Switching Settings (`switching` in `config.yml`)

| Option                     | Type    | Default | Description                                                                        |
|:---------------------------|:--------|:--------|:-----------------------------------------------------------------------------------|
| `warmup-enabled`           | Boolean | `true`  | If true, players must stand still for a warmup duration before switching profiles. |
| `warmup-seconds`           | Integer | `5`     | Warmup countdown duration in seconds.                                              |
| `cancel-on-move`           | Boolean | `true`  | Cancel the switch warmup if the player moves.                                      |
| `cancel-on-damage`         | Boolean | `true`  | Cancel the switch warmup if the player takes damage.                               |
| `cancel-in-combat`         | Boolean | `true`  | Cancel the switch warmup if the player is in combat.                               |
| `combat-tag-duration`      | Integer | `10`    | Duration in seconds that a player remains tagged in combat.                        |
| `switch-cooldown-seconds`  | Integer | `10`    | Cooldown period in seconds before a player can switch profiles again.              |
| `save-location`             | Boolean | `false` | Save and restore player coordinates per-profile.                                   |
| `warmup-title-enabled`     | Boolean | `true`  | Show title/subtitle countdown during warmup.                                       |

#### Storage Settings (`storage` in `config.yml`)

| Option     | Type    | Default      | Description                                  |
|:-----------|:--------|:-------------|:---------------------------------------------|
| `type`     | String  | `sqlite`     | Database storage type (`sqlite` or `mysql`). |
| `host`     | String  | `localhost`  | Hostname of the MySQL database.              |
| `port`     | Integer | `3306`       | Port of the MySQL database.                  |
| `database` | String  | `oumprofile` | Name of the MySQL schema.                    |
| `username` | String  | `root`       | Username for MySQL database authentication.  |
| `password` | String  | `""`         | Password for MySQL database authentication.  |

#### Integrations Settings (`config.yml`)

| Option                      | Type         | Default                           | Description                                          |
|:----------------------------|:-------------|:----------------------------------|:-----------------------------------------------------|
| `luckperms.enabled`         | Boolean      | `true`                            | Synchronize LuckPerms permission groups per-profile. |
| `economy.enabled`           | Boolean      | `true`                            | Enable per-profile multi-currency balances.          |
| `economy.currencies`        | List<String> | `["vault", "playerpoints"]`       | Currencies synchronized per-profile.                 |
| `skills.mcmmo-enabled`      | Boolean      | `true`                            | Synchronize mcMMO level and XP per-profile.          |
| `skills.aura-skills-enabled`| Boolean      | `true`                            | Synchronize AuraSkills level and XP per-profile.     |
| `skills.jobs-enabled`       | Boolean      | `true`                            | Synchronize JobsReborn job level and XP per-profile. |
| `statistics.enabled`        | Boolean      | `true`                            | Synchronize vanilla Minecraft statistics.            |
| `statistics.tracked`        | List<String> | `["MOB_KILLS", "DEATHS", "JUMP"]` | Vanilla statistics tracked.                          |

#### GUI Settings (`menus.yml`)

| Option                      | Type         | Description                                                                                          |
|:----------------------------|:-------------|:-----------------------------------------------------------------------------------------------------|
| `title`                     | String       | Title of the profile inventory menu (supports MiniMessage).                                          |
| `rows`                      | Integer      | Number of rows in the GUI grid (1-6).                                                                |
| `pattern`                   | List<String> | Character pattern defining the layout (e.g. `P` for profile items, `C` for creation button).         |
| `active-profile-material`   | String       | Item material/texture for the currently active profile (supports `head:<Base64>` or custom bridges).|
| `inactive-profile-material` | String       | Item material/texture for inactive profiles.                                                         |
| `empty-slot-material`       | String       | Item material/texture for unfilled profile slots.                                                    |
| `confirm-delete.enabled`    | Boolean      | Toggle confirmation dialog before deleting profiles.                                                 |
| `confirm-create.enabled`    | Boolean      | Toggle confirmation dialog before creating profiles.                                                 |

##### GUI Lore Placeholders

The active and inactive profile lore lists support the following placeholders:
* `<created>`: Profile creation timestamp.
* `<last_used>`: Timestamp when the profile was last logged into.
* `<playtime>`: Total active playtime formatted as duration.
* `<balance>`: Current Vault/Economy balance.
* `<group>`: Stored primary LuckPerms group name.
* `<jobs>`: Integrated JobsReborn jobs and level (e.g. `Miner (Lv. 30)`).
* `<mcmmo>`: Integrated mcMMO skills and level (e.g. `Acrobatics (Lv. 10)`).
* `<auraskills>`: Integrated AuraSkills level.

---

## Custom Items

GUI materials in the configuration support OumLib's `ItemBridge`. If you have Oraxen, ItemsAdder, Nexo, MythicMobs, or MMOItems installed on your server, you can query their items directly using their custom item identifiers:
* `oraxen:custom_coin`
* `itemsadder:blue_sword`
* `nexo:golden_cup`
* `mmoitems:SWORD:EXCALIBUR`
* `mythicmobs:skeleton_key`

---

## Developer API

You can hook into OumProfile directly using the static facade `ProfileAPI`:

```java
package dev.oum.profile.example;

import dev.oum.profile.api.ProfileAPI;
import dev.oum.profile.model.ProfileData;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;

public class OumProfileAPIExample {

    public void demonstrateAPI(Player player, UUID uuid) {
        // Get all loaded profiles for a player
        Map<String, ProfileData> profiles = ProfileAPI.getProfiles(uuid);

        // Get active profile name and data
        String activeName = ProfileAPI.getActiveProfileName(uuid);
        ProfileData activeProfile = ProfileAPI.getActiveProfile(uuid);

        // Modify or check profile configurations
        boolean hasPvp = ProfileAPI.hasProfile(uuid, "pvp");
        int maxSlots = ProfileAPI.getMaxProfiles(player);

        // Programmatically create, delete, switch, or rename profiles
        ProfileAPI.createProfile(player, "pvp");
        ProfileAPI.deleteProfile(player, "pvp");
        ProfileAPI.switchProfile(player, "pvp");
        ProfileAPI.renameProfile(player, "pvp", "factions");

        // Manage switch warmup states
        boolean isWarmupActive = ProfileAPI.hasPendingWarmup(uuid);
        ProfileAPI.cancelWarmup(uuid);

        // Read or adjust profile balances
        double pvpBalance = ProfileAPI.getProfileBalance(uuid, "pvp");
        ProfileAPI.setProfileBalance(uuid, "pvp", 5000.0);

        // Maintenance & auto-save
        ProfileAPI.saveAllOnline();
        ProfileAPI.pruneInactiveProfiles(180);

        // Read integrated stats, playtime, and plugin data
        long playtime = ProfileAPI.getProfilePlaytimeSeconds(uuid, "pvp");
        Map<String, SkillData> mcmmo = ProfileAPI.getProfileMcMMO(uuid, "pvp");
        Map<String, SkillData> auraskills = ProfileAPI.getProfileAuraSkills(uuid, "pvp");
        Map<String, SkillData> jobs = ProfileAPI.getProfileJobs(uuid, "pvp");
        Map<String, Double> currencies = ProfileAPI.getProfileCurrencies(uuid, "pvp");
        Map<String, Integer> stats = ProfileAPI.getProfileStatistics(uuid, "pvp");
    }
}
```

### Custom API Events

* **`ProfileLoadEvent`**: Fired when a player's profiles are loaded on join.
* **`ProfileCreateEvent`** *(Cancellable)*: Fired before a profile is created.
* **`ProfileDeleteEvent`** *(Cancellable)*: Fired before a profile is deleted.
* **`ProfileRenameEvent`** *(Cancellable)*: Fired before a profile is renamed.
* **`ProfileSwitchEvent`** *(Cancellable)*: Fired when a profile switch is requested, and right before the switch takes
  place.
* **`ProfilePostSwitchEvent`**: Fired after a profile switch completes.

---

## Compilation

Build the plugin using Maven:

```bash
mvn clean package
```

The compiled jar file will be located in the `target/` directory. Copy it into the server's `plugins/` directory to run
it.

---

## Support & Developer Info

For support, bug reports, feature requests, or developer discussions, join our official Discord server:

* **Discord Support Server:** [https://discord.gg/maDcwPV6KB](https://discord.gg/maDcwPV6KB)

Developed with ❤️ by the sun-plugins team.

---

## License

This project is licensed under a custom software license agreement. See the [LICENSE](LICENSE.md) file for the full terms.
