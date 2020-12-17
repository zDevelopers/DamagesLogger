package me.cassayre.florian.hawk.listeners;

import fr.zcraft.quartzlib.components.i18n.I;
import me.cassayre.florian.hawk.ReportsManager;
import me.cassayre.florian.hawk.report.Report;
import me.cassayre.florian.hawk.report.ReportEvent;
import me.cassayre.florian.hawk.report.record.DamageRecord;
import org.bukkit.Material;
import org.bukkit.entity.AbstractArrow;
import org.bukkit.entity.DragonFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Firework;
import org.bukkit.entity.FishHook;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.LlamaSpit;
import org.bukkit.entity.Mob;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.ShulkerBullet;
import org.bukkit.entity.SizedFireball;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.entity.ThrowableProjectile;
import org.bukkit.entity.ThrownPotion;
import org.bukkit.entity.WitherSkull;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EntityEquipment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.projectiles.ProjectileSource;

public class PlayerDamagesListener implements Listener {
    private final ReportsManager manager;

    public PlayerDamagesListener(ReportsManager manager) {
        this.manager = manager;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent ev) {
        manager.getTrackedReportsFor(ev.getEntity())
                .filter(Report::isStoppingTrackOnDeath)
                .forEach(report -> report.untrack(ev.getEntity()));

        manager.getTrackedReportsFor(ev.getEntity())
                .filter(Report::isAddingDefaultEvents)
                .forEach(report -> report.record(ReportEvent.withPlayer(
                        ReportEvent.EventType.GOLD,
                        I.t("Death of {0}", ev.getEntity().getName()),
                        ev.getDeathMessage(),
                        ev.getEntity()
                )));
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent ev) {
        if (!(ev.getEntity() instanceof Player)) {
            return;
        }

        final Player player = (Player) ev.getEntity();
        final double damages = ev.getFinalDamage();
        final boolean isLethal = player.getHealth() - damages <= 0;

        if (damages < 0.01) {
            return;
        }

        final DamageRecord record = computeRecord(ev, player, damages, isLethal);

        if (record == null) {
            return;
        }

        manager.getTrackedReportsFor(player).forEach(report -> report.record(record));
    }

    /**
     * Retrieves the item in the entity's main hand, if this makes sense, or null if
     * none is found.
     * @param entity The entity.
     * @return The item stack in its main hand, or null if either the hand is empty or there is no main hand.
     */
    private ItemStack getItemInMainHand(final Entity entity) {
        final EntityEquipment equipment = entity instanceof LivingEntity
                ? ((LivingEntity) entity).getEquipment() : null;
        return equipment != null ? equipment.getItemInMainHand() : null;
    }

