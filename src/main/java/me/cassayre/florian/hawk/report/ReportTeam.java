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
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.scoreboard.Team;

public class ReportTeam {
    private final String name;
    private final ChatColor color;
    private final Set<UUID> players;

    /**
     * Creates a team with a name, a color and some players.
     *
     * @param name    The team's name.
     * @param color   The team's color.
     * @param players The players.
     */
    public ReportTeam(final String name, final ChatColor color, final OfflinePlayer... players) {
        this.name = name;
        this.color = normalizeColor(color);
        this.players = Arrays.stream(players).map(OfflinePlayer::getUniqueId).collect(Collectors.toSet());
    }

    /**
     * Creates a team with a name, a color and some players.
     *
     * @param name    The team's name.
     * @param color   The team's color.
     * @param players The players.
     */
    public ReportTeam(final String name, final ChatColor color, final Iterable<OfflinePlayer> players) {
        this.name = name;
        this.color = normalizeColor(color);
        this.players = StreamSupport.stream(players.spliterator(), false).map(OfflinePlayer::getUniqueId)
                .collect(Collectors.toSet());
    }

    /**
     * Creates a  uncolored team with a name and some players.
     *
     * @param name    The team's name.
     * @param players The players.
     */
    public ReportTeam(final String name, final OfflinePlayer... players) {
        this(name, null, players);
    }

    /**
     * Creates a  uncolored team with a name and some players.
     *
     * @param name    The team's name.
     * @param players The players.
     */
    public ReportTeam(final String name, final Iterable<OfflinePlayer> players) {
        this(name, null, players);
    }

    private static ChatColor normalizeColor(final ChatColor color) {
        return color != null ? (color.isFormat() ? ChatColor.BLACK : color) : null;
    }

    /**
     * Converts a Scoreboard team to a Hawk team. All entries from the team will be matched against known players,
     * and matches will be added to the Hawk team.
     *
     * <p>The team color will be extracted from the team name formatting (if any).</p>
     *
     * @param team The scoreboard team.
     * @return A corresponding Hawk team.
     */
    public static ReportTeam fromScoreboardTeam(final Team team) {
        final String lastColors = ChatColor.getLastColors(team.getPrefix()).substring(1);

        return new ReportTeam(
                team.getName(),
                lastColors.length() > 0 ? ChatColor.getByChar(lastColors) : null,

                team.getEntries().stream()
                        .map(Bukkit::matchPlayer)
                        .filter(matches -> !matches.isEmpty())
                        .map(matches -> matches.get(0))
                        .collect(Collectors.toList())
        );
    }

    public String getName() {
        return name;
    }

    public ChatColor getColor() {
        return color;
    }

    public Set<UUID> getPlayers() {
        return Collections.unmodifiableSet(players);
    }

    public JsonObject toJSON() {
        final JsonObject json = new JsonObject();

        json.addProperty("name", name);
        if (color != null) {
            json.addProperty("color", color.name());
        }

        final JsonArray players = new JsonArray();
        this.players.forEach(player -> players.add(player.toString()));

        json.add("players", players);

        return json;
    }
}
