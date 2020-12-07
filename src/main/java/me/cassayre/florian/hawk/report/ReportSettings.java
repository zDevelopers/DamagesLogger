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

package me.cassayre.florian.hawk.report;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import fr.zcraft.zlib.tools.items.ItemUtils;
import fr.zcraft.zlib.tools.reflection.NMSException;
import me.cassayre.florian.hawk.ReportsUtils;
import org.bukkit.Material;
import org.bukkit.Statistic;
import org.bukkit.inventory.ItemStack;

import java.util.*;

public class ReportSettings {
    private final Report report;

    private boolean date = true;
    private boolean playersCount = true;
    private boolean winners = true;

    private boolean summary = true;
    private boolean summaryHistory = true;
    private boolean summaryPlayers = true;
    private boolean summaryTeams = true;

    private boolean damages = true;
    private boolean damagesPerPlayers = true;
    private boolean damagesPerTeams = true;
    private boolean damagesFromEnvironment = true;
    private boolean damagesDisplayKiller = true;

    private boolean players = true;
    private boolean playersPlayTime = true;

    private boolean playersGlobalStatistics = true;
    private final Set<Statistic> playersStatisticsWhitelist = new HashSet<>();
    private final Set<Statistic> playersStatisticsHighlight = new HashSet<>();

    private boolean playersUsedStatistics = false;
    private final Set<Material> playersUsedStatisticsWhitelist = new HashSet<>();
    private final Set<Material> playersUsedStatisticsHighlight = new HashSet<>();

    private boolean playersMinedStatistics = true;
    private final Set<Material> playersMinedStatisticsWhitelist = new HashSet<>();
    private final Set<Material> playersMinedStatisticsHighlight = new HashSet<>();

    private boolean playersPickedUpStatistics = true;
    private final Set<Material> playersPickedUpStatisticsWhitelist = new HashSet<>();
    private final Set<Material> playersPickedUpStatisticsHighlight = new HashSet<>();

    private String generatorName = null;
    private String generatorURL = null;


    public ReportSettings(Report report) {
        this.report = report;
    }

