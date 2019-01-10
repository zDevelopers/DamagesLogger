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
package me.cassayre.florian.damageslogger;

import com.google.common.collect.ImmutableMap;
import org.bukkit.Statistic;
import org.bukkit.enchantments.Enchantment;

import java.util.Map;

public final class ReportsUtils
{
    private static final Map<String, String> ENCHANTS_BUKKIT_TO_MINECRAFT;

    static {
        ENCHANTS_BUKKIT_TO_MINECRAFT = ImmutableMap.<String, String>builder()

                // 1.8 and before
                .put("PROTECTION_ENVIRONMENTAL", "protection")
                .put("PROTECTION_FIRE", "fire_protection")
                .put("PROTECTION_FALL", "feather_falling")
                .put("PROTECTION_EXPLOSIONS", "blast_protection")
                .put("PROTECTION_PROJECTILE", "projectile_protection")
                .put("THORNS", "thorns")
                .put("OXYGEN", "respiration")
                .put("DEPTH_STRIDER", "depth_strider")
                .put("WATER_WORKER", "aqua_affinity")
                .put("DAMAGE_ALL", "sharpness")
                .put("DAMAGE_UNDEAD", "smite")
                .put("DAMAGE_ARTHROPODS", "bane_of_arthropods")
                .put("KNOCKBACK", "knockback")
                .put("FIRE_ASPECT", "fire_aspect")
                .put("LOOT_BONUS_MOBS", "looting")
                .put("DIG_SPEED", "efficiency")
                .put("SILK_TOUCH", "silk_touch")
                .put("DURABILITY", "unbreaking")
                .put("LOOT_BONUS_BLOCKS", "fortune")
                .put("ARROW_DAMAGE", "power")
                .put("ARROW_KNOCKBACK", "punch")
                .put("ARROW_FIRE", "flame")
                .put("ARROW_INFINITE", "infinity")
                .put("LUCK", "luck_of_the_sea")
                .put("LURE", "lure")

                // 1.9
                .put("FROST_WALKER", "frost_walker")
                .put("MENDING", "mending")

                // 1.11
                .put("BINDING_CURSE", "binding_curse")
                .put("VANISHING_CURSE", "vanishing_curse")

                // 1.11.1
                .put("SWEEPING_EDGE", "sweeping")

                // 1.13
                .put("IMPALING", "impaling")
                .put("RIPTIDE", "riptide")
                .put("LOYALTY", "loyalty")
                .put("CHANNELING", "channeling")

                // 1.14
                // FIXME Bukkit names are subject to change.
                .put("MULTISHOT", "multishot")
                .put("PIERCING", "piercing")
                .put("QUICK_CHARGE", "quick_charge")

                .build();
    }

    /**
     * Returns the Minecraft 1.13+ ID for the given statistic.
     *
     * @param statistic The statistic.
     * @return The 1.13+ ID (including “minecraft.”).
     */
    public static String getStatisticID(final Statistic statistic)
    {
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

        return "minecraft." + name;
    }

    /**
     * Returns the Minecraft 1.13+ ID for the given enchant.
     *
     * @param enchantment The enchant.
     * @return The 1.13+ ID.
     */
    public static String getEnchantmentID(final Enchantment enchantment)
    {
        return ENCHANTS_BUKKIT_TO_MINECRAFT.getOrDefault(enchantment.getName(), enchantment.getName());
    }
}
