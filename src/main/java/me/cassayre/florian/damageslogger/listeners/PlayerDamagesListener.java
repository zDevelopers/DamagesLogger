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

    private Class<?> TIPPED_ARROW_CLASS;

    public PlayerDamagesListener(ReportsManager manager)
    {
        this.manager = manager;

        // Tipped arrows were not available in Minecraft 1.8
        try
        {
            TIPPED_ARROW_CLASS = Class.forName("org.bukkit.entity.TippedArrow");
        }
        catch (ClassNotFoundException e)
        {
            TIPPED_ARROW_CLASS = null;
        }
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

        final DamageType damageType = getDamageType(ev, player);
        final Weapon weapon = getWeapon(ev, player);

        final DamageRecord record;

        if (damageType == DamageType.PLAYER)
        {
            record = new DamageRecord(player, damages, weapon, getPlayerDamager(ev, player), isLethal);
        }
        else
        {
            record = new DamageRecord(player, damages, weapon, damageType, isLethal);
        }

        manager.getTrackedReportsFor(player).forEach(report -> report.record(record));

        manager._setLastDamageType(player, damageType);
        manager._setLastWeapon(player, weapon);
    }

    private DamageType getDamageType(final EntityDamageEvent ev, final Player damaged)
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

                // Separate FALLING_BLOCK?
                case SUFFOCATION:
                case FALLING_BLOCK:
                    return DamageType.SUFFOCATION;

                case DROWNING:
                    return DamageType.DROWNING;

                case STARVATION:
                    return DamageType.STARVATION;

                case WITHER:
                    switch (manager._getLastDamageType(damaged))
                    {
                        // Who knows, if players are given wither potions?
                        case PLAYER:
                            return DamageType.PLAYER;

                        case WITHER:
                            return DamageType.WITHER;

                        case WITHER_SKELETON:
                        default:
                            return DamageType.WITHER_SKELETON;
                    }

                case POISON:
                    switch (manager._getLastDamageType(damaged))
                    {
                        case PLAYER:
                            return DamageType.PLAYER;

                        case CAVE_SPIDER:
                            return DamageType.CAVE_SPIDER;

                        case WITCH:
                            return DamageType.WITCH;

                        case WITHER:
                            return DamageType.WITHER;

                        case WITHER_SKELETON:
                            return DamageType.WITHER_SKELETON;

                        default:
                            // TODO add 1.13+ fishes
                            return DamageType.UNKNOWN;
                    }

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

    private Player getPlayerDamager(final EntityDamageEvent ev, final Player damaged)
    {
        if (ev instanceof EntityDamageByEntityEvent)
        {
            final Entity damager = ((EntityDamageByEntityEvent) ev).getDamager();

            if (damager instanceof Player)
            {
                return (Player) damager;
            }

            else if (damager instanceof Arrow)
            {
                if (((Arrow) damager).getShooter() instanceof Player)
                {
                    if (TIPPED_ARROW_CLASS != null && TIPPED_ARROW_CLASS.isAssignableFrom(damager.getClass()))
                    {
                        manager._setLastMagicDamager(damaged, (Player) ((Arrow) damager).getShooter());
                    }

                    return (Player) ((Arrow) damager).getShooter();
                }
                else return null;
            }

            else if (damager instanceof ThrownPotion)
            {
                if (((ThrownPotion) damager).getShooter() instanceof Player)
                {
                    manager._setLastMagicDamager(damaged, (Player) ((ThrownPotion) damager).getShooter());
                    return (Player) ((ThrownPotion) damager).getShooter();
                }
                else return null;
            }

            else return null;
        }

        else if (ev.getCause() == DamageCause.POISON && manager._getLastDamageType(damaged) == DamageType.PLAYER)
        {
            return manager._getLastMagicDamager(damaged);
        }

        else return null;
    }

    private Weapon getWeapon(final EntityDamageEvent ev, final Player damaged)
    {
        final ItemStack weapon;

        if (ev instanceof EntityDamageByEntityEvent)
        {
            final Entity damager = ((EntityDamageByEntityEvent) ev).getDamager();

            if (damager instanceof Arrow)
            {
                if (((Arrow) damager).getShooter() instanceof LivingEntity)
                {
                    weapon = ((LivingEntity) ((Arrow) damager).getShooter()).getEquipment().getItemInHand();
                }
                else
                {
                    return Weapon.UNKNOWN;
                }
            }
            else if (damager instanceof LivingEntity)
            {
                weapon = ((LivingEntity) damager).getEquipment().getItemInHand();
            }
            else if (damager instanceof ThrownPotion)
            {
                return Weapon.MAGIC;
            }
            else
            {
                return Weapon.UNKNOWN;
            }
        }
        else
        {
            if (ev.getCause() == DamageCause.MAGIC || ev.getCause() == DamageCause.POISON)
            {
                switch (manager._getLastWeapon(damaged))
                {
                    case BOW:
                        return Weapon.BOW;

                    case MAGIC:
                    default:
                        return Weapon.MAGIC;
                }
            }

            // Wither Skeleton
            // FIXME Magic Value: a Wither Skeleton could use something else than a stone sword in some special cases.
            else if (ev.getCause() == DamageCause.WITHER && manager._getLastWeapon(damaged) == Weapon.SWORD_STONE)
            {
                return Weapon.SWORD_STONE;
            }

            else return Weapon.UNKNOWN;
        }

        if (ev.getCause() == DamageCause.THORNS)
        {
            return Weapon.THORNS;
        }

        switch (weapon.getType())
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
            case BOW: return Weapon.BOW;
            default: return Weapon.FISTS;
        }
    }
}