    /**
     * @param date {@code true} to display the match date on the report page.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings displayDate(final boolean date) {
        this.date = date;
        return this;
    }

    /**
     * @param playersCount {@code true} to display the players count on the report page.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings displayPlayersCount(final boolean playersCount) {
        this.playersCount = playersCount;
        return this;
    }

    /**
     * @param winners {@code true} to display the winners on the report page. If not manually provided, they will be calculated automatically.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings displayWinners(final boolean winners) {
        this.winners = winners;
        return this;
    }

    /**
     * Enables the summary tab on the report page.
     *
     * @param history {@code true} to enable the history (“timeline”) section of this tab, displaying all recorded events in order.
     * @param players {@code true} to display the players' list in this tab.
     * @param teams   {@code true} to display the teams in this tab. Only displayed if players are also enabled.
     *                If disabled players will be listed as there were no team, even if there are teams.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings enableSummary(final boolean history, final boolean players, final boolean teams) {
        this.summary = true;
        this.summaryHistory = history;
        this.summaryPlayers = players;
        this.summaryTeams = teams;

        return this;
    }

    /**
     * Disables the summary tab on the report page.
     *
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings disableSummary() {
        this.summary = false;
        return this;
    }

    /**
     * Enables the damages tab on the report page.
     *
     * @param perPlayers      {@code true} to display a per-player summary.
     * @param perTeams        {@code true} to display a per-team summary.
     * @param fromEnvironment {@code true} to display a summary of environmental damages.
     * @param withKiller      {@code true} to display the killer of each player in the per-player summary.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings enableDamages(final boolean perPlayers, final boolean perTeams, final boolean fromEnvironment,
                                        final boolean withKiller) {
        this.damages = true;
        this.damagesPerPlayers = perPlayers;
        this.damagesPerTeams = perTeams;
        this.damagesFromEnvironment = fromEnvironment;
        this.damagesDisplayKiller = withKiller;

        return this;
    }

    /**
     * Disables the damages tab on the report page.
     *
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings disableDamages() {
        this.damages = false;
        return this;
    }

    /**
     * Enables the players tab on the report page, displaying statistics on every players and aggregated global statistics.
     * <p>
     * SOme statistics can be highlighted (or whitelisted), see other settings methods for that.
     *
     * @param withPlayTime         Displays the play time for each player.
     * @param withGlobalStatistics Displays the global statistics of each player (plus an aggregation).
     * @param withUsed             Displays used items statistics for each player (plus an aggregation).
     * @param withMined            Displays mined blocks statistics for each player (plus an aggregation).
     * @param withPickedUp         Displays picked-up items statistics for each player (plus an aggregation).
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings enablePlayers(final boolean withPlayTime, final boolean withGlobalStatistics,
                                        final boolean withUsed, final boolean withMined, final boolean withPickedUp) {
        this.players = true;
        this.playersPlayTime = withPlayTime;
        this.playersGlobalStatistics = withGlobalStatistics;
        this.playersUsedStatistics = withUsed;
        this.playersMinedStatistics = withMined;
        this.playersPickedUpStatistics = withPickedUp;

        return this;
    }

    /**
     * Disables the players tab on the report page.
     *
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings disablePlayers() {
        this.players = false;
        return this;
    }

    /**
     * Only display these global statistics in the players tab.
     *
     * @param whitelisted The whitelist.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withTheseInGlobalStatisticsWhitelist(final Statistic... whitelisted) {
        playersStatisticsWhitelist.addAll(Arrays.asList(whitelisted));
        return this;
    }

    /**
     * Only display these used items in the players tab.
     *
     * @param whitelisted The whitelist.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withTheseInUsedStatisticsWhitelist(final Material... whitelisted) {
        playersUsedStatisticsWhitelist.addAll(Arrays.asList(whitelisted));
        return this;
    }

    /**
     * Only display these mined blocks in the players tab.
     *
     * @param whitelisted The whitelist.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withTheseInMinedStatisticsWhitelist(final Material... whitelisted) {
        playersMinedStatisticsWhitelist.addAll(Arrays.asList(whitelisted));
        return this;
    }

    /**
     * Only display these picked-up items in the players tab.
     *
     * @param whitelisted The whitelist.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withTheseInPickedUpStatisticsWhitelist(final Material... whitelisted) {
        playersPickedUpStatisticsWhitelist.addAll(Arrays.asList(whitelisted));
        return this;
    }

    /**
     * Only display these global statistics in the players tab.
     *
     * @param whitelisted The whitelist.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withTheseInGlobalStatisticsWhitelist(final Collection<Statistic> whitelisted) {
        playersStatisticsWhitelist.addAll(whitelisted);
        return this;
    }

    /**
     * Only display these used items in the players tab.
     *
     * @param whitelisted The whitelist.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withTheseInUsedStatisticsWhitelist(final Collection<Material> whitelisted) {
        playersUsedStatisticsWhitelist.addAll(whitelisted);
        return this;
    }

    /**
     * Only display these mined blocks in the players tab.
     *
     * @param whitelisted The whitelist.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withTheseInMinedStatisticsWhitelist(final Collection<Material> whitelisted) {
        playersMinedStatisticsWhitelist.addAll(whitelisted);
        return this;
    }

    /**
     * Only display these picked-up items in the players tab.
     *
     * @param whitelisted The whitelist.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withTheseInPickedUpStatisticsWhitelist(final Collection<Material> whitelisted) {
        playersPickedUpStatisticsWhitelist.addAll(whitelisted);
        return this;
    }

    /**
     * Displays all collected statistics in the players tab.
     *
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withoutGlobalStatisticsWhitelist() {
        playersStatisticsWhitelist.clear();
        return this;
    }

    /**
     * Displays all collected used items in the players tab.
     *
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withoutUsedStatisticsWhitelist() {
        playersUsedStatisticsWhitelist.clear();
        return this;
    }

    /**
     * Displays all collected mined blocks in the players tab.
     *
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withoutMinedStatisticsWhitelist() {
        playersMinedStatisticsWhitelist.clear();
        return this;
    }

    /**
     * Displays all collected picked-up items in the players tab.
     *
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withoutPickedUpStatisticsWhitelist() {
        playersPickedUpStatisticsWhitelist.clear();
        return this;
    }

    /**
     * Highlights these global statistics in the players tab.
     * <p>
     * Other statistics will still be accessible, but hidden by default. Because
     * there is usually a lot of statistics collected, this can help make the
     * report clearer.
     *
     * @param highlighted The highlighted statistics.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings highlightingTheseStatistics(final Statistic... highlighted) {
        playersStatisticsHighlight.addAll(Arrays.asList(highlighted));
        return this;
    }

    /**
     * Highlights these used items in the players tab.
     * <p>
     * Other items will still be accessible, but hidden by default. Because
     * there is usually a lot of statistics collected, this can help make the
     * report clearer.
     *
     * @param highlighted The highlighted items.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings highlightingTheseUsedItems(final Material... highlighted) {
        playersUsedStatisticsHighlight.addAll(Arrays.asList(highlighted));
        return this;
    }

    /**
     * Highlights these mined blocks in the players tab.
     * <p>
     * Other blocks will still be accessible, but hidden by default. Because
     * there is usually a lot of statistics collected, this can help make the
     * report clearer.
     *
     * @param highlighted The highlighted blocks.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings highlightingTheseMinedBlocks(final Material... highlighted) {
        playersMinedStatisticsHighlight.addAll(Arrays.asList(highlighted));
        return this;
    }

    /**
     * Highlights these picked-up items in the players tab.
     * <p>
     * Other items will still be accessible, but hidden by default. Because
     * there is usually a lot of statistics collected, this can help make the
     * report clearer.
     *
     * @param highlighted The highlighted items.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings highlightingThesePickedUpItems(final Material... highlighted) {
        playersPickedUpStatisticsHighlight.addAll(Arrays.asList(highlighted));
        return this;
    }

    /**
     * Highlights these global statistics in the players tab.
     * <p>
     * Other statistics will still be accessible, but hidden by default. Because
     * there is usually a lot of statistics collected, this can help make the
     * report clearer.
     *
     * @param highlighted The highlighted statistics.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings highlightingTheseStatistics(final Collection<Statistic> highlighted) {
        playersStatisticsHighlight.addAll(highlighted);
        return this;
    }

    /**
     * Highlights these used items in the players tab.
     * <p>
     * Other items will still be accessible, but hidden by default. Because
     * there is usually a lot of statistics collected, this can help make the
     * report clearer.
     *
     * @param highlighted The highlighted items.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings highlightingTheseUsedItems(final Collection<Material> highlighted) {
        playersUsedStatisticsHighlight.addAll(highlighted);
        return this;
    }

    /**
     * Highlights these mined blocks in the players tab.
     * <p>
     * Other blocks will still be accessible, but hidden by default. Because
     * there is usually a lot of statistics collected, this can help make the
     * report clearer.
     *
     * @param highlighted The highlighted blocks.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings highlightingTheseMinedBlocks(final Collection<Material> highlighted) {
        playersMinedStatisticsHighlight.addAll(highlighted);
        return this;
    }

    /**
     * Highlights these picked-up items in the players tab.
     * <p>
     * Other items will still be accessible, but hidden by default. Because
     * there is usually a lot of statistics collected, this can help make the
     * report clearer.
     *
     * @param highlighted The highlighted items.
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings highlightingThesePickedUpItems(final Collection<Material> highlighted) {
        playersPickedUpStatisticsHighlight.addAll(highlighted);
        return this;
    }

    /**
     * Displays all global statistics equally without hiding anything.
     *
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withoutHighlightingAnyGlobalStatistic() {
        playersStatisticsHighlight.clear();
        return this;
    }

    /**
     * Displays all used items equally without hiding anything.
     *
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withoutHighlightingAnyUsedItem() {
        playersUsedStatisticsHighlight.clear();
        return this;
    }

    /**
     * Displays all mined blocks equally without hiding anything.
     *
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withoutHighlightingAnyMinedBlock() {
        playersMinedStatisticsHighlight.clear();
        return this;
    }

    /**
     * Displays all picked-up items equally without hiding anything.
     *
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withoutHighlightingAnyPickedUpItem() {
        playersPickedUpStatisticsHighlight.clear();
        return this;
    }

    /**
     * Adds the name of the plugin, or other generator, responsible for this
     * report's generation. It will be credited in the report's footer, with a
     * link if one is provided and non-null.
     *
     * @param name The generator's name.
     * @param url  The generator's URL (may be {@code null}).
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withGenerator(final String name, final String url) {
        this.generatorName = name;
        this.generatorURL = url;

        return this;
    }

    /**
     * Removes any credit for the generator. Credits for this tool (the reports
     * plugin/API + website) will still be displayed.
     *
     * @return The current {@link ReportSettings} instance, for method chaining. Use {@link #done()} to end.
     */
    public ReportSettings withoutGenerator() {
        this.generatorName = null;
        this.generatorURL = null;

        return this;
    }

