# OFA-SK

**Build a Minecraft server with Skript, and get menus, player data, regions and cross-server
variables included.** No plugins to hunt down and no Java to write.

```
on ofa join:
    send "Welcome back, %player%!" to player
    send "Today: %{net::motd}%" to player
```

That `{net::motd}` is set once, on any of your servers, and every server sees it.

> [!IMPORTANT]
> **Early preview (0.1).** Today OFA-SK runs on Minestom servers that already include
> OneForAll. Dropping it into a plain skript-minestom server, as described below, arrives in
> **0.2**, along with the starter kit.

---

> ### 👋 New here? Pick your path
>
> | I am… | Go to |
> |---|---|
> | **Starting from nothing**: I just want a server running | [🚀 Starter kit](#-starter-kit-easiest) |
> | **Already running skript-minestom** | [📦 Add to your server](#-add-to-an-existing-skript-minestom-server) |
> | **Coming from Skript on Spigot/Paper** | [🔁 Coming from regular Skript](#-coming-from-regular-skript) first |
> | **Looking for syntax** | [📖 Cheat sheet](#-cheat-sheet) |

---

## ✨ What you get

| | | Status |
|---|---|---|
| 🧩 | **Menus**: GUIs with animated borders, click and close events | ✅ Ready |
| 📣 | **Events**: join, first join, chat, region enter/exit, death, status effects | ✅ Ready |
| 👤 | **Player data**: save anything per player, plus coins and tags | ✅ Ready |
| 🌐 | **Network variables**: `{net::...}` shared by every server | ✅ Ready |
| 🗺️ | **Regions**: protected areas you create in-game with a wand | 🚧 Next |
| ⚙️ | **Player settings**: toggles players flip in a `/settings` menu | 🗓️ Planned |
| 💰 | **Economy**: currencies, balances, `/pay` | 🗓️ Planned |
| ✨ | **Holograms and 3D models**: floating text, animated BDEngine models | 🗓️ Planned |

---

## 🚀 Starter kit (easiest)

> [!NOTE]
> The starter kit is coming with the first release. Until then, use
> [Add to an existing server](#-add-to-an-existing-skript-minestom-server).

One download with everything already in place: the server, OFA-SK, sensible configs and
example scripts.

1. **Install Java 25** ([Adoptium](https://adoptium.net/)). Not sure you have it? Run
   `java -version` and look for `25`.
2. **Download** `ofa-server-starter.zip` from [Releases](../../releases) and unzip it anywhere.
3. **Double-click** `start.bat` (Windows) or run `./start.sh` (Mac/Linux).
4. **Join** `localhost` in Minecraft.

You're in a working lobby with a server-selector menu, a welcome message and a spawn region.
Everything you see is a script in `Skript/scripts/`, so open one up and change it.

---

## 📦 Add to an existing skript-minestom server

You need **[skript-minestom](https://github.com/skript-minestom/skript-minestom) `1.0.0-alpha.47`
or newer**, already started at least once.

### Step 1: Drop the jar in

Download `ofa-sk.jar` from [Releases](../../releases) and put it in `Skript/addons/`:

```
my-server/
├── skript-minestom.jar
├── server.properties
└── Skript/
    ├── addons/
    │   └── ofa-sk.jar      ← put it here
    └── scripts/
```

### Step 2: Restart the server

The console should show:

```
[OFA-SK] OneForAll started
```

OFA-SK made a `config/` folder next to `Skript/`. You don't need to touch it yet.

### Step 3: Try it

Make `Skript/scripts/hello.sk`:

```
on ofa join:
    send "&aOFA-SK is working!" to player
```

Run `/sk reload hello`, rejoin, and you should see the message. That's the whole install.

---

## 🔁 Coming from regular Skript?

Most of what you know still works. Here's what's different:

| On Spigot/Paper | On skript-minestom |
|---|---|
| `plugins/Skript.jar` | Skript is **built into** the server jar, so there's nothing to install |
| Addons go in `plugins/` | Addons go in **`Skript/addons/`** |
| Scripts in `plugins/Skript/scripts/` | Scripts in **`Skript/scripts/`** |
| `server.properties` + `spigot.yml` + … | Just **`server.properties`** |
| Plugins for menus, regions, economy… | **OFA-SK** covers those |
| `/sk reload` | `/sk reload`, same as before |

**Why does OFA-SK syntax say `ofa`?** It marks the parts that come from OFA-SK, and it keeps
them from clashing with skript-minestom's own syntax. The rule is simple: `ofa` goes right before
the thing it owns.

```
set slot 13 of ofa menu {_m} to diamond     ← just like "slot 13 of player's inventory"
on ofa join:                                ← OFA-SK's join event
```

---

## 📖 Cheat sheet

<details open>
<summary><b>🧩 Menus</b></summary>

```
command /servers:
    trigger:
        set {_m} to a bordered 3 row ofa menu named "&8Pick a server"
        set border colour of ofa menu {_m} to "aqua"
        set slot 11 of ofa menu {_m} to diamond sword named "&bSurvival"
        set slot 15 of ofa menu {_m} to grass block named "&aCreative"
        fill empty slots of ofa menu {_m} with gray stained glass pane named " "
        open ofa menu {_m} for player

on ofa menu click:
    cancel event
    if clicked slot is 11:
        send "Sending you to Survival..." to player

on ofa menu close:
    send "Bye!" to player
```

The border animates on its own; you never write a loop for it.
</details>

<details open>
<summary><b>📣 Events</b></summary>

```
on ofa join:                     # profile is loaded, so player data is ready
on ofa first join:               # their very first time on your network
on ofa ready:                    # fully spawned: good moment for titles and menus
on ofa quit:

on ofa chat:
    if ofa chat message contains "discord":
        send "discord.gg/yourserver" to player
    set ofa chat format to "<gray><player>: <white><message>"

on ofa region enter of "spawn":
    send "Welcome to %ofa region%" to player
on ofa region exit:
    cancel event                 # stops them from leaving

on ofa death:
    if ofa killer is set:
        send "You killed %player%!" to ofa killer

on ofa status effect apply of "ofa:combat_tag":
    send "You're in combat!" to player

on ofa environmental damage:     # falling, void, fire, drowning...
    if ofa damage cause is "fall":
        cancel event
```

In `ofa chat format`, `<player>` and `<message>` are filled in for you. Players can't sneak colour
codes into their own message.
</details>

<details>
<summary><b>👤 Player data</b></summary>

```
on load:
    register ofa whole number field "lobby:visits"

on ofa join:
    add 1 to ofa field "lobby:visits" of player
    send "Visit number %ofa field "lobby:visits" of player%" to player

    if player has ofa tag "vip":
        send "Hello VIP!" to player

add ofa tag "vip" to player
add 100 to ofa field "coins" of player     # built-in fields work too: coins, gems, kills, deaths
```

Fields are saved for you, follow players between servers, and load when they join.
</details>

<details>
<summary><b>🧭 Moving players around</b></summary>

```
teleport player to ofa world "arena"       # to that world's spawn
send player to ofa server "survival"       # to another server on your network (needs a proxy)

apply ofa status effect "ofa:combat_tag" to player for 15 seconds
if player has ofa status effect "ofa:combat_tag":
    send "You can't do that in combat!" to player
```
</details>

<details>
<summary><b>🌐 Network variables</b></summary>

```
set {net::motd} to "Double XP weekend!"
add 1 to {net::total joins}
```

Start the name with `net::` and every server shares it. More in [Network variables](#-network-variables).
</details>

---

## ⚙️ Turning things on and off

Everything works out of the box. When you want to change something, the files are in `config/`:

| File | What it's for |
|---|---|
| `config/oneforall.yml` | Switch features and modules on or off |
| `config/ofask/worlds.yml` | Worlds to load when the server starts |
| `config/database.yml` | Leave as-is for one server; see [Running a network](#-running-a-network-optional) |

<details>
<summary>Example <code>oneforall.yml</code></summary>

```yaml
features:
  profiles: true      # player data
  holograms: true
  settings: true      # the /settings menu

modules:              # every module in modules/ runs unless you turn it off here
  ofa-economy: false
```
</details>

<details>
<summary>Example <code>worlds.yml</code></summary>

```yaml
default: spawn
worlds:
  spawn:
    polar: worlds/spawn.polar
    spawn: [0.5, 65, 0.5, 0, 0]   # x, y, z, yaw, pitch
  arena:
    polar: worlds/arena.polar
```

Worlds listed here load before anything else, so NPCs and regions can find them by name.
</details>

### Modules: extra features as drop-in jars

Want regions with a selection wand, NPCs, cosmetics or teams? Drop the module's jar into
`modules/` and restart. Each one makes its own config in `config/<module>/`. The list is in
the [OneForAll README](https://github.com/oneforall-mc/oneforall#modules).

---

## 🌐 Network variables

Start a variable's name with `net::` and every server on your network shares it. There's
nothing to set up and nothing to learn, because it's still a normal Skript variable:

```
set {net::motd} to "Double XP weekend!"
add 1 to {net::total joins}
delete {net::event::*}
```

- **One server?** `{net::...}` is just a normal saved variable. No database needed.
- **A network?** It's shared automatically once your servers share a database (see below).
- **Adding numbers is safe.** If two servers `add 1 to {net::total joins}` at the same moment,
  both count.

> [!TIP]
> For things that belong to **one player** (their visits, their last server), use
> `ofa field "..." of player`. Each server loads every `{net::}` variable when it starts, while
> fields only load when that player joins.

<details>
<summary>How fast is it?</summary>

| | |
|---|---|
| Reading `{net::x}` | Instant, like any variable |
| Seen on other servers | Usually within a tick |
| If the connection drops | Servers catch up within 30 seconds |
| Two servers **set** it at once | Every server ends up with the same value: whichever was saved last |
| Two servers **add** to it at once | Both adds count |

Saving never happens on the main thread, so your scripts never wait on the database.
</details>

---

## 🖧 Running a network (optional)

> [!NOTE]
> **Skip this if you have one server.** Out of the box, OFA-SK saves everything to files next to
> your server. No database to install, nothing running in the background.

When you grow to several servers that should share players' data and `{net::}` variables, they
need one shared place to keep it: a Postgres database and a Redis server.

1. Run one **Postgres** and one **Redis** that all your servers can reach.
2. On **every** server, set `config/database.yml`:
   ```yaml
   storage: shared
   postgres: { host: db.internal, database: oneforall, user: oneforall, password: changeme }
   redis:    { host: redis.internal }
   ```
3. Restart. That's it; your scripts don't change.

Moving an existing server from `local` to `shared` is covered in the
[OneForAll storage guide](https://github.com/oneforall-mc/oneforall#storage).

---

## 🧰 Troubleshooting

| What you see | Fix |
|---|---|
| No `[OFA-SK]` line when the server starts | The jar isn't in `Skript/addons/`, or skript-minestom is older than `alpha.47` |
| `Can't understand this event: 'on ofa join'` | OFA-SK didn't start. Scroll up to the first `[OFA-SK]` error |
| `{net::x}` doesn't reach other servers | The servers aren't on `storage: shared` with the same Postgres and Redis |
| NPCs from a module spawn in the wrong world | Add that world to `config/ofask/worlds.yml` |

Still stuck? [Open an issue](../../issues) with your console log from startup.

---

<details>
<summary><b>For developers</b></summary>

**Already run OneForAll inside your own Minestom server?** Use `ofa-sk-embedded.jar`. It adds the
syntax only and leaves startup to your server.

**skript-reflect** can reach OneForAll's Java API (`net.honormc.oneforall`) for anything OFA-SK
doesn't expose yet. **skript-bdengine** and OFA-SK's 3D models can both show BDEngine models; use
one or the other for any single model.

**Building:**

```bash
git clone https://github.com/oneforall-mc/ofa-sk
cd ofa-sk
./gradlew build
```

Jars land in `build/libs/`.
</details>

## License

MIT
