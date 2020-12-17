package me.cassayre.florian.hawk.listeners;

import fr.zcraft.quartzlib.tools.reflection.Reflection;
import java.lang.reflect.InvocationTargetException;
import java.util.List;
import me.cassayre.florian.hawk.ReportsManager;
import me.cassayre.florian.hawk.report.record.HealRecord;
import me.cassayre.florian.hawk.report.record.HealRecord.HealingType;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.potion.PotionType;

public class PlayerHealsListener implements Listener {
    private final ReportsManager manager;

    private Class<?> TIPPED_ARROW_CLASS;

    public PlayerHealsListener(final ReportsManager manager) {
        this.manager = manager;

        // Tipped arrows were not available in Minecraft 1.8
        try {
            TIPPED_ARROW_CLASS = Class.forName("org.bukkit.entity.TippedArrow");
        }
        catch (ClassNotFoundException e) {
            TIPPED_ARROW_CLASS = null;
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityRegainHealth(EntityRegainHealthEvent e) {
        if (e.getEntity() instanceof Player) {
            final Player player = (Player) e.getEntity();
            final HealingType healingType;

            switch (e.getRegainReason()) {
                case REGEN:
                case SATIATED:
                    healingType = HealingType.NATURAL;
                    break;

                default:
                    healingType = manager._getLastHealingType(player);
            }

            final HealRecord record = new HealRecord(player, e.getAmount(), healingType);

            manager.getTrackedReportsFor(player).forEach(report -> report.record(record));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerItemConsume(PlayerItemConsumeEvent e) {
        final Material material = e.getItem().getType();
        final boolean isNotchApple = e.getItem().getDurability() == 1; // FIXME Will break with 1.13

        final HealingType healingType;

        if (material == Material.GOLDEN_APPLE) {
            healingType = isNotchApple ? HealingType.NOTCH_APPLE : HealingType.GOLDEN_APPLE;
        } else if (material == Material.POTION) {
            healingType = HealingType.HEALING_POTION;
        } else {
            healingType = HealingType.UNKNOWN;
        }

        manager._setLastHealingType(e.getPlayer(), healingType);
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerShootByPositiveTippedArrow(final EntityDamageByEntityEvent ev) {
        if (!(ev.getEntity() instanceof Player) || ev.getDamager() == null || TIPPED_ARROW_CLASS == null) {
            return;
        }
        if (!TIPPED_ARROW_CLASS.isAssignableFrom(ev.getDamager().getClass())) {
            return;
        }

        final Arrow tippedArrow = (Arrow) ev.getDamager();

        try {
            final PotionEffectType mainEffect =
                    ((PotionType) Reflection.call(Reflection.call(tippedArrow, "getBasePotionData"), "getType"))
                            .getEffectType();

            //noinspection unchecked
            final boolean impliesHealthRegain = mainEffect.equals(PotionEffectType.HEAL)
                    || mainEffect.equals(PotionEffectType.REGENERATION)
                    || ((List<PotionEffect>) Reflection.call(tippedArrow, "getCustomEffects")).stream()
                    .map(PotionEffect::getType)
                    .anyMatch(effect -> effect.equals(PotionEffectType.HEAL) ||
                            effect.equals(PotionEffectType.REGENERATION));

            if (impliesHealthRegain) {
                // Specific type for tipped arrows?
                manager._setLastHealingType((Player) ev.getEntity(), HealingType.HEALING_POTION);
            }
        }

        // Minecraft version with a different Bukkit API.
        catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException ignored) {
        }
    }
}