    /**
     * @return The {@link Report} instance these settings are linked to, to come
     * back to it without breaking the methods chaining.
     */
    public Report done() {
        return report;
    }


    /**
     * Raw access for advanced manipulation.
     */
    public Set<Statistic> getPlayersStatisticsWhitelist() {
        return playersStatisticsWhitelist;
    }

    /**
     * Raw access for advanced manipulation.
     */
    public Set<Statistic> getPlayersStatisticsHighlight() {
        return playersStatisticsHighlight;
    }

    /**
     * Raw access for advanced manipulation.
     */
    public Set<Material> getPlayersUsedStatisticsWhitelist() {
        return playersUsedStatisticsWhitelist;
    }

    /**
     * Raw access for advanced manipulation.
     */
    public Set<Material> getPlayersUsedStatisticsHighlight() {
        return playersUsedStatisticsHighlight;
    }

    /**
     * Raw access for advanced manipulation.
     */
    public Set<Material> getPlayersMinedStatisticsWhitelist() {
        return playersMinedStatisticsWhitelist;
    }

    /**
     * Raw access for advanced manipulation.
     */
    public Set<Material> getPlayersMinedStatisticsHighlight() {
        return playersMinedStatisticsHighlight;
    }

    /**
     * Raw access for advanced manipulation.
     */
    public Set<Material> getPlayersPickedUpStatisticsWhitelist() {
        return playersPickedUpStatisticsWhitelist;
    }

