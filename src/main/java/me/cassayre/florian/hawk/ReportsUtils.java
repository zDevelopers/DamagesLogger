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

package me.cassayre.florian.hawk;

import com.google.common.collect.ImmutableMap;
import java.util.Map;
import org.bukkit.Statistic;
import org.bukkit.enchantments.Enchantment;

public final class ReportsUtils {
    private static final Map<String, String> ENCHANTS_BUKKIT_TO_MINECRAFT;
    private static final Map<Statistic, String> STATISTICS_BUKKIT_TO_MINECRAFT;

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


        // Statistics for whose the enum name differs from th 1.13+ ID in uppercase.
        STATISTICS_BUKKIT_TO_MINECRAFT = ImmutableMap.<Statistic, String>builder()

                .put(Statistic.PLAY_ONE_MINUTE,
                        "play_one_minute")  // Warning: misleading Minecraft name, as it's actually ticks.
                .put(Statistic.CAKE_SLICES_EATEN, "eat_cake_slice")
                .put(Statistic.CAULDRON_FILLED, "fill_cauldron")
                .put(Statistic.CAULDRON_USED, "use_cauldron")
                .put(Statistic.ARMOR_CLEANED, "clean_armor")
                .put(Statistic.BANNER_CLEANED, "clean_banner")
                .put(Statistic.BREWINGSTAND_INTERACTION, "interact_with_brewingstand")
                .put(Statistic.BEACON_INTERACTION, "interact_with_beacon")
                .put(Statistic.DROPPER_INSPECTED, "inspect_dropper")
                .put(Statistic.DISPENSER_INSPECTED, "inspect_dispenser")
                .put(Statistic.NOTEBLOCK_PLAYED, "play_noteblock")
                .put(Statistic.NOTEBLOCK_TUNED, "tune_noteblock")
                .put(Statistic.FLOWER_POTTED, "pot_flower")
                .put(Statistic.TRAPPED_CHEST_TRIGGERED, "trigger_trapped_chest")
                .put(Statistic.ENDERCHEST_OPENED, "open_enderchest")
                .put(Statistic.ITEM_ENCHANTED, "enchant_item")
                .put(Statistic.RECORD_PLAYED, "play_record")
                .put(Statistic.FURNACE_INTERACTION, "interact_with_furnace")
                .put(Statistic.CRAFTING_TABLE_INTERACTION, "interact_with_crafting_table")
                .put(Statistic.CHEST_OPENED, "open_chest")

                .build();
    }

    /**
     * Returns the Minecraft 1.13+ ID for the given statistic.
     *
     * @param statistic The statistic.
     * @return The 1.13+ ID (including “minecraft.”).
     */
    public static String getStatisticID(final Statistic statistic) {
        return "minecraft." + STATISTICS_BUKKIT_TO_MINECRAFT.getOrDefault(statistic, statistic.name().toLowerCase());
    }

    /**
     * Returns the Minecraft 1.13+ ID for the given enchant.
     *
     * @param enchantment The enchant.
     * @return The 1.13+ ID.
     */
    public static String getEnchantmentID(final Enchantment enchantment) {
        return ENCHANTS_BUKKIT_TO_MINECRAFT.getOrDefault(enchantment.getName(), enchantment.getName().toLowerCase());
    }
}
