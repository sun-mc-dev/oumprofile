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

| Command                  | Description                                | Permission               | Default |
|:-------------------------|:-------------------------------------------|:-------------------------|:--------|
| `/profile`               | Opens the profile selection GUI.           | `profiles.use`           | True    |
| `/profile list`          | Lists all created profiles in chat.        | `profiles.use`           | True    |
| `/profile current`       | Displays the active profile name.          | `profiles.use`           | True    |
| `/profile create <name>` | Creates a new profile with the given name. | `profiles.create.<name>` | OP      |
| `/profile switch <name>` | Switches to the specified profile.         | `profiles.use`           | True    |
| `/profile delete <name>` | Deletes the specified profile.             | `profiles.use`           | True    |
| `/profile alerts`        | Toggles receiving admin profile alerts.    | `profiles.alerts`        | OP      |

*Aliases: `/profiles`, `/prof`*

### Bypass and Administrative Permissions

| Command / Permission                       | Description                                                  | Default                        |
|:-------------------------------------------|:-------------------------------------------------------------|:-------------------------------|
| `/profile admin-open <player>`             | Opens the profile GUI for the specified player.              | OP (requires `profiles.admin`) |
| `/profile admin-list <player>`             | Lists all profiles of the specified player.                  | OP (requires `profiles.admin`) |
| `/profile admin-create <player> <profile>` | Creates a profile for the specified player.                  | OP (requires `profiles.admin`) |
| `/profile admin-switch <player> <profile>` | Forces the specified player to switch to a profile.          | OP (requires `profiles.admin`) |
| `/profile admin-delete <player> <profile>` | Deletes a profile for the specified player.                  | OP (requires `profiles.admin`) |
| `/profile debug`                           | Toggles debug logging in console.                            | OP (requires `profiles.admin`) |
| `profiles.admin`                           | Access to administrative commands and config reload.         | OP                             |
| `profiles.create.*`                        | Permission to create profiles with any name.                 | OP                             |
| `profiles.max.unlimited`                   | Bypasses all profile slot limits.                            | OP                             |
| `profiles.max.<number>`                    | Sets the maximum profile slot limit (e.g. `profiles.max.5`). | False                          |
| `profiles.bypass.combat`                   | Allows switching profiles while tagged in combat.            | OP                             |
| `profiles.bypass.warmup`                   | Bypasses the switch countdown warmup.                        | OP                             |
| `profiles.bypass.cooldown`                 | Bypasses the switch cooldown.                                | OP                             |
| `profiles.alerts`                          | Receives administrative alerts for profile actions.          | OP                             |

---

## Configuration Reference

The `config.yml` file allows detailed configuration of storage backends, warmup checks, interface layouts, and messages:

