/*
 * Plugin UHCReloaded : Alliances
 *
 * Copyright ou © ou Copr. Amaury Carrade (2016)
 * Idées et réflexions : Alexandre Prokopowicz, Amaury Carrade, "Vayan".
 *
 * Ce logiciel est régi par la licence CeCILL soumise au droit français et
 * respectant les principes de diffusion des logiciels libres. Vous pouvez
 * utiliser, modifier et/ou redistribuer ce programme sous les conditions
 * de la licence CeCILL telle que diffusée par le CEA, le CNRS et l'INRIA
 * sur le site "http://www.cecill.info".
 *
 * En contrepartie de l'accessibilité au code source et des droits de copie,
 * de modification et de redistribution accordés par cette licence, il n'est
 * offert aux utilisateurs qu'une garantie limitée.  Pour les mêmes raisons,
 * seule une responsabilité restreinte pèse sur l'auteur du programme,  le
 * titulaire des droits patrimoniaux et les concédants successifs.
 *
 * A cet égard  l'attention de l'utilisateur est attirée sur les risques
 * associés au chargement,  à l'utilisation,  à la modification et/ou au
 * développement et à la reproduction du logiciel par l'utilisateur étant
 * donné sa spécificité de logiciel libre, qui peut le rendre complexe à
 * manipuler et qui le réserve donc à des développeurs et des professionnels
 * avertis possédant  des  connaissances  informatiques approfondies.  Les
 * utilisateurs sont donc invités à charger  et  tester  l'adéquation  du
 * logiciel à leurs besoins dans des conditions permettant d'assurer la
 * sécurité de leurs systèmes et ou de leurs données et, plus généralement,
 * à l'utiliser et l'exploiter dans les mêmes conditions de sécurité.
 *
 * Le fait que vous puissiez accéder à cet en-tête signifie que vous avez
 * pris connaissance de la licence CeCILL, et que vous en avez accepté les
 * termes.
 */
package me.cassayre.florian.damageslogger.report;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.zcraft.zlib.tools.PluginLogger;
import me.cassayre.florian.damageslogger.report.record.DamageRecord;
import me.cassayre.florian.damageslogger.report.record.HealRecord;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Scoreboard;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;


/**
 * A report.
 *
 * Saves data about a Minecraft game (players, teams, damages, heals, events) and exports them into
 * a JSON format that can be used to build an user-friendly report.
 */
public class Report
{
    private final static long COOLDOWN_DAMAGES_PVP = 10L * 1000;
    private final static long COOLDOWN_DAMAGES_PVE = 10L * 1000;
    private final static long COOLDOWN_HEALS = 3L * 1000;

    /**
     * An unique ID for this report.
     */
    private UUID uuid = UUID.randomUUID();

    /**
     * This report's title.
     */
    private String title = "Minecraft Report";

    /**
     * The startDate where this report starts.
     */
    private long startDate = System.currentTimeMillis();

    /**
     * If true, damages & heal will be automatically tracked for this report.
     * Else damage & heal events will have to be manually supplied to the report.
     */
    private boolean autoTrack = true;

    /**
     * If auto-track is enabled, and this option is true, players joining the server
     * will be tracked automatically.
     */
    private boolean autoTrackNewPlayers = true;

    /**
     * If auto-track is enabled and this option is true, if a player is added to the report
     * while offline, its previous statistics will be automatically collected as soon as he or she
     * come back online.
     */
    private boolean autoCollectPreviousStatistics = true;

    /**
     * If auto-track is enabled, and this option is true, tracking will stop
     * for each player when they die.
     */
    private boolean stopTrackOnDeath = true;

    /**
     * If auto-track is enabled, and this option is true, tracking will stop
     * for each player when they disconnect.
     */
    private boolean stopTrackOnDisconnection = false;

    /**
     * If auto-track is enabled, and this option is true, events for players
     * deaths will be automatically added.
     */
    private boolean addDefaultEvents = true;

    /*
     * The settings of this report.
     */
    // private ReportSettings settings = new ReportSettings();

    /**
     * The players in this report.
     */
    private Map<UUID, ReportPlayer> players = new HashMap<>();

    /**
     * The players tracked by the plugin for this report. Dead players may
     * be removed from this tracked list, as example.
     */
    private Set<UUID> trackedPlayers = new HashSet<>();

    /**
     * The teams considered in this report. May be empty or not containing all player.
     */
    private Set<ReportTeam> teams = new HashSet<>();

    /**
     * The recorded history of damages per player.
     */
    private Map<UUID, LinkedList<DamageRecord>> damages = new HashMap<>();

    /**
     * The recorded history of heals per player.
     */
    private Map<UUID, LinkedList<HealRecord>> heals = new HashMap<>();

