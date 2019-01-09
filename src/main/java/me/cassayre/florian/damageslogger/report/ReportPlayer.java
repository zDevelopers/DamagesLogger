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
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.Statistic;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

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

    private Map<Statistic, Integer> genericStatistics = new HashMap<>();
    private Map<Material, Integer> usedStatistics = new HashMap<>();
    private Map<Material, Integer> minedStatistics = new HashMap<>();
    private Map<Material, Integer> pickedUpStatistics = new HashMap<>();

    public ReportPlayer(final OfflinePlayer player)
    {
        this.uuid = player.getUniqueId();
        this.name = player.getName();
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
        final Player player = Bukkit.getPlayer(uuid);
        if (player == null) return false;

        genericStatistics.clear();
        usedStatistics.clear();
        minedStatistics.clear();
        pickedUpStatistics.clear();

        for (Statistic statistic : Statistic.values())
        {
            if (statistic.getType() == Statistic.Type.UNTYPED)
            {
                genericStatistics.put(statistic, player.getStatistic(statistic));
            }
            else
            {
                switch (statistic)
                {
                    case MINE_BLOCK:
                        collectSubStatistics(player, statistic, minedStatistics);
                        break;

                    case USE_ITEM:
                        collectSubStatistics(player, statistic, usedStatistics);
                        break;

                    default:
                        // This statistic appeared after 1.8 and is not available
                        // in the version this plugin is compiled for.
                        // The statistics will only be diffused in compatible versions.
                        // The PICKUP statistic is a sub-statistic of type ITEM.
                        if (statistic.name().equals("PICKUP"))
                        {
                            collectSubStatistics(player, statistic, pickedUpStatistics);
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
    private void collectSubStatistics(final Player player, final Statistic statistic, final Map<Material, Integer> collector)
    {
        for (Material material : Material.values())
        {
            try
            {
                final int stat = player.getStatistic(statistic, material);
                if (stat != 0) collector.put(material, stat);
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
        genericStatistics.forEach((statistic, value) -> {
            final String name;

            // We convert all keys names to 1.13+ IDs.
            // FIXME Magic values?
            switch (statistic) {
                case PLAY_ONE_TICK:
                    name = "play_one_minute"; break;
                case CAKE_SLICES_EATEN:
                    name = "eat_cake_slice"; break;
                case CAULDRON_FILLED:
                    name = "fill_cauldron"; break;
                case CAULDRON_USED:
                    name = "use_cauldron"; break;
                case ARMOR_CLEANED:
                    name = "clean_armor"; break;
                case BANNER_CLEANED:
                    name = "clean_banner"; break;
                case BREWINGSTAND_INTERACTION:
                    name = "interact_with_brewingstand"; break;
                case BEACON_INTERACTION:
                    name = "interact_with_beacon "; break;
                case DROPPER_INSPECTED:
                    name = "inspect_dropper"; break;
                case HOPPER_INSPECTED:
                    name = "inspect_hopper"; break;
                case DISPENSER_INSPECTED:
                    name = "inspect_dispenser"; break;
                case NOTEBLOCK_PLAYED:
                    name = "play_noteblock"; break;
                case NOTEBLOCK_TUNED:
                    name = "tune_noteblock"; break;
                case FLOWER_POTTED:
                    name = "pot_flower"; break;
                case TRAPPED_CHEST_TRIGGERED:
                    name = "trigger_trapped_chest"; break;
                case ENDERCHEST_OPENED:
                    name = "open_enderchest"; break;
                case ITEM_ENCHANTED:
                    name = "enchant_item"; break;
                case RECORD_PLAYED:
                    name = "play_record"; break;
                case FURNACE_INTERACTION:
                    name = "interact_with_furnace"; break;
                case CRAFTING_TABLE_INTERACTION:
                    name = "interact_with_crafting_table"; break;
                case CHEST_OPENED:
                    name = "open_chest"; break;

                default:
                    name = statistic.name().toLowerCase();
            }

            statisticsGeneric.addProperty("minecraft." + name, value);
        });

        statistics.add("generic", statisticsGeneric);
        statistics.add("used", toJson(usedStatistics));
        statistics.add("mined", toJson(minedStatistics));
        statistics.add("picked_up", toJson(pickedUpStatistics));

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