```yaml
# Enable detailed debug logging in console
debug: false

# Profile switching settings
switching:
  warmupEnabled: true
  warmupSeconds: 5
  cancelOnMove: true
  cancelOnDamage: true
  cancelInCombat: true
  combatTagDuration: 10
  switchCooldownSeconds: 10
  saveLocation: false
  warmupTitleEnabled: true
  warmupTitleText: "<color:#74c7ec>Switching Profile...</color>"
  warmupSubtitleText: "<color:#9399b2>Do not move for <color:#f9e2af><seconds>s</color></color>"
  warmupSoundEnabled: true
  warmupSoundKey: "block.note_block.hat"
  warmupCompleteSoundKey: "entity.player.levelup"
  warmupCancelSoundKey: "entity.villager.no"

# Database storage settings (SQLite/MySQL)
storage:
  type: "sqlite"
  host: "localhost"
  port: 3306
  database: "oumprofile"
  username: "root"
  password: ""

# LuckPerms group integration
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
  mcmmoEnabled: true
  auraSkillsEnabled: true
  jobsEnabled: true

# Vanilla statistics synchronization settings
statistics:
  enabled: true
  tracked:
    - "MOB_KILLS"
    - "DEATHS"
    - "JUMP"

# Custom plugin messages (MiniMessage tags allowed)
messages:
  profileNotFound: "<color:#f38ba8>Profile <color:#fab387>'<target>'</color> does not exist.</color>"
  profileAlreadyActive: "<color:#f38ba8>You are already using that profile.</color>"
  combatBlock: "<color:#f38ba8>You cannot switch profiles while in combat.</color>"
  warmupStart: "<color:#74c7ec>Switching to <color:#cba6f7><target></color> in <color:#fab387><seconds>s</color>...</color>"
  switchSuccess: "<color:#a6e3a1>Switched to profile <color:#cba6f7><target></color>.</color>"
  noPermission: "<color:#f38ba8>You don't have permission to create a profile named <color:#fab387>'<name>'</color>.</color>"
  createFail: "<color:#f38ba8>Could not create profile <color:#fab387>'<name>'</color> (limit reached or name exists).</color>"
  createSuccess: "<color:#a6e3a1>Created profile <color:#cba6f7><name></color>.</color>"
  deleteFail: "<color:#f38ba8>Could not delete profile <color:#fab387>'<name>'</color> (active, last remaining, or not found).</color>"
  deleteSuccess: "<color:#a6e3a1>Deleted profile <color:#cba6f7><name></color>.</color>"
  help: "<color:#b4befe>OumProfile <color:#585b70>»</color> <color:#9399b2>/profile <list | current | create | switch | delete | reload></color></color>"
  listHeader: "<color:#74c7ec>Your profiles (<color:#fab387><count></color>):</color>"
  listItemActive: "<color:#a6e3a1>● <color:#cdd6f4><name></color> <color:#585b70>—</color> <color:#9399b2>Active</color></color>"
  listItemInactive: "<color:#9399b2>○ <color:#a6adc8><name></color> <color:#585b70>—</color> <color:#6c7086>Last used <date></color></color>"
  currentProfile: "<color:#b4befe>Active Profile: <color:#cba6f7><name></color></color>"
  playerOnly: "<color:#f38ba8>This command must be run as a player.</color>"
  noProfiles: "<color:#9399b2>You have no profiles.</color>"
  noActiveProfile: "<color:#f38ba8>No active profile found.</color>"
  reloadSuccess: "<color:#a6e3a1>Configuration reloaded successfully.</color>"
  switchCooldown: "<color:#f38ba8>Please wait <color:#fab387><seconds>s</color> before switching profiles again.</color>"
  maxProfilesReached: "<color:#f38ba8>You have reached your maximum profile slot limit.</color>"
  cannotDeleteActive: "<color:#f38ba8>You cannot delete your active profile.</color>"
  cannotDeleteDefault: "<color:#f38ba8>You cannot delete your default profile.</color>"
  invalidProfileName: "<color:#f38ba8>Profile name must not be empty or contain spaces.</color>"
  profileCreationCancelled: "<color:#f38ba8>Profile creation cancelled.</color>"
  profileCreationTimedOut: "<color:#f38ba8>Profile creation timed out.</color>"
  playerNotFound: "<color:#f38ba8>Player not found.</color>"
  adminOpenSuccess: "<color:#a6e3a1>Opened profile menu for <color:#cba6f7><target></color>.</color>"
  adminListHeader: "<color:#74c7ec>Profiles for <color:#cba6f7><target></color> (<color:#fab387><count></color>):</color>"
  adminCreateSuccess: "<color:#a6e3a1>Successfully created profile <color:#fab387><name></color> for <color:#cba6f7><target></color>.</color>"
  adminCreateFail: "<color:#f38ba8>Failed to create profile (already exists or limit reached).</color>"
  adminSwitchSuccess: "<color:#a6e3a1>Forced <color:#cba6f7><target></color> to switch to profile <color:#fab387><name></color>.</color>"
  adminSwitchFailNoProfile: "<color:#f38ba8>Player does not have a profile named <color:#fab387>'<name>'</color>.</color>"
  adminDeleteSuccess: "<color:#a6e3a1>Successfully deleted profile <color:#fab387><name></color> for <color:#cba6f7><target></color>.</color>"
  adminDeleteFail: "<color:#f38ba8>Failed to delete profile (active, last remaining, or not found).</color>"
  adminAlertSwitch: "<color:#f38ba8><b>ALERT</b></color> <color:#585b70><b>|</b></color> <color:#a6adc8><player> switched to profile <color:#cba6f7><b><target></b></color></color>"
  adminAlertCreate: "<color:#f38ba8><b>ALERT</b></color> <color:#585b70><b>|</b></color> <color:#a6adc8><player> created profile <color:#cba6f7><b><name></b></color></color>"
  adminAlertDelete: "<color:#f38ba8><b>ALERT</b></color> <color:#585b70><b>|</b></color> <color:#a6adc8><player> deleted profile <color:#cba6f7><b><name></b></color></color>"
  alertsEnabled: "<color:#a6e3a1>Profile alerts enabled.</color>"
  alertsDisabled: "<color:#f38ba8>Profile alerts disabled.</color>"
  warmupCancelledMove: "<color:#f38ba8>Profile switch cancelled because you moved.</color>"
  warmupCancelledDamage: "<color:#f38ba8>Profile switch cancelled because you took damage.</color>"
  warmupCancelledGeneric: "<color:#f38ba8>Profile switch cancelled.</color>"
  debugEnabled: "<color:#a6e3a1>Debug mode enabled.</color>"
  debugDisabled: "<color:#f38ba8>Debug mode disabled.</color>"

# GUI menus and chat input settings
gui:
  title: "<color:#5c5f77>Select a Profile</color>"
  rows: 3
  pattern:
    - "#########"
    - "  PPPPP  "
    - "####C####"
  createButtonMaterial: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNWZmMzE0MzFkNjQ1ODdmZjZlZjk4YzA2NzU4MTA2ODFmOGMxM2JmOTZmNTFkOWNiMDdlZDc4NTJiMmZmZDEifX19"
  createButtonMaterialLimitReached: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvODE5OWI1ZWUzMjBlNzk5N2Q5MWJiNWY4NjY1ZjNkMzJhZTQ5MjBlMDNjNmIzZDliN2VlY2E2OTcxMTk5OTcifX19"
  createButtonName: "<color:#a6e3a1><b>Create New Profile</b></color>"
  createButtonNameLimitReached: "<color:#f38ba8><b>Profile Limit Reached</b></color>"
  createButtonLore:
    - "<color:#9399b2>Slots: <color:#f9e2af><slots_current></color> / <color:#9399b2><slots_max></color>"
    - ""
    - "<color:#a6e3a1>Click to start profile creation</color>"
  createButtonLoreLimitReached:
    - "<color:#9399b2>Slots: <color:#f9e2af><slots_current></color> / <color:#9399b2><slots_max></color>"
    - ""
    - "<color:#f38ba8>Purchase more slots on our store</color>"
  activeProfileMaterial: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZjdmYWFlMWQxOTgzNmJkMDc4NTQyNmU0ZmQyOGFhNjNhMzgxZTllNzE0OTU1OWVlNmIyYTUwOTk5NWJiY2ZkMiJ9fX0="
  inactiveProfileMaterial: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvZDVjNmRjMmJiZjUxYzM2Y2ZjNzcxNDU4NWE2YTU2ODNlZjJiMTRkNDdkOGZmNzE0NjU0YTg5M2Y1ZGE2MjIifX19"
  emptySlotMaterial: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDZiYTYzMzQ0ZjQ5ZGQxYzRmNTQ4OGU5MjZiZjNkOWUyYjI5OTE2YTZjNTBkNjEwYmI0MGE1MjczZGM4YzgyIn19fQ=="
  activeProfileName: "<color:#a6e3a1><b><name></b></color> <color:#9399b2>(Active)</color>"
  inactiveProfileName: "<color:#cba6f7><b><name></b></color>"
  emptySlotName: "<color:#585b70>Empty Slot</color>"
  activeProfileLore:
    - "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>"
    - "<color:#9399b2>Created: <color:#cdd6f4><created></color></color>"
    - "<color:#9399b2>Last Used: <color:#cdd6f4><last_used></color></color>"
    - "<color:#9399b2>Playtime: <color:#f9e2af><playtime></color></color>"
    - "<color:#9399b2>Balance: <color:#f9e2af>$<balance></color></color>"
    - "<color:#9399b2>Rank Group: <color:#b4befe><group></color></color>"
    - "<color:#9399b2>Jobs: <color:#f9e2af><jobs></color></color>"
    - "<color:#585b70>━━━━━━━━━━━━━━━━━━━━━</color>"
    - "<color:#a6e3a1>Currently Active</color>"
  inactiveProfileLore:
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
  borderMaterial: "GRAY_STAINED_GLASS_PANE"
  borderName: " "
  promptMessage: "<color:#cba6f7><b>Profile Creation</b></color>\n<color:#9399b2>Type a name in chat for your new profile.\nType <color:#f38ba8><b>cancel</b></color> to return.</color>"
  cancelWord: "cancel"
  textInputTimeoutSeconds: 30
  profileSlotChar: "P"
  createButtonSlotChar: "C"
  openSoundEnabled: true
  openSoundKey: "block.chest.open"
  clickSoundEnabled: true
  clickSoundKey: "ui.button.click"
  errorSoundEnabled: true
  errorSoundKey: "entity.villager.no"
  closeSoundEnabled: true
  closeSoundKey: "block.chest.close"

# Confirmation GUI settings for deleting profiles
confirmDelete:
  enabled: true
  title: "<color:#f38ba8>Confirm Deleting <profile></color>"
  rows: 3
  pattern:
    - "#########"
    - "  C   D  "
    - "#########"
  confirmSlotChar: "C"
  confirmMaterial: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ=="
  confirmName: "<color:#f38ba8><b>Confirm Deletion</b></color>"
  confirmLore:
    - "<color:#a6adc8>Clicking here will permanently</color>"
    - "<color:#a6adc8>delete the profile <color:#fab387><profile></color>.</color>"
    - ""
    - "<color:#f38ba8><b>WARNING: This cannot be undone!</b></color>"
  denySlotChar: "D"
  denyMaterial: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDMxMmNhNDYzMmRlZjVmZmFmMmViMGQ5ZDdjYzdiNTVhNTBjNGUzOTIwZDkwMzcyYWFiMTQwNzgxZjVkZmJjNCJ9fX0="
  denyName: "<color:#a6e3a1><b>Cancel</b></color>"
  denyLore:
    - "<color:#a6adc8>Click to keep your profile</color>"
    - "<color:#a6adc8>and return to the menu.</color>"
  borderMaterial: "GRAY_STAINED_GLASS_PANE"
  borderName: " "

# Confirmation GUI settings for creating profiles
confirmCreate:
  enabled: true
  title: "<color:#a6e3a1>Confirm Creating <name></color>"
  rows: 3
  pattern:
    - "#########"
    - "  C   D  "
    - "#########"
  confirmSlotChar: "C"
  confirmMaterial: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvNDMxMmNhNDYzMmRlZjVmZmFmMmViMGQ5ZDdjYzdiNTVhNTBjNGUzOTIwZDkwMzcyYWFiMTQwNzgxZjVkZmJjNCJ9fX0="
  confirmName: "<color:#a6e3a1><b>Confirm Creation</b></color>"
  confirmLore:
    - "<color:#a6adc8>Click here to create</color>"
    - "<color:#a6adc8>profile <color:#cba6f7><name></color>.</color>"
  denySlotChar: "D"
  denyMaterial: "head:eyJ0ZXh0dXJlcyI6eyJTS0lOIjp7InVybCI6Imh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvYmViNTg4YjIxYTZmOThhZDFmZjRlMDg1YzU1MmRjYjA1MGVmYzljYWI0MjdmNDYwNDhmMThmYzgwMzQ3NWY3In19fQ=="
  denyName: "<color:#f38ba8><b>Cancel</b></color>"
  denyLore:
    - "<color:#a6adc8>Click to cancel creation</color>"
    - "<color:#a6adc8>and return to the menu.</color>"
  borderMaterial: "GRAY_STAINED_GLASS_PANE"
  borderName: " "

# Max profile limits based on oumprofile.max.<tier> permission nodes
limitTiers:
  - 1
  - 3
  - 5
  - 10

# Name of the default profile created on first join
defaultProfileName: "default"

# Global date format pattern
dateFormat: "yyyy-MM-dd HH:mm"

# Enable administrative alerts when players switch, create, or delete profiles
adminAlertsEnabled: true
```

