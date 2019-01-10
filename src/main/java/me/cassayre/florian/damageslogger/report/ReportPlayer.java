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

import com.google.gson.JsonObject;
import fr.zcraft.zlib.tools.PluginLogger;
import fr.zcraft.zlib.tools.items.ItemUtils;
import fr.zcraft.zlib.tools.reflection.NMSException;
import me.cassayre.florian.damageslogger.ReportsUtils;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ReportPlayer
{
    private final UUID uuid;
    private final String name;

    private String tagLine;
    private String tagLineSecondary;
    private String tagLineDetails;

    private boolean hasPreviousStatistics = false;

    private Map<Statistic, Integer> previousGenericStatistics = new HashMap<>();
    private Map<Material, Integer> previousUsedStatistics = new HashMap<>();
    private Map<Material, Integer> previousMinedStatistics = new HashMap<>();
    private Map<Material, Integer> previousPickedUpStatistics = new HashMap<>();

    private Map<Statistic, Integer> genericStatistics = new HashMap<>();
    private Map<Material, Integer> usedStatistics = new HashMap<>();
    private Map<Material, Integer> minedStatistics = new HashMap<>();
    private Map<Material, Integer> pickedUpStatistics = new HashMap<>();

    public ReportPlayer(final OfflinePlayer player)
    {
        this.uuid = player.getUniqueId();
        this.name = player.getName();

        collectPreviousStatistics();
    }

    public UUID getUniqueId()
    {
        return uuid;
    }

    public String getName()
    {
        return name;
    }

    public boolean hasPreviousStatistics()
    {
        return hasPreviousStatistics;
    }

    /**
     * Sets the tag line displayed in the player's list in the reports page.
     *
     * In the following example, let's say we want to tag the player as a white
     * werewolf in a Werewolf UHC game.
     *
     * @param tagLine This is the main attribute. It could contains « White Werewolf ».
     * @param tagLineSecondary This is a secondary attribute displayed smaller, under the main one.
     *                         It could contains « Against the village ».
     * @param tagLineDetails This contains details displayed in a tooltip, so this can be longer.
     *                       It could contains a description of the role. Line returns ({@code \n})
     *                       will be preserved.
     */
    public void setTagLine(final String tagLine, final String tagLineSecondary, final String tagLineDetails)
    {
        this.tagLine = tagLine;
        this.tagLineSecondary = tagLineSecondary;
        this.tagLineDetails = tagLineDetails;
    }

    /**
     * Collects and stores the statistics for the given player.
     *
     * @return {@code true} if statistics could be collected (i.e. player was online).
     */
    public boolean collectStatistics()
    {
        return collectStatistics(
                genericStatistics, usedStatistics, minedStatistics, pickedUpStatistics,
                previousGenericStatistics, previousUsedStatistics, previousMinedStatistics, previousPickedUpStatistics
        );
    }

    /**
     * Collects and stores the statistics of the given player before the record.
     *
     * This allows to in fine only collect & store statistics earned during the record.
     * This method is automatically called when this class is instantiated. But if you want
     * another point of reference for statistics, you can re-call it anytime to set a new
     * previous-statistics-reference.
     *
     * @return {@code true} if statistics could be collected (i.e. player was online).
     */
    public boolean collectPreviousStatistics()
    {
        final boolean success = collectStatistics(previousGenericStatistics, previousUsedStatistics, previousMinedStatistics, previousPickedUpStatistics);

        if (!hasPreviousStatistics && success) hasPreviousStatistics = true;

        return success;
    }

    /**
     * If you don't want to collect only the statistics during the record, call this to erase
     * all collected previous statistic. As a result, statistics will be global statistics for the
     * player, including statistics before the record.
     */
    public void resetPreviousStatistics()
    {
        previousGenericStatistics.clear();
        previousUsedStatistics.clear();
        previousMinedStatistics.clear();
        previousPickedUpStatistics.clear();

        hasPreviousStatistics = false;
    }

    private boolean collectStatistics(
            final Map<Statistic, Integer> generic,
            final Map<Material, Integer> used,
            final Map<Material, Integer> mined,
            final Map<Material, Integer> pickedUp)
    {
        return collectStatistics(
                generic, used, mined, pickedUp,
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                Collections.emptyMap()
        );
    }

    private boolean collectStatistics(
            final Map<Statistic, Integer> generic,
            final Map<Material, Integer> used,
            final Map<Material, Integer> mined,
            final Map<Material, Integer> pickedUp,

            final Map<Statistic, Integer> previousGeneric,
            final Map<Material, Integer> previousUsed,
            final Map<Material, Integer> previousMined,
            final Map<Material, Integer> previousPickedUp)
    {
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null) return false;

        generic.clear();
        used.clear();
        mined.clear();
        pickedUp.clear();

        for (Statistic statistic : Statistic.values())
        {
            if (statistic.getType() == Statistic.Type.UNTYPED)
            {
                final int stat = player.getStatistic(statistic) - previousGeneric.getOrDefault(statistic, 0);
                if (stat > 0) generic.put(statistic, stat);
            }
            else
            {
                switch (statistic)
                {
                    case MINE_BLOCK:
                        collectSubStatistics(player, statistic, mined, previousMined);
                        break;

                    case USE_ITEM:
                        collectSubStatistics(player, statistic, used, previousUsed);
                        break;

                    default:
                        // This statistic appeared after 1.8 and is not available
                        // in the version this plugin is compiled for.
                        // The statistics will only be diffused in compatible versions.
                        // The PICKUP statistic is a sub-statistic of type ITEM.
                        if (statistic.name().equals("PICKUP"))
                        {
                            collectSubStatistics(player, statistic, pickedUp, previousPickedUp);
                        }
                }
            }
        }

        return true;
    }

    /**
     * Collects all values for a ITEM or BLOCK sub-statistic into a map.
     *
     * @param player The player to collect statistics for.
     * @param statistic The statistic to collect.
     * @param collector The map were statistics will be stored.
     */
    private void collectSubStatistics(final Player player, final Statistic statistic, final Map<Material, Integer> collector, final Map<Material, Integer> previous)
    {
        for (Material material : Material.values())
        {
            try
            {
                final int stat = player.getStatistic(statistic, material) - previous.getOrDefault(material, 0);
                if (stat > 0) collector.put(material, stat);
            }
            catch (IllegalArgumentException ignored)
            {
                // This material does not have a corresponding statistic (e.g. item for mined statistic).
            }
        }
    }

    public JsonObject toJSON() {
        final JsonObject json = new JsonObject();

        json.addProperty("uuid", uuid.toString());
        json.addProperty("name", name);

        json.addProperty("tag_line", tagLine);
        json.addProperty("tag_line_secondary", tagLineSecondary);
        json.addProperty("tag_line_details", tagLineDetails);

        final JsonObject statistics = new JsonObject();

        final JsonObject statisticsGeneric = new JsonObject();
        genericStatistics.forEach((statistic, value) -> statisticsGeneric.addProperty(ReportsUtils.getStatisticID(statistic), value));

        statistics.add("generic", statisticsGeneric);
        statistics.add("used", toJson(usedStatistics));
        statistics.add("mined", toJson(minedStatistics));
        statistics.add("picked_up", toJson(pickedUpStatistics));

        json.add("statistics", statistics);

        return json;
    }

    private JsonObject toJson(final Map<Material, Integer> statistics) {
        final JsonObject json = new JsonObject();

        statistics.forEach((material, value) -> {
            try
            {
                final String materialID = ItemUtils.getMinecraftId(new ItemStack(material));
                if (materialID != null) json.addProperty(materialID, value);
            }
            catch (NMSException e)
            {
                PluginLogger.error("Unable to export statistic for {0}: unable to retrieve Minecraft key.", e, material);
            }
        });

        return json;
    }
}
