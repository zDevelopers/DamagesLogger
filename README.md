# Hawk

_Formerly DamagesLogger_

Hawk is a Bukkit/Spigot plugin made to track damages, heals, players statistics, events… from Minecraft (mini-)games to
export them into a visual and sharable report on a web page. The project is made of two main components: this one, the
Bukkit plugin, made to collect all data and send it to the web, and the web part, written in Rust, processing and
displaying the collected data into a beautiful web page.

Hawk can either be used manually, by installing it as a plugin into your server, or by other plugins to generate reports
for their games.

Hawk requires CraftBukkit or Spigot 1.8.3 or later. It also supports Minecraft 1.13 through Spigot's compatibility
layer, but all new damages sources will fall under the `UNKNOWN` category; official 1.13 support will be added soon.

Java 8+ is required.

_**Warning**: the Hawk reports website is not yet available. Soon!_


## Standalone use (for server owners)

If you wan to use Hawk on your server by itself to generate reports for your games, install Hawk like any other plugin
in your Bukkit/Spigot 1.8.3+ server. You'll be provided these commands to create reports.

### `/hawk start [name]`

The `/hawk start` command starts tracking for a new report. It will track damages, heals, deaths, and all players'
statistics earned while the recording was active (ignoring previous statistics). A few options allows you to tweak
the record, by adding them after `start` (space-separated).

- `--track-new-players`: if present, players joining the server while the recording is active will be automatically
tracked. Else, only players logged in when the record is started are tracked.
- `--stop-track-on-death`: if present, when a player die, the tracking will cease for him/her. It will be in the record
but no information will be collected after its death.
- `--stop-track-on-disconnection`: if present, when a player logs out, the tracking will stop for him/her.