    /**
     * The special events of the game, recorded there and displayed on the report's timeline.
     */
    private Set<ReportEvent> events = new HashSet<>();


    /**
     * Sets the title of the report.
     *
     * @param title The report's title.
     * @return Current instance, for method chaining.
     */
    public Report title(final String title) { this.title = title; return this; }

    /**
     * Sets the start date of this report. All events/heals/damages/etc. dates will
     * be displayed relative to this date.
     *
     * If not modified, this will be the instant where the instance was created.
     *
     * @param startDate The report's start date (as a milli-timestamp).
     * @return Current instance, for method chaining.
     */
    public Report starts(final long startDate) { this.startDate = startDate; return this; }

    /**
     * Sets now as the start date of this report. All events/heals/damages/etc.
     * dates will be displayed relative to this date.
     *
     * If not modified, this will be the instant where the instance was created.
     *
     * @return Current instance, for method chaining.
     */
    public Report startsNow() { this.startDate = System.currentTimeMillis(); return this; }

    /**
     * Enables or disables auto-track of all players damages & heals. If disabled,
     * you'll have to manually register damages & heals for them to be recorded.
     *
     * Enabled by default.
     *
     * @param autoTrack {@code true} to enable auto-track.
     * @return Current instance, for method chaining.
     */
    public Report autoTrack(final boolean autoTrack) { this.autoTrack = autoTrack; return this; }

    /**
     * If auto-track is enabled, and this option is true, players joining the server
     * will be tracked automatically.
     *
     * Enabled by default.
     *
     * @param autoTrackNewPlayers {@code true} to enable auto-track of new players.
     * @return Current instance, for method chaining.
     */
    public Report autoTrackNewPlayers(final boolean autoTrackNewPlayers) { this.autoTrackNewPlayers = autoTrackNewPlayers; return this; }

    /**
     * If auto-track is enabled and this option is true, if a player is added to the report
     * while offline, its previous statistics will be automatically collected as soon as he or she
     * come back online.
     *
     * Enabled by default.
     *
     * @param autoCollectPreviousStatistics {@code true} to automatically collect previous
     *                                      statistics when a registered player without them
     *                                      logs in.
     * @return Current instance, for method chaining.
     */
    public Report autoCollectPreviousStatistics(final boolean autoCollectPreviousStatistics) { this.autoCollectPreviousStatistics = autoCollectPreviousStatistics; return this; }

    /**
     * If auto-track is enabled, and this option is true, tracking will stop
     * for each player when they die.
     *
     * Enabled by default.
     *
     * @param stopTrackOnDeath {@code true} to stop players' tracking when they die.
     * @return Current instance, for method chaining.
     */
    public Report stopTrackOnDeath(final boolean stopTrackOnDeath) { this.stopTrackOnDeath = stopTrackOnDeath; return this; }

    /**
     * If auto-track is enabled, and this option is true, tracking will stop
     * for each player when they disconnect.
     *
     * Disabled by default.
     *
     * @param stopTrackOnDisconnection {@code true} to stop players' tracking when they disconnect.
     * @return Current instance, for method chaining.
     */
    public Report stopTrackOnDisconnection(final boolean stopTrackOnDisconnection) { this.stopTrackOnDisconnection = stopTrackOnDisconnection; return this; }

    /**
     * If auto-track is enabled, and this option is true, events for players deaths
     * will be automatically added.
     *
     * Enabled by default.
     *
     * @param addDefaultEvents {@code true} to automatically add default events.
     * @return Current instance, for method chaining.
     */
    public Report addDefaultEvents(final boolean addDefaultEvents) { this.addDefaultEvents = addDefaultEvents; return this; }

    /**
     * Regenerates the UUID for this report. Useful if you want to save multiple versions of this
     * report under multiple URLs, as the webservice will update a report if sent with the same UUID.
     *
     * @return Current instance, for method chaining.
     */
    public Report regenerateUUID() { this.uuid = UUID.randomUUID(); return this; }


    /**
     * Registers one or more players to be in the report.
     * If auto-track is enabled, this will also automatically track the players damages & heals.
     *
     * @param players The players to add to the report.
     * @return Current instance, for method chaining.
     */
    public Report registerPlayers(final OfflinePlayer... players)
    {
        return registerPlayers(Arrays.asList((OfflinePlayer[]) players));
    }

    /**
     * Registers one or more players to be in the report.
     * If auto-track is enabled, this will also automatically track the players damages & heals.
     *
     * @param players The players to add to the report.
     * @return Current instance, for method chaining.
     */
    public Report registerPlayers(final Iterable<OfflinePlayer> players)
    {
        players.forEach(player -> {
            this.players.put(player.getUniqueId(), new ReportPlayer(player));
            trackPlayer(player);
        });

        return this;
    }