### Configuration Options

#### Global Settings

| Option               | Type          | Default            | Description                                                                        |
|:---------------------|:--------------|:-------------------|:-----------------------------------------------------------------------------------|
| `debug`              | Boolean       | `false`            | Enable detailed debug logging in the server console.                               |
| `defaultProfileName` | String        | `default`          | Name of the initial profile created automatically when a player first joins.       |
| `dateFormat`         | String        | `yyyy-MM-dd HH:mm` | Date format used for displaying profile creation and last used timestamps.         |
| `adminAlertsEnabled` | Boolean       | `true`             | Broadcast profile actions (create, delete, switch) to administrators.              |
| `limitTiers`         | List<Integer> | `[1, 3, 5, 10]`    | Profile slot limit thresholds based on permission nodes (e.g. `oumprofile.max.5`). |

#### Switching Settings (`switching`)

| Option                  | Type    | Default | Description                                                                        |
|:------------------------|:--------|:--------|:-----------------------------------------------------------------------------------|
| `warmupEnabled`         | Boolean | `true`  | If true, players must stand still for a warmup duration before switching profiles. |
| `warmupSeconds`         | Integer | `5`     | Warmup countdown duration in seconds.                                              |
| `cancelOnMove`          | Boolean | `true`  | Cancel the switch warmup if the player moves.                                      |
| `cancelOnDamage`        | Boolean | `true`  | Cancel the switch warmup if the player takes damage.                               |
| `cancelInCombat`        | Boolean | `true`  | Cancel the switch warmup if the player is in combat.                               |
| `combatTagDuration`     | Integer | `10`    | Duration in seconds that a player remains tagged in combat.                        |
| `switchCooldownSeconds` | Integer | `10`    | Cooldown period in seconds before a player can switch profiles again.              |
| `saveLocation`          | Boolean | `false` | Save and restore player coordinates per-profile.                                   |
| `warmupTitleEnabled`    | Boolean | `true`  | Show title/subtitle countdown during warmup.                                       |