You can also, before or after these flags, write a title for your report. The title can be formatted using
[Minecraft formatting codes](https://minecraft.gamepedia.com/Formatting_codes#Color_codes) with `&` instead of `§`. If
no title is provided, « Minecraft Report » will be used.

Hawk supports teams in the generated reports. If used standalone, it will loads teams from the default Scoreboard.

### `/hawk stop`

This command stops and save the record. It will save the record into the `plugins/Hawk/reports` directory as JSON, and
publish it onto the website; you'll be given the URL seconds after the command is executed. Easy!

If you only want to keep the JSON report, call the command with `--no-publish`.

While tracking, Hawk will make regular backups in the `plugins/Hawk/reports/backups` directory, so if your server
crashes for some reasons before you call the `/hawk stop` command, you'll still have the latest backup. By default,
backups are made every minute, if there were changes since the last backup. Backups are not deleted when you call
`/hawk stop`. Backups are made from another thread, so they will not alter your server's performances.


## Usage from other plugins (for developers)

If you want to create hassle-free reports for your games, use Hawk! We made all the hard stuff for you; you only have
to plug Hawk in your plugin and use its very simple API to create, save and publish reports.

### Step 1: link

To include Hawk into your project, you can either shade it into your plugin, using Maven, or depend on the plugin
being installed on the server.

The shading solution makes your plugin a little (little) bit bigger, and requires Maven or equivalent, but it is really
simpler for your users as they'll only have to install your plugin and boom, it works.

In both cases, add Hawk to your `pom.xml` (or equivalent if you're using Gradle):

```xml
<repositories>
    <!-- [...] -->
    <repository>
        <id>zDevelopers</id>
        <url>http://maven.carrade.eu/artifactory/snapshots</url>
    </repository>
</repositories>

<dependencies>
    <!-- [...] -->
    <dependency>
        <groupId>me.cassayre.florian</groupId>
        <artifactId>Hawk</artifactId>
        <version>1.0-SNAPSHOT</version>
    </dependency>
</dependencies>
```

#### Install through shading

You first have to shade the library into your `pom.xml`:

```xml
<build>
    <plugins>
        <plugin>
            <groupId>org.apache.maven.plugins</groupId>
            <artifactId>maven-shade-plugin</artifactId>
            <version>2.4</version>
            <configuration>
                <minimizeJar>true</minimizeJar>
                <artifactSet>
                    <includes>
                        <include>me.cassayre.florian:Hawk</include>
                    </includes>
                </artifactSet>
                <relocations>
                    <relocation>
                        <pattern>me.cassayre.florian.hawk</pattern>
                        <shadedPattern>eu.carrade.amaury.UHCReloaded.hawk</shadedPattern>
                    </relocation>
                </relocations>
            </configuration>
            <executions>
                <execution>
                    <phase>package</phase>
                    <goals>
                        <goal>shade</goal>
                    </goals>
                </execution>
            </executions>
        </plugin>
    </plugins>
</build>
```

(If you're already shading some other libraries, add this one to the `<relocations>` & `<includes>` sections.)

Then, you'll have to initialize Hawk like so, as example when you plugin is enabled:

```java
import me.cassayre.florian.hawk.ReportsManager;

// plugin being your plugin's instance (`this`, if this is called from your plugin's `onEnable` method).
ReportsManager.init(plugin);
```

You're ready to go. Skip to the second step.


#### Install through plugin dependency

Depend (or soft-depend) on `Hawk` in your `plugin.yml`, and ask your users to install Hawk into their servers.

```yaml
depend:
  - Hawk
 
# or

softdepend:
  - Hawk
```

Hawk will be automatically initialized. You can go to step 2.


### Step 2: ignite

Hawk is feed with data. And data is collected into reports. You'll have to create a report to collect data. It's as
simple as instantiating a class.

```java
import me.cassayre.florian.hawk.report.Report;

final Report report = new Report()
        .selfRegister();
```

The `selfRegister` thing registers the report into Hawk manager to enable auto-track and other features.

The whole API is chained: every methods returns the current instance (with a few obvious exceptions like getters).
You can discover the methods available in the documentation, but a typical use will be like this:

```java
final Report report = new Report()
        .selfRegister()
        
        // Enables damages & heals automatic tracking, so you'll have nothing
        // to do to record them (it can be tricky so it's a good thing!).
        // Auto-track is enabled by default so you'll not technically have to
        // call this. It's only for demonstration purposes.
        .autoTrack(true)
        
        // Do you want to automatically stop track on player death?
        // Else you can always start and stop tracking for any player by calling a method.
        // Enabled by default, too.
        .stopTrackOnDeath(true)
        
        // We'll talk about events later, but this will add “death” events on the timeline
        // automatically.
        // Enabled by default, too.
        .addDefaultEvents(true)
        
        // You can use Minecraft formatting codes in these and they will be correctly converted
        // on the web page.
        // By default, the title is « Minecraft Report ».
        .title(ChatColor.GREEN + "The title of your report")
        
        // The report's web page can be configured. See the `ReportSettings` documentation for
        // the full reference (there's a lot more than that).
        .settings()
            .displayDate(false)
            .displayWinners(true)
            .done()
        
        // This one is important! Only players registered on the report will be tracked and listed
        // on the web page. This method registers players from an iterable, an array or an `OfflinePlayer`
        // alone. It will automatically track the players (if auto-track is enabled).
        .registerPlayers(players)
        
        
        // For lasy developers :p
        .registerOnlinePlayers();
```

All these options (including auto-track) can be changed at any time.

#### Tracking and un-tracking players

If you use auto-track (and you should!), _tracked_ players will have their statistics and damages/heal automatically
saved into the report. You can track (or re-track) new players using `report.track(player)` (`player` being an
`OfflinePlayer`) and un-track players using `report.untrack(player)`.

When you untrack a player, if it is online, its statistics will be collected.

#### Adding teams to the report

Reports can display teams in the players list, and group damages reports by team (soon). For that you'll have to
register the teams into the report. A team contains a name, optionally a color, and a list of players.

Teams are represented using `ReportTeams` instances. You can build them directly using the constructors, if you
use a custom team implementation, or create them from a
[scoreboard `Team`](https://hub.spigotmc.org/javadocs/bukkit/index.html?org/bukkit/scoreboard/Team.html) using
`ReportTeam.fromScoreboardTeam(team)`. In this case, the color will be extracted from the prefix's last colors.

When you have an instance, register it into the report like so:

```java
report
    .registerTeam(team)
    .registerTeam(new ReportTeam("My Team", ChatColor.BLUE, players));
    // Register as many teams as required.
```

You can also automatically register all teams from the main scoreboard:

```java
report.registerTeamsFromScoreboard();
```

…or from a custom one.

```java
report.registerTeamsFromScoreboard(scoreboard);
```

#### Adding events

Hawk reports feature a timeline of game events in the main section of the report. It's useful to get an idea of
what happened into the game at a glance. By default (but you can disable that), all (tracked) players deaths are
added to the timeline. But you can add your own events too!

Events feature a type (their color), an icon, a title and a small description. They were designed to mimic the
advancements design. The icon can either be a player head, a Minecraft icon or an external icon from an URL.
External icons should be 32px × 32px. The built-in icons list
[is available here](https://github.com/zDevelopers/Hawk-GUI/blob/master/static/scss/reports/minecraft/_icons.scss). You
should avoid icons ending with `-large` or `-small`, or starting with `gui`, as they will badly render.

To create events, you can create instances directly or (recommended) use the shortcut static methods. Then register
them into the report. Events will be by default dated from the moment the instance is created, but if you absolutely
need to create ante- or post-dated events, use tne `ReportEvent`'s constructor accepting a `date` argument.

```java
report
    // Event with a player's head as icon
    .record(ReportEvent.withPlayer(
        /* title */  "Notch disappeared",
        /* desc. */  "Nobody know where he is",
        /* player */ Bukkit.getOfflinePlayer("Notch")
    ))
    
    // Evet with a built-in icon (without description)
    .record(ReportEvent.withIcon(
        "A wild event appears",
        "block-glazed-terracotta-purple"
    ))
    
    // Event with a custom remote icon
    .record(ReportEvent.withURL(
        "The event is still there",
        "In the wild…",
        "https://your-website.eu/icon-32x32.png"
    ));
```

#### Manually recording damages & heals

You can manually record damages and heals, either because you disabled auto-track or you want to register custom
ones in the report. You can do so at any time using `report.record(damage or heal)`, `damage` and `heal` being instances
of the `DamageRecord` and `HealRecord` respectively. A special damage and heal type « `COMMAND` » is available if you
need that.


### Step 3: launch!

Okay so you have your reports build and with a lot of data. Now, how to save and publish them?

First thing, ongoing reports (all of them) are automatically saved into a backup directory every few minutes
(configurable in the `ReportsManager`). If the server crashes for some reason, your users will be covered. The backup
directory is a sub-directory `backup` of the save directory, this one being by default the `reports` directory of
your plugin (if used shaded) or Hawk (if used as a standard dependency).

To save the report as JSON, use the `report.save` method. It accepts two callbacks if you want to act on success or
error; the first one gives you the `File` where the report was saved; the second one the `Throwable` exception if an
error occurred. If you don't care about the success or error of the operation, set the callback(s) to `null`.

If you want to save the report to a specific location, you can use `report.save(file, onSuccess, onError)`.

We have to use callbacks because all Hawk I/O (backups, saves and publication) is executed on another thread for
performances.

Tu publish the report into the website, it's very similar but with the `publish` method. The first argument is
a callback giving you the report's URL, as `URI`, called if everything succeeded. The second one is called if an
error happens (network, invalid report for some reason) and gives you an exception (that can basically be an
`IOException` or an `InvalidReportException`). Same, pass `null` if you don't care about errors.

To wrap everything up into an example:

```java
report.save(
    file -> /* do something with the saved File */,
    error -> /* handle the error */
);

report.publish(
    uri -> /* do something with the URI (broadcast uri.toString() ?) */,
    error -> /* handle the error */
);
```

Yes, it's as simple as that. You don't have to bother with network, HTTP requests or these things, it's already
handled under the hood. Enjoy your reports!