    /**
     * Registers all online players to be in the report.
     * If auto-track is enabled, this will also automatically track the players damages & heals.
     *
     * @return Current instance, for method chaining.
     */
    public Report registerOnlinePlayers()
    {
        return registerPlayers(new HashSet<>(Bukkit.getOnlinePlayers()));
    }


    /**
     * Registers a player to be tracked if auto-track is enabled.
     *
     * @param player The player to track.
     * @return Current instance, for method chaining.
     */
    public Report trackPlayer(OfflinePlayer player)
    {
        ensurePlayer(player);
        trackedPlayers.add(player.getUniqueId());
        PluginLogger.info("Now tracking {0}", player.getName());

        return this;
    }

    /**
     * Unregisters a player to be tracked if auto-track is enabled.
     *
     * After this method call, no event will be automatically recorded for this player.
     * You'll still be able to records heals or damages manually for this player.
     *
     * @param player The player to track.
     * @return Current instance, for method chaining.
     */
    public Report untrackPlayer(final OfflinePlayer player)
    {
        ensurePlayer(player);
        trackedPlayers.remove(player.getUniqueId());

        if (player.isOnline())
        {
            players.get(player.getUniqueId()).collectStatistics();
        }

        return this;
    }


    /**
     * Registers a team into this report.
     *
     * Teams registered will be displayed in the report' summary page, and all
     * players referenced in the report page will be colored (with a border or
     * on hover) according to their team.
     *
     * @param team The team to register.
     * @return Current instance, for method chaining.
     *
     * @see #registerTeamsFromScoreboard(Scoreboard) Shortcut to register all teams already in a given scoreboard.
     * @see #registerTeamsFromScoreboard() Shortcut to register all teams already in the main scoreboard.
     */
    public Report registerTeam(final ReportTeam team)
    {
        teams.add(team);
        return this;
    }