#### Storage Settings (`storage`)

| Option     | Type    | Default      | Description                                  |
|:-----------|:--------|:-------------|:---------------------------------------------|
| `type`     | String  | `sqlite`     | Database storage type (`sqlite` or `mysql`). |
| `host`     | String  | `localhost`  | Hostname of the MySQL database.              |
| `port`     | Integer | `3306`       | Port of the MySQL database.                  |
| `database` | String  | `oumprofile` | Name of the MySQL schema.                    |
| `username` | String  | `root`       | Username for MySQL database authentication.  |
| `password` | String  | `""`         | Password for MySQL database authentication.  |

#### Integrations Settings

| Option                     | Type         | Default                           | Description                                          |
|:---------------------------|:-------------|:----------------------------------|:-----------------------------------------------------|
| `luckperms.enabled`        | Boolean      | `true`                            | Synchronize LuckPerms permission groups per-profile. |
| `economy.enabled`          | Boolean      | `true`                            | Enable per-profile multi-currency balances.          |
| `economy.currencies`       | List<String> | `["vault", "playerpoints"]`       | Currencies synchronized per-profile.                 |
| `skills.mcmmoEnabled`      | Boolean      | `true`                            | Synchronize mcMMO level and XP per-profile.          |
| `skills.auraSkillsEnabled` | Boolean      | `true`                            | Synchronize AuraSkills level and XP per-profile.     |
| `skills.jobsEnabled`       | Boolean      | `true`                            | Synchronize JobsReborn job level and XP per-profile. |
| `statistics.enabled`       | Boolean      | `true`                            | Synchronize vanilla Minecraft statistics.            |
| `statistics.tracked`       | List<String> | `["MOB_KILLS", "DEATHS", "JUMP"]` | Vanilla statistics tracked.                          |

