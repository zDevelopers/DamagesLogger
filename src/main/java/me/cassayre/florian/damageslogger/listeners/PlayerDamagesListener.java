package me.cassayre.florian.damageslogger.listeners;

import fr.zcraft.zlib.components.i18n.I;
import me.cassayre.florian.damageslogger.ReportsManager;
import me.cassayre.florian.damageslogger.report.Report;
import me.cassayre.florian.damageslogger.report.ReportEvent;
import me.cassayre.florian.damageslogger.report.record.DamageRecord;
import me.cassayre.florian.damageslogger.report.record.DamageRecord.DamageType;
import me.cassayre.florian.damageslogger.report.record.DamageRecord.Weapon;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

public class PlayerDamagesListener implements Listener
{
    private final ReportsManager manager;

    public PlayerDamagesListener(ReportsManager manager)
    {
        this.manager = manager;
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDeath(final PlayerDeathEvent ev)
    {
        manager.getTrackedReportsFor(ev.getEntity())
                .filter(Report::isStoppingTrackOnDeath)
                .forEach(report -> report.untrackPlayer(ev.getEntity()));

        manager.getTrackedReportsFor(ev.getEntity())
                .filter(Report::isAddingDefaultEvents)
                .forEach(report -> report.record(ReportEvent.withPlayer(
                        ReportEvent.EventType.GOLD,
                        I.t("Death of {0}", ev.getEntity().getName()),
                        ev.getDeathMessage(),
                        ev.getEntity()
                )));
    }

    @EventHandler (priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageEvent ev)
    {
        if (!(ev.getEntity() instanceof Player)) return;

        final Player player = (Player) ev.getEntity();
        final double damages = ev.getFinalDamage();

        final boolean isLethal = player.getHealth() - damages <= 0;

        final DamageType damageType = getDamageType(ev);
        final Weapon weapon = ev instanceof EntityDamageByEntityEvent ? getWeapon(((EntityDamageByEntityEvent) ev)) : null;

        final DamageRecord record;

        if (damageType == DamageType.PLAYER)
        {
            record = new DamageRecord(player, damages, weapon, getPlayerDamager((ev)), isLethal);
        }
        else
        {
            record = new DamageRecord(player, damages, weapon, damageType, isLethal);
        }

        manager.getTrackedReportsFor(player).forEach(report -> report.record(record));
    }

    private DamageType getDamageType(final EntityDamageEvent ev)
    {
        if (ev instanceof EntityDamageByEntityEvent)
        {
            final Entity damager = ((EntityDamageByEntityEvent) ev).getDamager();

            if (damager instanceof Player)
            {
                return DamageType.PLAYER;
            }
            else if (damager instanceof Zombie)
            {
                final Zombie zombie = (Zombie) damager;

                if (zombie instanceof PigZombie)
                {
                    return DamageType.PIGMAN;
                }
                else if (zombie.isVillager())
                {
                    return DamageType.ZOMBIE_VILLAGER;
                }
                else
                {
                    return DamageType.ZOMBIE;
                }
            }
            else if (damager instanceof Skeleton)
            {
                final Skeleton skeleton = (Skeleton) damager;

                // Might be possible in some special cases...
                return skeleton.getSkeletonType() == Skeleton.SkeletonType.NORMAL ? DamageType.SKELETON : DamageType.WITHER_SKELETON;
            }
            else if (damager instanceof Witch)
            {
                return DamageType.WITCH;
            }
            else if (damager instanceof Arrow)
            {
                final Arrow arrow = (Arrow) damager;

                if (arrow.getShooter() instanceof Player)
                {
                    return DamageType.PLAYER;
                }
                else if (arrow.getShooter() instanceof Skeleton)
                {
                    return DamageType.SKELETON;
                }
            }
            else if (damager instanceof ThrownPotion)
            {
                if (((ThrownPotion) damager).getShooter() instanceof Player)
                {
                    return DamageType.PLAYER;
                }
                else if (((ThrownPotion) damager).getShooter() instanceof Witch)
                {
                    return DamageType.WITCH;
                }
            }
            else if (damager instanceof Spider)
            {
                final Spider spider = (Spider) damager;

                return spider instanceof CaveSpider ? DamageType.CAVE_SPIDER : DamageType.SPIDER;
            }
            else if (damager instanceof Creeper)
            {
                return DamageType.CREEPER;
            }
            else if (damager instanceof Enderman)
            {
                return DamageType.ENDERMAN;
            }
            else if (damager instanceof Slime)
            {
                final Slime slime = (Slime) damager;

                return slime instanceof MagmaCube ? DamageType.MAGMA_CUBE : DamageType.SLIME;
            }
            else if (damager instanceof Ghast)
            {
                return DamageType.GHAST;
            }
            else if (damager instanceof Blaze)
            {
                return DamageType.BLAZE;
            }
            else if (damager instanceof Fireball)
            {
                final Fireball fireball = (Fireball) damager;

                if (fireball.getShooter() instanceof Blaze)
                {
                    return DamageType.BLAZE;
                }
                else if (fireball.getShooter() instanceof Ghast)
                {
                    return DamageType.GHAST;
                }
            }
            else if (damager instanceof Wolf)
            {
                final Wolf wolf = (Wolf) damager;

                // Don't ask me how the wold could be non-angry
                return wolf.isAngry() ? DamageType.ANGRY_WOLF : DamageType.WOLF;
            }
            else if (damager instanceof Silverfish)
            {
                return DamageType.SILVERFISH;
            }
            else if (damager instanceof IronGolem)
            {
                return DamageType.IRON_GOLEM;
            }
            else if (damager instanceof LightningStrike)
            {
                return DamageType.THUNDERBOLT;
            }
            else if (damager instanceof EnderDragon)
            {
                return DamageType.ENDER_DRAGON; // Let's just hope for it
            }
            else if (damager instanceof Wither)
            {
                return DamageType.WITHER;
            }
            else if (damager instanceof TNTPrimed)
            {
                return DamageType.TNT;
            }
        }

        else
        {
            switch (ev.getCause())
            {
                case FIRE:
                case FIRE_TICK:
                    return DamageType.FIRE;

                case LAVA:
                    return DamageType.LAVA;

                case CONTACT:
                    return DamageType.CACTUS;

                case FALL:
                    return DamageType.FALL;

                case SUFFOCATION:
                case FALLING_BLOCK:
                    // Separate FALLING_BLOCK?
                    return DamageType.SUFFOCATION;

                case DROWNING:
                    return DamageType.DROWNING;

                case STARVATION:
                    return DamageType.STARVATION;

                case WITHER:
                    // TODO Check for latest damage (either Wither or Wither Skeleton)
                    return DamageType.WITHER_SKELETON;

                case POISON:
                    // FIXME Check for latest magic damage (either Player, Witch, Wither or Wither Skeleton)
                    return DamageType.UNKNOWN;

                default:
                    // Enum value not available in Minecraft 1.8
                    if (ev.getCause().name().equals("DRAGON_BREATH"))
                    {
                        return DamageType.ENDER_DRAGON;
                    }
            }
        }

        return DamageType.UNKNOWN;
    }

    private Player getPlayerDamager(final EntityDamageEvent ev)
    {
        // FIXME will not always be true for poison
        if (!(ev instanceof EntityDamageByEntityEvent)) return null;

        final Entity damager = ((EntityDamageByEntityEvent) ev).getDamager();

        if (damager instanceof Player)
        {
            return (Player) damager;
        }

        else if (damager instanceof Arrow)
        {
            if (((Arrow) damager).getShooter() instanceof Player)
            {
                return (Player) ((Arrow) damager).getShooter();
            }
            else return null;
        }

        else if (damager instanceof ThrownPotion)
        {
            if (((ThrownPotion) damager).getShooter() instanceof Player)
            {
                return (Player) ((ThrownPotion) damager).getShooter();
            }
            else return null;
        }

        // FIXME check for latest magic damage in case it's from a player

        else return null;
    }

    private Weapon getWeapon(final EntityDamageByEntityEvent ev)
    {
        final ItemStack weapon;

        if (ev.getDamager() instanceof Arrow)
        {
            if (((Arrow) ev.getDamager()).getShooter() instanceof LivingEntity)
            {
                return Weapon.BOW;
            }
            else
            {
                return Weapon.UNKNOWN;
            }
        }
        else if (ev.getDamager() instanceof LivingEntity)
        {
            weapon = ((LivingEntity) ev.getDamager()).getEquipment().getItemInHand();
        }
        else if (ev.getDamager() instanceof ThrownPotion)
        {
            return Weapon.MAGIC;
        }
        else
        {
            return Weapon.UNKNOWN;
        }

        if (ev.getCause() == DamageCause.ENTITY_ATTACK || ev.getCause().name().equals("ENTITY_SWEEP_ATTACK"))
        {
            switch(weapon.getType())
            {
                case WOOD_SWORD: return Weapon.SWORD_WOOD;
                case GOLD_SWORD: return Weapon.SWORD_GOLD;
                case STONE_SWORD: return Weapon.SWORD_STONE;
                case IRON_SWORD: return Weapon.SWORD_IRON;
                case DIAMOND_SWORD: return Weapon.SWORD_DIAMOND;
                case WOOD_AXE: return Weapon.AXE_WOOD;
                case GOLD_AXE: return Weapon.AXE_GOLD;
                case STONE_AXE: return Weapon.AXE_STONE;
                case IRON_AXE: return Weapon.AXE_IRON;
                case DIAMOND_AXE: return Weapon.AXE_DIAMOND;
                default: return Weapon.FISTS;
            }
        }

        else if (ev.getCause() == DamageCause.THORNS)
        {
            return Weapon.THORNS;
        }

        else return Weapon.UNKNOWN;
    }
}