    @SuppressWarnings("CheckStyle")
    private DamageRecord computeRecord(final EntityDamageEvent ev, final Player damaged, final double damages, final boolean lethal) {
        if (ev instanceof EntityDamageByEntityEvent) {
            final Entity damager = ((EntityDamageByEntityEvent) ev).getDamager();
            final ItemStack weapon = getItemInMainHand(damager);

            if (damager instanceof Player) {
                return new DamageRecord(damaged, damages, weapon, (Player) damager, lethal);
            }

            else if (damager instanceof Mob) {
                return new DamageRecord(damaged, damages, weapon, damager.getType(), lethal);
            }

            else if (damager instanceof Projectile) {
                final ProjectileSource shooter = ((Projectile) damager).getShooter();

                if (shooter instanceof Entity) {
                    if (damager instanceof AbstractArrow || damager instanceof FishHook) {
                        final ItemStack bowTridentOrFishingRod = getItemInMainHand((Entity) shooter);
                        if (shooter instanceof Player) {
                            return new DamageRecord(damaged, damages, bowTridentOrFishingRod, (Player) shooter, lethal);
                        } else if (shooter instanceof LivingEntity) {
                            return new DamageRecord(damaged, damages, bowTridentOrFishingRod, ((LivingEntity) shooter).getType(), lethal);
                        }
                    }

                    else if (damager instanceof ShulkerBullet) {
                        return new DamageRecord(damaged, damages, null, EntityType.SHULKER, lethal);
                    }

                    else if (damager instanceof LlamaSpit) {
                        return new DamageRecord(damaged, damages, null, EntityType.LLAMA, lethal);
                    }

                    // These are ender pearls, snowballs, experience bottles, and eggs. They should not cause any
                    // damage but we never know what changes a plugin could make.
                    else if (damager instanceof ThrowableProjectile) {
                        if (shooter instanceof Player) {
                            return new DamageRecord(damaged, damages, ((ThrowableProjectile) damager).getItem(), (Player) shooter, lethal);
                        } else if (shooter instanceof LivingEntity) {
                            return new DamageRecord(damaged, damages, ((ThrowableProjectile) damager).getItem(), ((LivingEntity) shooter).getType(), lethal);
                        }
                    }

                    else if (damager instanceof ThrownPotion) {
                        if (shooter instanceof Player) {
                            return manager._setLastMagicDamage(damaged, new DamageRecord(damaged, damages, ((ThrownPotion) damager).getItem(), (Player) shooter, lethal));
                        } else if (shooter instanceof LivingEntity) {
                            return new DamageRecord(damaged, damages, ((ThrownPotion) damager).getItem(), ((LivingEntity) shooter).getType(), lethal);
                        }
                    }

                    else if (damager instanceof Fireball) {
                        if (damager instanceof DragonFireball) {
                            return new DamageRecord(damaged, damages, null, EntityType.ENDER_DRAGON, lethal);
                        } else if (damager instanceof WitherSkull) {
                            return manager._setLastMagicDamage(damaged, new DamageRecord(damaged, damages, null, EntityType.WITHER, lethal));
                        } else if (damager instanceof SizedFireball) {
                            if (shooter instanceof Player) {
                                return new DamageRecord(damaged, damages, ((SizedFireball) damager).getDisplayItem(), (Player) shooter, lethal);
                            } else if (shooter instanceof LivingEntity) {
                                return new DamageRecord(damaged, damages, ((SizedFireball) damager).getDisplayItem(), ((LivingEntity) shooter).getType(), lethal);
                            }
                        }
                    }

                    else if (damager instanceof Firework) {
                        if (shooter instanceof Player) {
                            return new DamageRecord(damaged, damages, new ItemStack(Material.FIREWORK_ROCKET), (Player) shooter, lethal);
                        } else if (shooter instanceof LivingEntity) {
                          return new DamageRecord(damaged, damages, new ItemStack(Material.FIREWORK_ROCKET), ((LivingEntity) shooter).getType(), lethal);
                        } else {
                            return new DamageRecord(damaged, damages, null, DamageRecord.DamageCause.BLOCK_EXPLOSION, lethal);
                        }
                    }
                }

                return new DamageRecord(damaged, damages, null, DamageRecord.DamageCause.PROJECTILE, lethal);
            }

            else if (damager instanceof TNTPrimed) {
                return new DamageRecord(damaged, damages, null, DamageRecord.DamageCause.BLOCK_EXPLOSION, lethal);
            }

            return new DamageRecord(damaged, damages, null, DamageRecord.DamageCause.fromBukkit(ev.getCause()), lethal);
        }

        else {
            switch (ev.getCause()) {
                case WITHER:
                    final DamageRecord lastWitherRecord = manager._getLastMagicDamage(damaged);
                    if (lastWitherRecord != null) {
                        switch (lastWitherRecord.getDamageCause()) {
                            // Who knows, if players are given wither potions?
                            case PLAYER:
                            case WITHER:
                                return lastWitherRecord.cloneWithDamage(damages, lethal);

                            case ENTITY:
                                if (lastWitherRecord.getEntityDamager() == EntityType.WITHER_SKELETON) {
                                    return lastWitherRecord.cloneWithDamage(damages, lethal);
                                }
                        }
                    }

                    return new DamageRecord(damaged, damages, null, DamageRecord.DamageCause.WITHER, lethal);

                case POISON:
                    final DamageRecord lastPoisonRecord = manager._getLastMagicDamage(damaged);
                    if (lastPoisonRecord != null) {
                        return lastPoisonRecord.cloneWithDamage(damages, lethal);
                    } else {
                        return manager._setLastMagicDamage(damaged, new DamageRecord(damaged, damages, null, DamageRecord.DamageCause.MAGIC, lethal));
                    }

                case DRAGON_BREATH:
                    return new DamageRecord(damaged, damages, null, EntityType.ENDER_DRAGON, lethal);

                default:
                    return new DamageRecord(damaged, damages, null, DamageRecord.DamageCause.fromBukkit(ev.getCause()), lethal);
            }
        }
    }
}