#### GUI Settings (`gui`)

| Option                    | Type         | Description                                                                                         |
|:--------------------------|:-------------|:----------------------------------------------------------------------------------------------------|
| `title`                   | String       | Title of the profile inventory menu (supports MiniMessage).                                         |
| `rows`                    | Integer      | Number of rows in the GUI grid (1-6).                                                               |
| `pattern`                 | List<String> | Character pattern defining the layout (e.g. `P` for profile items, `C` for creation button).        |
| `activeProfileMaterial`   | String       | Item material/texture for the currently active profile (supports `head:<Base64>` or `head:<Hash>`). |
| `inactiveProfileMaterial` | String       | Item material/texture for inactive profiles.                                                        |
| `emptySlotMaterial`       | String       | Item material/texture for unfilled profile slots.                                                   |

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

        // Programmatically create, delete, or switch profiles
        ProfileAPI.createProfile(player, "pvp");
        ProfileAPI.deleteProfile(player, "pvp");
        ProfileAPI.switchProfile(player, "pvp");

        // Manage switch warmup states
        boolean isWarmupActive = ProfileAPI.hasPendingWarmup(uuid);
        ProfileAPI.cancelWarmup(uuid);

        // Read or adjust profile balances
        double pvpBalance = ProfileAPI.getProfileBalance(uuid, "pvp");
        ProfileAPI.setProfileBalance(uuid, "pvp", 5000.0);

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