    /**
     * Raw access for advanced manipulation.
     */
    public Set<Material> getPlayersPickedUpStatisticsHighlight() {
        return playersPickedUpStatisticsHighlight;
    }


    public JsonObject toJSON() {
        final JsonObject json = new JsonObject();

        json.addProperty("date", date);
        json.addProperty("players_count", playersCount);
        json.addProperty("winners", winners);

        final JsonObject summary = new JsonObject();

        summary.addProperty("enabled", this.summary);
        summary.addProperty("history", this.summaryHistory);
        summary.addProperty("players", this.summaryPlayers);
        summary.addProperty("teams", this.summaryTeams);

        final JsonObject damages = new JsonObject();

        damages.addProperty("enabled", this.damages);
        damages.addProperty("damages_per_players", this.damagesPerPlayers);
        damages.addProperty("damages_per_teams", this.damagesPerTeams);
        damages.addProperty("damages_from_environment", this.damagesFromEnvironment);
        damages.addProperty("display_killer", this.damagesDisplayKiller);

        final JsonObject players = new JsonObject();

        players.addProperty("enabled", this.players);
        players.addProperty("play_time", this.playersPlayTime);

        players.addProperty("global_statistics", this.playersGlobalStatistics);
        players.add("statistics_whitelist", toJSONStatistics(playersStatisticsWhitelist));
        players.add("statistics_highlight", toJSONStatistics(playersStatisticsHighlight));

        players.addProperty("used", this.playersUsedStatistics);
        players.add("used_whitelist", toJSONMaterials(playersUsedStatisticsWhitelist));
        players.add("used_highlight", toJSONMaterials(playersUsedStatisticsHighlight));

        players.addProperty("mined", this.playersMinedStatistics);
        players.add("mined_whitelist", toJSONMaterials(playersMinedStatisticsWhitelist));
        players.add("mined_highlight", toJSONMaterials(playersMinedStatisticsHighlight));

        players.addProperty("picked_up", this.playersPickedUpStatistics);
        players.add("picked_up_whitelist", toJSONMaterials(playersPickedUpStatisticsWhitelist));
        players.add("picked_up_highlight", toJSONMaterials(playersPickedUpStatisticsHighlight));

        json.add("summary", summary);
        json.add("damages", damages);
        json.add("players", players);

        if (generatorName != null) {
            final JsonObject generator = new JsonObject();

            generator.addProperty("name", generatorName);
            generator.addProperty("link", generatorURL);

            json.add("generator", generator);
        }

        return json;
    }

    private JsonArray toJSONStatistics(final Set<Statistic> statistics) {
        final JsonArray jsonStatistics = new JsonArray();

        statistics.stream()
                .map(ReportsUtils::getStatisticID)
                .forEach(jsonStatistics::add);

        return jsonStatistics;
    }

    private JsonArray toJSONMaterials(final Set<Material> materials) {
        final JsonArray jsonMaterials = new JsonArray();

        materials.stream()
                .map(material -> {
                    try {
                        return ItemUtils.getMinecraftId(new ItemStack(material));
                    }
                    catch (NMSException e) {
                        return null;
                    }
                })
                .filter(Objects::nonNull)
                .forEach(jsonMaterials::add);

        return jsonMaterials;
    }
}