    /**
     * Registers all teams in the main scoreboard into this report.
     *
     * Teams registered will be displayed in the report' summary page, and all
     * players referenced in the report page will be colored (with a border or
     * on hover) according to their team.
     *
     * @return Current instance, for method chaining.
     *
     * @see #registerTeamsFromScoreboard(Scoreboard) Shortcut to register all teams already in a given scoreboard.
     */
    public Report registerTeamsFromScoreboard()
    {
        return registerTeamsFromScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    /**
     * Registers all teams in the given scoreboard into this report.
     *
     * Teams registered will be displayed in the report' summary page, and all
     * players referenced in the report page will be colored (with a border or
     * on hover) according to their team.
     *
     * @return Current instance, for method chaining.
     *
     * @see #registerTeamsFromScoreboard() Shortcut to register all teams already in the main scoreboard.
     */
    public Report registerTeamsFromScoreboard(Scoreboard scoreboard)
    {
        scoreboard.getTeams().forEach(team -> teams.add(ReportTeam.fromScoreboardTeam(team)));
        ensurePlayersInTeams();
        return this;
    }

    /**
     * Unregisters all the teams.
     * @return Current instance, for method chaining.
     */
    public Report resetTeams()
    {
        teams.clear();
        return this;
    }


    /**
     * Records a damage. The damage will be merged if there is a similar damage recorded just before.
     *
     * @param record The record.
     * @return Current instance, for method chaining.
     */
    public Report record(final DamageRecord record)
    {
        ensurePlayer(record.getPlayer());

        final LinkedList<DamageRecord> records = damages.computeIfAbsent(record.getPlayer().getUniqueId(), uuid -> new LinkedList<>());

        // We group consecutive similar damages together.
        if (!records.isEmpty())
        {
            final DamageRecord latestRecord = records.getLast();

            if (latestRecord.similarTo(record) && latestRecord.inCooldown(latestRecord.getDamageType() == DamageRecord.DamageType.PLAYER ? COOLDOWN_DAMAGES_PVP : COOLDOWN_DAMAGES_PVE))
            {
                latestRecord.addPoints(record.getPoints(), record.isLethal() || latestRecord.isLethal());
                PluginLogger.info(" → Adding +{0} points to {1} damage", record.getPoints(), record.getDamageType());
                return this;
            }
        }

        PluginLogger.info("Recording {0}", record);

        records.addLast(record.clone());
        return this;
    }

    /**
     * Records a heal. The heal will be merged if there is a similar damage recorded just before.
     *
     * @param record The record.
     * @return Current instance, for method chaining.
     */
    public Report record(final HealRecord record)
    {
        ensurePlayer(record.getPlayer());

        final LinkedList<HealRecord> records = heals.computeIfAbsent(record.getPlayer().getUniqueId(), uuid -> new LinkedList<>());

        // We group consecutive similar heals together.
        if (!records.isEmpty())
        {
            final HealRecord latestRecord = records.getLast();

            if (latestRecord.similarTo(record) && latestRecord.inCooldown(COOLDOWN_HEALS))
            {
                latestRecord.addPoints(record.getPoints());
                PluginLogger.info(" → Adding +{0} points to {1} heal", record.getPoints(), record.getHealingType());
                return this;
            }
        }

        PluginLogger.info("Recording {0}", record);

        records.addLast(record.clone());
        return this;
    }

    /**
     * Records an event.
     *
     * @param event The event to record.
     * @return Current instance, for method chaining.
     */
    public Report record(final ReportEvent event)
    {
        events.add(event);
        return this;
    }


    public UUID getUUID()
    {
        return uuid;
    }

    public String getTitle()
    {
        return title;
    }

    public long getStartDate()
    {
        return startDate;
    }

    public boolean isAutoTracked()
    {
        return autoTrack;
    }

    public boolean isAutoTrackingNewPlayers()
    {
        return autoTrackNewPlayers;
    }

    public boolean isAutoCollectingPreviousStatistics()
    {
        return autoCollectPreviousStatistics;
    }

    public boolean isStoppingTrackOnDeath()
    {
        return stopTrackOnDeath;
    }

    public boolean isStoppingTrackOnDisconnection()
    {
        return stopTrackOnDisconnection;
    }

    public boolean isAddingDefaultEvents()
    {
        return addDefaultEvents;
    }

    public Set<ReportPlayer> getPlayers()
    {
        return Collections.unmodifiableSet(new HashSet<>(players.values()));
    }

    public Set<UUID> getTrackedPlayers()
    {
        return Collections.unmodifiableSet(trackedPlayers);
    }

    public boolean isTracked(final OfflinePlayer player)
    {
        return trackedPlayers.contains(player.getUniqueId());
    }

    public boolean isTracked(final UUID playerId)
    {
        return trackedPlayers.contains(playerId);
    }

    public Set<ReportTeam> getTeams()
    {
        return Collections.unmodifiableSet(teams);
    }

    public Set<DamageRecord> getDamages()
    {
        return damages.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public Set<HealRecord> getHeals()
    {
        return heals.values().stream().flatMap(Collection::stream).collect(Collectors.toSet());
    }

    public Set<ReportEvent> getEvents()
    {
        return Collections.unmodifiableSet(events);
    }

    /**
     * Ensures any player mentioned in a damage/heal/etc. is registered into the players list.
     * This will not add players to the tracking list.
     *
     * @param player The player.
     */
    private void ensurePlayer(final OfflinePlayer player)
    {
        if (!players.containsKey(player.getUniqueId()))
        {
            players.put(player.getUniqueId(), new ReportPlayer(player));
        }
    }

    /**
     * Ensures all players mentioned in teams are registered into the players list.
     * This will not add players to the tracking list.
     */
    private void ensurePlayersInTeams()
    {
        teams.stream().flatMap(team -> team.getPlayers().stream()).map(Bukkit::getOfflinePlayer).filter(Objects::nonNull).forEach(this::ensurePlayer);
    }


    /**
     * Exports this report into a valid JSON to be sent to the Minecraft Reports web service.
     *
     * @return A JSON export.
     */
    public JsonObject toJSON()
    {
        final JsonObject json = new JsonObject();

        json.addProperty("match_uuid", uuid.toString());
        json.addProperty("title", title);
        json.addProperty("date", Instant.ofEpochMilli(startDate).atZone(TimeZone.getDefault().toZoneId()).toOffsetDateTime().toString());
        // json.add("settings", settings.toJSON());

        final JsonArray players = new JsonArray();
        this.players.values().forEach(reportPlayer -> players.add(reportPlayer.toJSON()));

        final JsonArray teams = new JsonArray();
        this.teams.forEach(reportTeam -> teams.add(reportTeam.toJSON()));

        final JsonArray damages = new JsonArray();
        this.damages.values().stream().flatMap(Collection::stream).forEach(damage -> damages.add(damage.toJSON()));

        final JsonArray heals = new JsonArray();
        this.heals.values().stream().flatMap(Collection::stream).forEach(heal -> heals.add(heal.toJSON()));

        final JsonArray events = new JsonArray();
        this.events.forEach(reportEvent -> events.add(reportEvent.toJSON()));

        json.add("players", players);
        json.add("teams", teams);
        json.add("damages", damages);
        json.add("heals", heals);
        json.add("events", events);

        return json;
    }
}
