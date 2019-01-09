package me.cassayre.florian.damageslogger.listeners;

import me.cassayre.florian.damageslogger.ReportsManager;
import me.cassayre.florian.damageslogger.report.record.HealRecord;
import me.cassayre.florian.damageslogger.report.record.HealRecord.HealingType;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityRegainHealthEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;

public class PlayerHealsListener implements Listener
{
    private final ReportsManager manager;

    public PlayerHealsListener(ReportsManager manager)
    {
        this.manager = manager;
    }

    @EventHandler
    public void onEntityRegainHealth(EntityRegainHealthEvent e)
    {
        if(e.getEntity() instanceof Player)
        {
            final Player player = (Player) e.getEntity();
            final HealingType healingType;

            switch (e.getRegainReason())
            {
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

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent e)
    {
        final Material material = e.getItem().getType();
        final boolean isNotchApple = e.getItem().getDurability() == 1; // FIXME Will break with 1.13

        final HealingType healingType;

        if (material == Material.GOLDEN_APPLE)
        {
            healingType = isNotchApple ? HealingType.NOTCH_APPLE : HealingType.GOLDEN_APPLE;
        }
        else if (material == Material.POTION)
        {
            healingType = HealingType.HEALING_POTION;
        }
        else
        {
            healingType = HealingType.UNKNOWN;
        }

        manager._setLastHealingType(e.getPlayer(), healingType);
    }
}
